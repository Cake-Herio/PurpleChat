package com.purplechat.service.impl;

import com.purplechat.entity.dto.MessageSendDto;
import com.purplechat.entity.dto.TokenUserInfoDto;
import com.purplechat.entity.po.ChatSessionUser;
import com.purplechat.entity.po.UserContact;
import com.purplechat.entity.query.ChatSessionUserQuery;
import com.purplechat.entity.query.UserContactQuery;
import com.purplechat.entity.vo.PaginationResultVO;
import com.purplechat.entity.query.SimplePage;
import com.purplechat.enums.MessageTypeEnum;
import com.purplechat.enums.PageSize;
import com.purplechat.enums.UserContactStatusEnum;
import com.purplechat.enums.UserContactTypeEnum;
import com.purplechat.mappers.ChatSessionUserMapper;
import com.purplechat.mappers.UserContactMapper;
import com.purplechat.redis.RedisComponent;
import com.purplechat.service.ChatSessionUserService;
import com.purplechat.websocket.netty.HandlerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @Description:会话用户Service
 * @Author:文翔
 * @date:2024/07/28
 */
@Service("chatSessionUserService")
public class ChatSessionUserServiceImpl implements ChatSessionUserService {
    private static final Logger logger = LoggerFactory.getLogger(ChatSessionUserServiceImpl.class);
    @Resource
    private ChatSessionUserMapper<ChatSessionUser, ChatSessionUserQuery> chatSessionUserMapper;
    @Resource
    private UserContactMapper<UserContact, UserContactQuery> userContactMapper;
    @Resource
    private HandlerMessage handlerMessage;
    @Resource
    private RedisComponent redisComponent;


    /**
     * 根据条件查询列表
     */
    @Override
    public List<ChatSessionUser> findListByParam(ChatSessionUserQuery query) {
        return this.chatSessionUserMapper.selectList(query);
    }

    /**
     * 根据条件查询数量
     */
    @Override
    public Integer findCountByParam(ChatSessionUserQuery query) {
        return this.chatSessionUserMapper.selectCount(query);
    }

    /**
     * 分页查询
     */
    @Override
    public PaginationResultVO<ChatSessionUser> findListByPage(ChatSessionUserQuery query) {
        Integer count = this.findCountByParam(query);
        Integer pageSize = query.getPageNo() == null ? PageSize.SIZE15.getSize() : query.getPageSize();
        SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
        query.setSimplePage(page);
        List<ChatSessionUser> list = this.findListByParam(query);
        PaginationResultVO<ChatSessionUser> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(ChatSessionUser bean) {
        return this.chatSessionUserMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<ChatSessionUserQuery> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.chatSessionUserMapper.insertBatch(listBean);
    }

    /**
     * 批量新增/修改
     */
    @Override
    public Integer addOrUpdateBatch(List<ChatSessionUserQuery> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.chatSessionUserMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 根据UserIdAndContactId查询
     */
    public ChatSessionUser getChatSessionUserByUserIdAndContactId(String userId, String contactId) {
        return this.chatSessionUserMapper.selectByUserIdAndContactId(userId, contactId);
    }

    /**
     * 根据UserIdAndContactId修改
     */
    public Integer updateChatSessionUserByUserIdAndContactId(ChatSessionUser bean, String userId, String contactId) {
        return this.chatSessionUserMapper.updateByUserIdAndContactId(bean, userId, contactId);
    }

    /**
     * 根据UserIdAndContactId删除
     */
    public Integer deleteChatSessionUserByUserIdAndContactId(String userId, String contactId) {
        return this.chatSessionUserMapper.deleteByUserIdAndContactId(userId, contactId);
    }


    @Override
    public void updateNickName(String contactName, String contactId) {
        //首先关注chatSessionUser 因为这里面需要填入名称
        ChatSessionUser chatSessionUser = new ChatSessionUser();
        chatSessionUser.setContactName(contactName);
        ChatSessionUserQuery chatSessionUserQuery = new ChatSessionUserQuery();
        chatSessionUserQuery.setContactId(contactId);
        chatSessionUserMapper.updateByParam(chatSessionUser, chatSessionUserQuery);

        //群名变了只需要通过channelGroup发送消息即可,如果是个人昵称更换情况有所不同
        //先看群昵称更换逻辑
        UserContactTypeEnum userContactTypeEnum = UserContactTypeEnum.getByPrefix(contactId);
        if(userContactTypeEnum == null){
            logger.info("用户类型不存在");
            return;
        }

        if(userContactTypeEnum.equals(UserContactTypeEnum.GROUP)){
            MessageSendDto messageSendDto = new MessageSendDto();
            messageSendDto.setContactType(UserContactTypeEnum.GROUP.getType());
            messageSendDto.setContactId(contactId);
            messageSendDto.setSendTime(new Date().getTime());
            //额外信息为更新的群聊名称

            messageSendDto.setExtendData(contactName);
            messageSendDto.setMessageType(MessageTypeEnum.CONTACT_NAME_UPDATE.getType());
            //修改群昵称，发送ws消息
            handlerMessage.sendMessage(messageSendDto);
        }
        else if(userContactTypeEnum.equals(UserContactTypeEnum.USER)){
            //用户需要遍历加过他的所有好友 发给他们自己的昵称变动的信息 TODO 待测试P32
            UserContactQuery userContactQuery = new UserContactQuery();
            userContactQuery.setContactId(contactId);
            userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            List<UserContact> userContactList = userContactMapper.selectList(userContactQuery);
            for(UserContact userContact : userContactList){
                MessageSendDto messageSendDto = new MessageSendDto();
                messageSendDto.setContactType(UserContactTypeEnum.USER.getType());
                messageSendDto.setContactId(userContact.getUserId());
                messageSendDto.setSendTime(new Date().getTime());
                messageSendDto.setSendUserId(contactId);
                messageSendDto.setSendUserNickName(contactName);

                messageSendDto.setExtendData(contactName);
                messageSendDto.setMessageType(MessageTypeEnum.CONTACT_NAME_UPDATE.getType());
                //修改群昵称，发送ws消息
                handlerMessage.sendMessage(messageSendDto);


            }
        //还需要更新redis中tokenDto中的昵称的信息
            TokenUserInfoDto tokenUserInfoDtoById = redisComponent.getTokenUserInfoDtoById(contactId);
            if(Objects.nonNull(tokenUserInfoDtoById)){
                tokenUserInfoDtoById.setNickName(contactName);
                redisComponent.saveTokenUserInfoDto(tokenUserInfoDtoById);
            }
        }










    }
}