package com.purplechat.service.impl;

import com.purplechat.entity.config.AppConfig;
import com.purplechat.entity.dto.MessageSendDto;
import com.purplechat.entity.dto.SysSettingDto;
import com.purplechat.entity.dto.TokenUserInfoDto;
import com.purplechat.entity.po.*;
import com.purplechat.entity.query.*;
import com.purplechat.entity.vo.PaginationResultVO;
import com.purplechat.enums.*;
import com.purplechat.exception.BusinessException;
import com.purplechat.mappers.*;
import com.purplechat.redis.RedisComponent;
import com.purplechat.service.ChatSessionUserService;
import com.purplechat.service.GroupInfoService;
import com.purplechat.service.UserContactService;
import com.purplechat.utils.CopyTools;
import com.purplechat.utils.SaveFile;
import com.purplechat.utils.StringTools;
import com.purplechat.websocket.ChannelContextUtil;
import com.purplechat.websocket.netty.HandlerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @Description:Service
 * @Author:文翔
 * @date:2024/07/16
 */
@Service("groupInfoService")
public class GroupInfoServiceImpl implements GroupInfoService {
    private static final Logger logger = LoggerFactory.getLogger(GroupInfoServiceImpl.class);

    @Resource
    private GroupInfoMapper<GroupInfo, GroupInfoQuery> groupInfoMapper;
    @Resource
    private UserContactMapper<UserContact, UserContactQuery> userContactMapper;
    @Resource
    private RedisComponent redisComponent;
    @Resource
    private AppConfig appConfig;
    @Resource
    private ChatSessionMapper<ChatSession, ChatSessionQuery> chatSessionMapper;
    @Resource
    private ChatSessionUserMapper<ChatSessionUser, ChatSessionUserQuery> chatSessionUserMapper;
    @Resource
    private ChatSessionUserService chatSessionUserService;
    @Resource
    private ChannelContextUtil channelContextUtil;
    @Resource
    private ChatMessageMapper<ChatMessage, ChatMessageQuery> chatMessageMapper;
    @Resource
    private HandlerMessage handlerMessage;
    @Resource
    private UserContactService userContactService;
    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;
    @Resource
    @Lazy
    private GroupInfoService groupInfoService;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<GroupInfo> findListByParam(GroupInfoQuery query) {
        return this.groupInfoMapper.selectList(query);
    }

    /**
     * 根据条件查询数量
     */
    @Override
    public Integer findCountByParam(GroupInfoQuery query) {
        return this.groupInfoMapper.selectCount(query);
    }

    /**
     * 分页查询
     */
    @Override
    public PaginationResultVO<GroupInfo> findListByPage(GroupInfoQuery query) {
        Integer count = this.findCountByParam(query);
        Integer pageSize = query.getPageNo() == null ? PageSize.SIZE15.getSize() : query.getPageSize();
        SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
        query.setSimplePage(page);
        List<GroupInfo> list = this.findListByParam(query);
        PaginationResultVO<GroupInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(GroupInfo bean) {
        return this.groupInfoMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<GroupInfoQuery> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.groupInfoMapper.insertBatch(listBean);
    }

    /**
     * 批量新增/修改
     */
    @Override
    public Integer addOrUpdateBatch(List<GroupInfoQuery> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.groupInfoMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 根据GroupId查询
     */
    public GroupInfo getGroupInfoByGroupId(String groupId) {
        return this.groupInfoMapper.selectByGroupId(groupId);
    }

    /**
     * 根据GroupId修改
     */
    public Integer updateGroupInfoByGroupId(GroupInfo bean, String groupId) {
        return this.groupInfoMapper.updateByGroupId(bean, groupId);
    }

    /**
     * 根据GroupId删除
     */
    public Integer deleteGroupInfoByGroupId(String groupId) {
        return this.groupInfoMapper.deleteByGroupId(groupId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveGroup(GroupInfo groupInfo, MultipartFile avatarFile, MultipartFile avatarCover) throws BusinessException, IOException {
        Date date = new Date();
        //新增   修改部分已经判断了是否id真实
        if (StringTools.isEmpty(groupInfo.getGroupId())) {
            GroupInfoQuery groupInfoQuery = new GroupInfoQuery();
            groupInfoQuery.setGroupOwnerId(groupInfo.getGroupOwnerId());
            Integer count = groupInfoMapper.selectCount(groupInfoQuery);


            //通过redis获取sysSettingDto
            SysSettingDto sysSetting = redisComponent.getSysSetting();
            if (sysSetting.getMaxGroupCount() <= count) {
                throw new BusinessException("最多能支持创建" + sysSetting.getMaxGroupCount());
            }


			if(null == avatarFile){
				throw new BusinessException(ResponseCodeEnum.CODE_600);
			}

            //添加群组到联系人 于redis中



            groupInfo.setGroupId(StringTools.getGroupId());
            redisComponent.addUserContact(groupInfo.getGroupOwnerId(), groupInfo.getGroupId());
            List<String> userContactList = redisComponent.getUserContactList(groupInfo.getGroupOwnerId());

            //将联系人通道添加到群组通道
            channelContextUtil.add2GroupByUserId( groupInfo.getGroupId(), groupInfo.getGroupOwnerId());
            groupInfo.setCreateTime(date);
            groupInfo.setStatus(GroupStatusEnum.NORMAL.getStatus());
            this.groupInfoMapper.insert(groupInfo);


            //创建联系人
            UserContact userContact = new UserContact();
            userContact.setUserId(groupInfo.getGroupOwnerId());
            userContact.setContactId(groupInfo.getGroupId());
            userContact.setCreateTime(date);
            userContact.setLastUpdateTime(date);
            userContact.setContactType(UserContactTypeEnum.GROUP.getType());
            userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            this.userContactMapper.insert(userContact);

            //创建会话
            ChatSession chatSession = new ChatSession();
            String chatSessionId = StringTools.getChatSessionId4Group(groupInfo.getGroupId());
            chatSession.setSessionId(chatSessionId);
            chatSession.setLastReceiveTime(date.getTime());
            chatSession.setLastMessage(MessageTypeEnum.GROUP_CREATE.getInitMessage());
            chatSessionMapper.insert(chatSession);

            //群聊只需添加一个
            ChatSessionUser chatSessionUser = new ChatSessionUser();
            chatSessionUser.setUserId(groupInfo.getGroupOwnerId());
            chatSessionUser.setContactId(groupInfo.getGroupId());
            chatSessionUser.setContactName(groupInfo.getGroupName());
            chatSessionUser.setSessionId(chatSessionId);
            chatSessionUserMapper.insert(chatSessionUser);

            //创建消息
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSessionId(chatSessionId);
            chatMessage.setSendUserNickName(groupInfo.getGroupName());
            chatMessage.setMessageType(MessageTypeEnum.GROUP_CREATE.getType());
            chatMessage.setMessageContent(MessageTypeEnum.GROUP_CREATE.getInitMessage());
            chatMessage.setSendTime(date.getTime());
            chatMessage.setContactId(groupInfo.getGroupId());
            chatMessage.setStatus(MessageStatusEnum.SENT.getType());
            chatMessage.setContactType(UserContactTypeEnum.GROUP.getType());

            chatMessageMapper.insert(chatMessage);




            //ws发送消息
            chatSessionUser.setLastMessage(MessageTypeEnum.GROUP_CREATE.getInitMessage());
            chatSessionUser.setLastReceiveTime(date.getTime());
            chatSessionUser.setMemberCount(1);

            MessageSendDto messageSendDto = CopyTools.copy(chatMessage, MessageSendDto.class);
            messageSendDto.setExtendData(chatSessionUser);
            messageSendDto.setLastMessage(chatSessionUser.getLastMessage());
            handlerMessage.sendMessage(messageSendDto);



        } else {
            //修改
            GroupInfo dbGroup = this.groupInfoMapper.selectByGroupId(groupInfo.getGroupId());
            //验证只有群主可以更改
            if (!dbGroup.getGroupOwnerId().equals(groupInfo.getGroupOwnerId())) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }

            this.groupInfoMapper.updateByGroupId(groupInfo, groupInfo.getGroupId());
            //更新相关表冗余信息
            //查看群名是否改变 如果改变需要更新多个表的群名

            if(Objects.equals(dbGroup.getGroupName(), groupInfo.getGroupName())){
                logger.info("群名未改变，无需进行后续操作");
            }else{
                //直接更新昵称即可
                chatSessionUserService.updateNickName(groupInfo.getGroupName(), groupInfo.getGroupId());
            }






        }
		if(null == avatarFile) {
			return;
		}

		//直接调用工具类上传头像
		SaveFile.saveAvatarFile(avatarFile, avatarCover, appConfig, groupInfo.getGroupId());

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dissolutionGroup(String groupOwnerId, String groupId) throws BusinessException {

        GroupInfo groupInfo = this.groupInfoMapper.selectByGroupId(groupId);
        if (null == groupInfo) {
            throw new BusinessException("群聊不存在");
        }
        //校验操作者id是否为群主
        if (!groupInfo.getGroupOwnerId().equals(groupOwnerId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        //删除群组
        GroupInfo delGroup = new GroupInfo();
        delGroup.setStatus(GroupStatusEnum.ABNORMAL.getStatus());
        this.groupInfoMapper.updateByGroupId(delGroup, groupId);
        //移除相关群员的联系人缓存 redis中的每个人在redis中存储该群的缓存
        UserContactQuery deleteInfo = new UserContactQuery();
        deleteInfo.setContactId(groupId);
        deleteInfo.setContactType(UserContactTypeEnum.GROUP.getType());
        deleteInfo.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        List<UserContact> userContacts = this.userContactMapper.selectList(deleteInfo);

        for(UserContact userContact : userContacts){
            redisComponent.removeUserContact(userContact.getUserId(), groupId);
        }


        //与该群相关的联系人都应该将状态改为删除
        UserContactQuery userContactQuery = new UserContactQuery();
        //设置筛选条件 状态为群组， 群组id为groupId
        userContactQuery.setContactType(UserContactTypeEnum.GROUP.getType());
        userContactQuery.setContactId(groupId);

        //设置更新的内容
        UserContact updateUserContact = new UserContact();
        updateUserContact.setStatus(UserContactStatusEnum.DELETE.getStatus());
        updateUserContact.setLastUpdateTime(new Date());
        this.userContactMapper.updateByParam(updateUserContact, userContactQuery);




        //拿到会话Id 存储数据库
        String sessionId = StringTools.getChatSessionId4Group(groupId);
        Date date = new Date();
        String messageContent = MessageTypeEnum.DISSOLUTION_GROUP.getInitMessage();

        //更新会话表
        ChatSession chatSession = new ChatSession();
        chatSession.setSessionId(sessionId);
        chatSession.setLastReceiveTime(date.getTime());
        chatSession.setLastMessage(messageContent);

        chatSessionMapper.updateBySessionId(chatSession, sessionId);

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSessionId(sessionId);
        chatMessage.setSendTime(date.getTime());
        chatMessage.setContactId(groupId);
        chatMessage.setMessageType(MessageTypeEnum.DISSOLUTION_GROUP.getType());
        chatMessage.setMessageContent(messageContent);
        chatMessage.setStatus(MessageStatusEnum.SENT.getType());
        //更新数据库(插入） 并且发送消息
        chatMessageMapper.insert(chatMessage);

        MessageSendDto messageSendDto = CopyTools.copy(chatMessage, MessageSendDto.class);
        handlerMessage.sendMessage(messageSendDto);


    }

    @Override
    public void addOrRemoveGroupUser(TokenUserInfoDto tokenUserInfoDto, String groupId, String selectContacts, Integer type) throws BusinessException {

        GroupInfo groupInfo = groupInfoMapper.selectByGroupId(groupId);


        //首先判断是否是群主操作 而不是非法调用接口
        String userId = tokenUserInfoDto.getUserId();
        if(groupInfo == null || !groupInfo.getGroupOwnerId().equals(userId)){
            logger.info("非群主调用接口，非法操作");
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        //将contacts变成数组
        String[] contactList = selectContacts.split(",");
        for(String userContactId : contactList){
            if(type == 0){
                //分两种 一种是群主踢人 另一种是自己退群
                //不能直接内部调用 否则事务不会生效 应该通过spring管理的bean调用
                groupInfoService.leaveGroup(userContactId, groupId, MessageTypeEnum.REMOVE_GROUP);
            }else if(type ==1){
                //直接调用
                userContactService.addContact(userContactId, groupInfo.getGroupOwnerId(), groupId, UserContactTypeEnum.GROUP.getType(), null);
            }
        }







    }

    /**
     *
     * @param userId 是退群人的userId
     * @param groupId
     * @param messageTypeEnum
     * @throws BusinessException
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void leaveGroup(String userId, String groupId, MessageTypeEnum messageTypeEnum) throws BusinessException {
        //自己只能解散 不能群主退 查是否是非法调用
        GroupInfo groupInfo = groupInfoMapper.selectByGroupId(groupId);
        if(groupInfo== null || Objects.equals(userId, groupInfo.getGroupOwnerId())){
            logger.info("不允许群主自己退群 只能解散群");
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        //根据参数查询是否还在群聊中
        UserContact userContact = userContactService.getUserContactByUserIdAndContactId(userId, groupId);
        if(userContact == null || !Objects.equals(userContact.getStatus(), UserContactStatusEnum.FRIEND.getStatus())){
            logger.info("不在群聊中");
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        //查用户名
        UserInfo userInfo =  userInfoMapper.selectByUserId(userId);
        if(userInfo == null){
            logger.info("用户不存在");
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        //根据消息参数分别执行
        String sessionId = StringTools.getChatSessionId4Group(groupId);
        Date date = new Date();


        String messageContent = String.format(messageTypeEnum.getInitMessage(), userInfo.getNickName());

        //更新会话表
        ChatSession chatSession = new ChatSession();
        chatSession.setSessionId(sessionId);
        chatSession.setLastMessage(messageContent);
        chatSession.setLastReceiveTime(date.getTime());
        chatSessionMapper.updateBySessionId(chatSession, sessionId);

        //更新消息
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSessionId(sessionId);
        chatMessage.setSendTime(date.getTime());
        chatMessage.setContactType(UserContactTypeEnum.GROUP.getType());
        chatMessage.setMessageType(messageTypeEnum.getType());
        chatMessage.setContactId(groupId);
        chatMessage.setMessageContent(messageContent);
        chatMessage.setStatus(MessageStatusEnum.SENT.getType());
        chatMessageMapper.insert(chatMessage);

        //更新contactId状态
        UserContact updateUserContact = new UserContact();
        updateUserContact.setStatus(UserContactStatusEnum.DELETE.getStatus());
        updateUserContact.setLastUpdateTime(date);
        this.userContactMapper.updateByUserIdAndContactId(updateUserContact, userId, groupId);

        //更新memberCount
        UserContactQuery userContactQuery = new UserContactQuery();
        userContactQuery.setContactId(groupId);
        userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        Integer count = userContactMapper.selectCount(userContactQuery);

        MessageSendDto messageSendDto = CopyTools.copy(chatMessage, MessageSendDto.class);
        messageSendDto.setMemberCount(count);
        handlerMessage.sendMessage(messageSendDto);

        //移除群组通道
        channelContextUtil.removeFromGroupByUserId(groupId, userId);
        //移除redis缓存记录
        redisComponent.removeUserContact(userId, groupId);




    }


}