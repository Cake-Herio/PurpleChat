package com.purplechat.service.impl;

import com.purplechat.entity.config.AppConfig;
import com.purplechat.entity.constants.Constants;
import com.purplechat.entity.dto.MessageSendDto;
import com.purplechat.entity.dto.SysSettingDto;
import com.purplechat.entity.dto.TokenUserInfoDto;
import com.purplechat.entity.po.ChatMessage;
import com.purplechat.entity.po.ChatSession;
import com.purplechat.entity.po.UserContact;
import com.purplechat.entity.query.ChatMessageQuery;
import com.purplechat.entity.query.ChatSessionQuery;
import com.purplechat.entity.query.UserContactQuery;
import com.purplechat.entity.vo.PaginationResultVO;
import com.purplechat.entity.query.SimplePage;
import com.purplechat.enums.*;
import com.purplechat.exception.BusinessException;
import com.purplechat.mappers.ChatMessageMapper;
import com.purplechat.mappers.ChatSessionMapper;
import com.purplechat.mappers.UserContactMapper;
import com.purplechat.redis.RedisComponent;
import com.purplechat.service.ChatMessageService;
import com.purplechat.utils.CopyTools;
import com.purplechat.utils.DateUtils;
import com.purplechat.utils.SaveFile;
import com.purplechat.utils.StringTools;
import com.purplechat.websocket.netty.HandlerMessage;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @Description:聊天消息表Service
 * @Author:文翔
 * @date:2024/07/28
 */
@Service("chatMessageService")
public class ChatMessageServiceImpl implements ChatMessageService {

    private static final Logger logger = LoggerFactory.getLogger(ChatMessageServiceImpl.class);

    @Resource
    private ChatMessageMapper<ChatMessage, ChatMessageQuery> chatMessageMapper;
    @Resource
    private ChatSessionMapper<ChatSession, ChatSessionQuery> chatSessionMapper;
    @Resource
    private RedisComponent redisComponent;
    @Resource
    private HandlerMessage handlerMessage;
    @Resource
    private UserContactMapper<UserContact, UserContactQuery> userContactMapper;

    @Resource
    private AppConfig appConfig;


    /**
     * 根据条件查询列表
     */
    @Override
    public List<ChatMessage> findListByParam(ChatMessageQuery query) {
        return this.chatMessageMapper.selectList(query);
    }

    /**
     * 根据条件查询数量
     */
    @Override
    public Integer findCountByParam(ChatMessageQuery query) {
        return this.chatMessageMapper.selectCount(query);
    }

    /**
     * 分页查询
     */
    @Override
    public PaginationResultVO<ChatMessage> findListByPage(ChatMessageQuery query) {
        Integer count = this.findCountByParam(query);
        Integer pageSize = query.getPageNo() == null ? PageSize.SIZE15.getSize() : query.getPageSize();
        SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
        query.setSimplePage(page);
        List<ChatMessage> list = this.findListByParam(query);
        PaginationResultVO<ChatMessage> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(ChatMessage bean) {
        return this.chatMessageMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<ChatMessageQuery> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.chatMessageMapper.insertBatch(listBean);
    }

    /**
     * 批量新增/修改
     */
    @Override
    public Integer addOrUpdateBatch(List<ChatMessageQuery> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.chatMessageMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 根据MessageId查询
     */
    public ChatMessage getChatMessageByMessageId(Long messageId) {
        return this.chatMessageMapper.selectByMessageId(messageId);
    }

    /**
     * 根据MessageId修改
     */
    public Integer updateChatMessageByMessageId(ChatMessage bean, Long messageId) {
        return this.chatMessageMapper.updateByMessageId(bean, messageId);
    }

    /**
     * 根据MessageId删除
     */
    public Integer deleteChatMessageByMessageId(Long messageId) {
        return this.chatMessageMapper.deleteByMessageId(messageId);
    }


    //当抛902 或 903时仍然先存储消息
    @Override
    public MessageSendDto  saveMessage(ChatMessage chatMessage, TokenUserInfoDto tokenUserInfoDto) throws BusinessException {
        boolean isFailSend = false;
        //分机器人好友和其他 分别做判断
        //如果是不是机器人 TODO 老罗代码有问题 P33
        UserContactTypeEnum userContactTypeEnum = UserContactTypeEnum.getByPrefix(chatMessage.getContactId());
        if (userContactTypeEnum == null) {
            logger.info("联系人类型错误");
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        if (!Constants.ROBOT_UID.equals(chatMessage.getContactId())) {
            //获取当前的所有联系人
            List<String> userContactList = redisComponent.getUserContactList(tokenUserInfoDto.getUserId());
            //判断是否存在list中 若不存在则抛异常
            if (!userContactList.contains(chatMessage.getContactId())) {
                isFailSend = true;
            }
        }


        //判断消息类型
        Integer messageType = chatMessage.getMessageType();
        MessageTypeEnum messageTypeEnum = MessageTypeEnum.getByType(messageType);
        if (messageTypeEnum == null || !ArrayUtils.contains(new Integer[]{MessageTypeEnum.CHAT.getType(), MessageTypeEnum.MEDIA_CHAT.getType()}, messageType)) {
            logger.info("消息类型错误");
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        String sessionId = null;
        if (Objects.equals(userContactTypeEnum, UserContactTypeEnum.USER)) {
            sessionId = StringTools.getChatSessionId4User(new String[]{tokenUserInfoDto.getUserId(), chatMessage.getContactId()});
        } else {
            sessionId = StringTools.getChatSessionId4Group(chatMessage.getContactId());
        }
        Long curTime = System.currentTimeMillis();
        //判断消息类型 如果是文字则是已发送状态 如果是文件 则是发送中
        Integer status = null;
        if(chatMessage.getStatus() == null){
            status = MessageTypeEnum.CHAT.getType().equals(messageType) ? MessageStatusEnum.SENT.getType() : MessageStatusEnum.SENDING.getType();
        }else{
            status = chatMessage.getStatus();
        }
        //对消息进行防止html注入
        String messageContent = StringTools.cleanHtmlTag(chatMessage.getMessageContent());

        //更新会话
        ChatSession chatSession = new ChatSession();
        chatSession.setLastReceiveTime(curTime);
        chatSession.setSessionId(sessionId);
        //如果是群聊 将展示昵称
        if (Objects.equals(userContactTypeEnum, UserContactTypeEnum.USER)) {
            chatSession.setLastMessage(messageContent);
        } else {
            chatSession.setLastMessage(tokenUserInfoDto.getNickName() + "：" + messageContent);
        }
        //更新会话
        chatSessionMapper.updateBySessionId(chatSession, sessionId);



        //更新消息
        chatMessage.setSessionId(sessionId);
        status = isFailSend? MessageStatusEnum.SEND_FAIL.getType() : status;
        chatMessage.setStatus(status);
        chatMessage.setContactType(userContactTypeEnum.getType());
        chatMessage.setSendTime(curTime);
        chatMessage.setSendUserId(tokenUserInfoDto.getUserId());
        chatMessage.setSendUserNickName(tokenUserInfoDto.getNickName());
        //插入消息 这里会为chatMessage 自动增加id
        chatMessageMapper.insert(chatMessage);



        //发消息
        MessageSendDto messageSendDto = CopyTools.copy(chatMessage, MessageSendDto.class);
        handlerMessage.sendMessage(messageSendDto);



        //TODO 如果是机器人 调用gpt接口
        if (Constants.ROBOT_UID.equals(chatMessage.getContactId())) {

        }

        //这里抛出异常
        if(isFailSend) {
            if (Objects.equals(userContactTypeEnum, UserContactTypeEnum.USER)) {
                throw new BusinessException(ResponseCodeEnum.CODE_902);
            } else if (Objects.equals(userContactTypeEnum, UserContactTypeEnum.GROUP)) {
                throw new BusinessException(ResponseCodeEnum.CODE_903);
            }
        }

        return messageSendDto;
    }

    @Override
    public void saveMessageFile(String userId, Long messageId, MultipartFile file, MultipartFile cover) throws BusinessException, IOException {
        ChatMessage chatMessage = chatMessageMapper.selectByMessageId(messageId);
        if (null == chatMessage) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //判断token得到的user是否是发消息人
        if (!userId.equals(chatMessage.getSendUserId())) {
            logger.info("用户不是发消息人");
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //读取设置判断大小
        SysSettingDto sysSetting = redisComponent.getSysSetting();
        //这里通过文件后缀判断文件类型 后续可优化
        String fileSuffix = StringTools.getFileSuffix(file.getOriginalFilename());
        String lowerCaseSuffix = fileSuffix.toLowerCase();
        boolean isImage = ArrayUtils.contains(Constants.IMAGE_SUFFIX_LIST, lowerCaseSuffix);
        boolean isVideo = ArrayUtils.contains(Constants.VIDEO_SUFFIX_LIST, lowerCaseSuffix);


        if(file.getSize() > sysSetting.getMaxFileSize() * Constants.FILE_SIZE_MB){
            logger.info("大小太大");
            throw new BusinessException("文件大小太大");
        }




        //存储文件到本地 通过messageId的方式存储
        SaveFile.saveMultipartFile(file, cover, messageId.toString(), appConfig, lowerCaseSuffix);

        //文件上传到服务器了 说明已经上传成功 更改数据库的状态
        ChatMessage uploadInfo = new ChatMessage();
        uploadInfo.setStatus(MessageStatusEnum.SENT.getType());
        //应该联合查询sending 和 id
        //！！状态强调从老状态转变为新状态  乐观锁
        ChatMessageQuery chatMessageQuery = new ChatMessageQuery();
        chatMessageQuery.setMessageId(messageId);
        chatMessageQuery.setStatus(MessageStatusEnum.SENDING.getType());
        chatMessageMapper.updateByParam(uploadInfo, chatMessageQuery);

        //发消息
        MessageSendDto messageSendDto = new MessageSendDto();
        messageSendDto.setContactId(chatMessage.getContactId());
        messageSendDto.setStatus(MessageStatusEnum.SENT.getType());
        messageSendDto.setMessageType(MessageTypeEnum.FILE_UPLOAD.getType());
        messageSendDto.setMessageId(messageId);
        handlerMessage.sendMessage(messageSendDto);

    }

    //从服务器端寻找数据并传回给前端
    @Override
    public File downloadFile(TokenUserInfoDto tokenUserInfoDto, Long fileId, boolean showCover) throws BusinessException {
        //此处fileId就是messageId 通过fileId就可以查到相关消息
        //TODO 是否可以直接查redis 后面回过头研究一下这段代码
        ChatMessage chatMessage = chatMessageMapper.selectByMessageId(fileId);
        if (null == chatMessage) {
            logger.info("文件不存在");
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        String contactId = chatMessage.getContactId();
        UserContactTypeEnum userContactTypeEnum = UserContactTypeEnum.getByPrefix(contactId);
        if (userContactTypeEnum == null) {
            logger.info("联系人类型错误");
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //分别判断
        if (Objects.equals(userContactTypeEnum, UserContactTypeEnum.USER)) {
            //判断是否是自己的消息或者联系人的消息  userId就是联系人 注意这边是对方的视角

            if (!tokenUserInfoDto.getUserId().equals(chatMessage.getSendUserId()) && !tokenUserInfoDto.getUserId().equals(chatMessage.getContactId())) {
                logger.info("非法获取");
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
        } else {
            //判断自己是否在群聊中
            UserContactQuery userContactQuery = new UserContactQuery();
            userContactQuery.setUserId(tokenUserInfoDto.getUserId());
            userContactQuery.setContactId(contactId);
            userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            userContactQuery.setContactType(UserContactTypeEnum.GROUP.getType());

            Integer count = userContactMapper.selectCount(userContactQuery);
            if (count == 0) {
                logger.info("你已不在群聊");
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
        }
        //读文件
        String month = DateUtils.format(new Date(chatMessage.getSendTime()), DateTimePatternEnum.YYYYMM.getPattern());
        File folder = new File(appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + month);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String fileName = chatMessage.getFileName();
        String fileExtName = StringTools.getFileSuffix(fileName);


        //如果是图片 修改后缀为png
        if(ArrayUtils.contains(Constants.IMAGE_SUFFIX_LIST, fileExtName)){
            fileExtName = ".png";
        }

        //如果是视频，修改后缀为mp4
        if(ArrayUtils.contains(Constants.VIDEO_SUFFIX_LIST, fileExtName)){
            fileExtName = ".mp4";
        }
        String fileRealName = fileId + fileExtName;
        if (showCover) {
            fileRealName = StringTools.removeFileExtension(fileRealName) + Constants.COVER_IMAGE_SUFFIX;
        }
        logger.info("下载文件路径为：" + folder.getPath() + "/" + fileRealName);

        String filePath = StringTools.normalizePath(folder.getPath() + "\\" + fileRealName);

        File file = new File(filePath);
        if (!file.exists()) {
            logger.info("文件不存在： {} path为：{}", fileId, filePath);
            throw new BusinessException(ResponseCodeEnum.CODE_602);
        }
        return file;
    }
}