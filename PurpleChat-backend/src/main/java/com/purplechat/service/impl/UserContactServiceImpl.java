package com.purplechat.service.impl;

import com.purplechat.entity.dto.MessageSendDto;
import com.purplechat.entity.dto.SysSettingDto;
import com.purplechat.entity.dto.TokenUserInfoDto;
import com.purplechat.entity.dto.UserContactSearchResultDto;
import com.purplechat.entity.po.*;
import com.purplechat.entity.query.*;
import com.purplechat.entity.vo.PaginationResultVO;
import com.purplechat.enums.*;
import com.purplechat.exception.BusinessException;
import com.purplechat.mappers.*;
import com.purplechat.redis.RedisComponent;
import com.purplechat.service.UserContactService;
import com.purplechat.utils.CopyTools;
import com.purplechat.utils.StringTools;
import com.purplechat.websocket.ChannelContextUtil;
import com.purplechat.websocket.netty.HandlerMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @Description:联系人Service
 * @Author:文翔
 * @date:2024/07/16
 */
@Service("userContactService")
public class UserContactServiceImpl implements UserContactService {

    @Resource
    private UserContactMapper<UserContact, UserContactQuery> userContactMapper;
    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;
    @Resource
    private GroupInfoMapper<GroupInfo, GroupInfoQuery> groupInfoMapper;
    @Resource
    private UserContactApplyMapper<UserContactApply, UserContactApplyQuery> userContactApplyMapper;
    @Resource
    private RedisComponent redisComponent;
    @Resource
    private ChatSessionMapper<ChatSession, ChatSessionQuery> chatSessionMapper;
    @Resource
    private ChatSessionUserMapper<ChatSessionUser, ChatSessionUserQuery> chatSessionUserMapper;
    @Resource
    private ChatMessageMapper<ChatMessage, ChatMessageQuery> chatMessageMapper;
    @Resource
    private HandlerMessage handlerMessage;
    @Resource
    private ChannelContextUtil channelContextUtil;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<UserContact> findListByParam(UserContactQuery query) {
        return this.userContactMapper.selectList(query);
    }

    /**
     * 根据条件查询数量
     */
    @Override
    public Integer findCountByParam(UserContactQuery query) {
        return this.userContactMapper.selectCount(query);
    }

    /**
     * 分页查询
     */
    @Override
    public PaginationResultVO<UserContact> findListByPage(UserContactQuery query) {
        Integer count = this.findCountByParam(query);
        Integer pageSize = query.getPageNo() == null ? PageSize.SIZE15.getSize() : query.getPageSize();
        SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
        query.setSimplePage(page);
        List<UserContact> list = this.findListByParam(query);
        PaginationResultVO<UserContact> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(UserContact bean) {
        return this.userContactMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<UserContactQuery> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userContactMapper.insertBatch(listBean);
    }

    /**
     * 批量新增/修改
     */
    @Override
    public Integer addOrUpdateBatch(List<UserContactQuery> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userContactMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 根据UserIdAndContactId查询
     */
    public UserContact getUserContactByUserIdAndContactId(String userId, String contactId) {
        return this.userContactMapper.selectByUserIdAndContactId(userId, contactId);
    }

    /**
     * 根据UserIdAndContactId修改
     */
    public Integer updateUserContactByUserIdAndContactId(UserContact bean, String userId, String contactId) {
        return this.userContactMapper.updateByUserIdAndContactId(bean, userId, contactId);
    }

    /**
     * 根据UserIdAndContactId删除
     */
    public Integer deleteUserContactByUserIdAndContactId(String userId, String contactId) {
        return this.userContactMapper.deleteByUserIdAndContactId(userId, contactId);
    }

    @Override
    public UserContactSearchResultDto searchUserContact(String userId, String contactId) {
        UserContactTypeEnum typeEnum = UserContactTypeEnum.getByPrefix(contactId);
        UserContactSearchResultDto userContactSearchResultDto = new UserContactSearchResultDto();
        switch (typeEnum) {
            case USER:
                UserInfo userInfo = userInfoMapper.selectByUserId(contactId);
                if (userInfo == null) {
                    return null;
                }
                userContactSearchResultDto = CopyTools.copy(userInfo, UserContactSearchResultDto.class);
                break;

            case GROUP:
                GroupInfo groupInfo = groupInfoMapper.selectByGroupId(contactId);
                if (groupInfo == null) {
                    return null;
                }
                userContactSearchResultDto.setNickName(groupInfo.getGroupName());
                userContactSearchResultDto.setJoinType(groupInfo.getJoinType());
                break;

            default:
                break;
        }

        userContactSearchResultDto.setContactType(typeEnum.toString());
        userContactSearchResultDto.setContactId(contactId);

        if (contactId.equals(userId)) {
            userContactSearchResultDto.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            return userContactSearchResultDto;
        }

        UserContact userContact = this.userContactMapper.selectByUserIdAndContactId(userId, contactId);
        userContactSearchResultDto.setStatus(userContact == null ? null : userContact.getStatus());
        return userContactSearchResultDto;


    }


    /**
     *
     * 申请好友 服务器可能集群 发送消息需要借用redisson
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer applyAdd(TokenUserInfoDto userInfo, String contactId, String applyInfo, Integer contactType) throws BusinessException {
        Integer joinType = null;
        String receiveUserId = null;

        UserContact userContact = this.userContactMapper.selectByUserIdAndContactId(userInfo.getUserId(),contactId);
        //先查看是否被拉黑
        if (userContact != null && (Objects.equals(userContact.getStatus(), UserContactStatusEnum.BLACKLIST_BE.getStatus()) ||
                Objects.equals(userContact.getStatus(), UserContactStatusEnum.BLACKLIST_BE_FIRST.getStatus()))) {
            throw new BusinessException("对方已经将你拉黑，无法添加");
        }

        if (UserContactTypeEnum.GROUP.getType().equals(contactType)) {
            GroupInfo groupInfo = groupInfoMapper.selectByGroupId(contactId);
            if (groupInfo == null || GroupStatusEnum.ABNORMAL.getStatus().equals(groupInfo.getStatus())) {
                throw new BusinessException("群组不存在");
            }
            joinType = groupInfo.getJoinType();
            receiveUserId = groupInfo.getGroupOwnerId();
        } else if (UserContactTypeEnum.USER.getType().equals(contactType)) {
            UserInfo receiveUserInfo = userInfoMapper.selectByUserId(contactId);
            if (receiveUserInfo == null) {
                throw new BusinessException("用户不存在");
            }
            joinType = receiveUserInfo.getJoinType();
            receiveUserId = contactId;
        }

        //直接
        if (JoinTypeEnum.DIRECT.getType().equals(joinType)) {
            this.addContact(userInfo.getUserId(), receiveUserId, contactId, contactType, applyInfo);
            return joinType;
        }

        //间接 先查是否之前添加过
        Date curTime = new Date();
        UserContactApply dbApply = this.userContactApplyMapper.selectByApplyUserIdAndReceiveUserIdAndContactId(userInfo.getUserId(), receiveUserId, contactId);

        if (null == dbApply) {
            UserContactApply userContactApply = new UserContactApply();
            userContactApply.setApplyUserId(userInfo.getUserId());
            userContactApply.setReceiveUserId(receiveUserId);
            userContactApply.setContactId(contactId);
            userContactApply.setContactType(contactType);
            userContactApply.setApplyInfo(applyInfo);
            userContactApply.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
            userContactApply.setLastApplyTime(curTime.getTime());

            this.userContactApplyMapper.insert(userContactApply);
        } else {
            UserContactApply userContactApply = new UserContactApply();
            userContactApply.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
            userContactApply.setLastApplyTime(curTime.getTime());
            userContactApply.setApplyInfo(applyInfo);
            this.userContactApplyMapper.updateByApplyId(userContactApply, dbApply.getApplyId()); //applyId是唯一索引 保证只有一条记录
        }

        //TODO 理解本处逻辑 发送ws消息
        if (dbApply == null || !UserContactApplyStatusEnum.INIT.getStatus().equals(dbApply.getStatus())) {
            //集群化部署 发送消息
            MessageSendDto messageSendDto = new MessageSendDto();
            messageSendDto.setMessageType(MessageTypeEnum.CONTACT_APPLY.getType());
            messageSendDto.setContactId(receiveUserId);
            messageSendDto.setMessageContent(applyInfo);

            handlerMessage.sendMessage(messageSendDto);
        }
        return joinType;
    }


    @Transactional(rollbackFor = Exception.class)
    public void addContact(String applyUserId, String receiveUserId, String contactId, Integer contactType, String applyInfo) throws BusinessException {
        //如果是群聊 receiveUserId是群主 而contactId是群号
        //如果是加群 判断群是否满员
        if (UserContactTypeEnum.GROUP.getType().equals(contactType)) {
            UserContactQuery userContactQuery = new UserContactQuery();
            userContactQuery.setContactId(contactId);
            userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            Integer count = this.userContactMapper.selectCount(userContactQuery);
            SysSettingDto sysSetting = redisComponent.getSysSetting();
            if (count >= sysSetting.getMaxGroupCount()) {
                throw new BusinessException("群组已满员");
            }


        }
        Date curDate = new Date();
        List<UserContact> userContactList = new ArrayList<>();
        //申请方添加联系人
        UserContact userContact = new UserContact();
        userContact.setUserId(applyUserId);
        userContact.setContactId(contactId);
        userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        userContact.setCreateTime(curDate);
        userContact.setLastUpdateTime(curDate);
        userContact.setContactType(contactType);
        userContactList.add(userContact);

        //接受申请方需要判断 如果对方是申请加入群聊则不添加
        if (UserContactTypeEnum.USER.getType().equals(contactType)) {
            userContact = new UserContact();
            userContact.setUserId(receiveUserId);
            userContact.setContactId(applyUserId);
            userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            userContact.setCreateTime(curDate);
            userContact.setLastUpdateTime(curDate);
            userContact.setContactType(contactType);
            userContactList.add(userContact);
        }

        //批量插入 效率更高
        userContactMapper.insertOrUpdateBatch(userContactList);


        //分好友和群组 添加于redis中
        redisComponent.addUserContact(applyUserId, contactId);
        if(UserContactTypeEnum.USER.getType().equals(contactType)){
            redisComponent.addUserContact(contactId, applyUserId);
            //创建会话
            ChatSession chatSession = new ChatSession();
            String sessionId = StringTools.getChatSessionId4User(new String[]{applyUserId, contactId});
            chatSession.setSessionId(sessionId);
            chatSession.setLastReceiveTime(curDate.getTime());
            chatSession.setLastMessage(applyInfo);


            chatSessionMapper.insertOrUpdate(chatSession);




            //创建session
            List<ChatSessionUser> chatSessionUserList = new ArrayList<>();
            //有两份
            ChatSessionUser chatSessionUser1 = new ChatSessionUser();
            chatSessionUser1.setSessionId(sessionId);
            chatSessionUser1.setUserId(applyUserId);
            chatSessionUser1.setContactId(contactId);
            UserInfo userInfo1 = this.userInfoMapper.selectByUserId(contactId);
            chatSessionUser1.setContactName(userInfo1.getNickName());
            chatSessionUser1.setContactType(UserContactTypeEnum.USER.getType());
            chatSessionUserList.add(chatSessionUser1);


            ChatSessionUser chatSessionUser2 = new ChatSessionUser();
            chatSessionUser2.setSessionId(sessionId);
            chatSessionUser2.setUserId(contactId);
            chatSessionUser2.setContactId(applyUserId);
            UserInfo userInfo2 = this.userInfoMapper.selectByUserId(applyUserId);
            chatSessionUser2.setContactName(userInfo2.getNickName());
            chatSessionUser2.setContactType(UserContactTypeEnum.USER.getType());
            chatSessionUserList.add(chatSessionUser2);

            chatSessionUserMapper.insertOrUpdateBatch(chatSessionUserList);




            //首先申请人发送给接收人
            ChatMessage chatMessage1 = new ChatMessage();
            chatMessage1.setSessionId(sessionId);
            chatMessage1.setMessageType(MessageTypeEnum.ADD_FRIEND.getType());
            chatMessage1.setMessageContent(applyInfo);

            chatMessage1.setSendUserId(applyUserId);
            chatMessage1.setContactId(contactId);
            chatMessage1.setContactType(UserContactTypeEnum.USER.getType());
            chatMessage1.setSendTime(curDate.getTime());
            chatMessage1.setSendUserNickName(userInfo2.getNickName());
            chatMessageMapper.insert(chatMessage1);


            //转Dto
            MessageSendDto messageSendDto = CopyTools.copy(chatMessage1, MessageSendDto.class);
            handlerMessage.sendMessage(messageSendDto);


            //其次接收人发送给申请人打招呼 同时也需要记录到表中
            ChatMessage chatMessage2 = new ChatMessage();
            chatMessage2.setSessionId(sessionId);
            chatMessage2.setMessageType(MessageTypeEnum.ADD_FRIEND_SELF.getType());
            chatMessage2.setMessageContent(MessageTypeEnum.ADD_FRIEND_SELF.getInitMessage());
            chatMessage2.setSendUserId(contactId);
            chatMessage2.setContactId(applyUserId);
            chatMessage2.setContactType(UserContactTypeEnum.USER.getType());
            chatMessage2.setSendTime(curDate.getTime());
            chatMessage2.setSendUserNickName(userInfo1.getNickName());
            chatMessageMapper.insert(chatMessage2);
            MessageSendDto messageSendDto1 = CopyTools.copy(chatMessage2, MessageSendDto.class);


            messageSendDto.setMessageType(MessageTypeEnum.ADD_FRIEND_SELF.getType());
            messageSendDto.setSendUserId(contactId);
            messageSendDto.setContactId(applyUserId);
            messageSendDto.setContactName(userInfo2.getNickName());
            messageSendDto.setMessageContent(MessageTypeEnum.ADD_FRIEND_SELF.getInitMessage());
            handlerMessage.sendMessage(messageSendDto1);
        }else{
            //群组修改会话

            String sessionId = StringTools.getChatSessionId4Group(contactId);
            ChatSessionUser chatSessionUser = new ChatSessionUser();
            chatSessionUser.setSessionId(sessionId);
            chatSessionUser.setUserId(applyUserId);
            chatSessionUser.setContactId(contactId);
            GroupInfo groupInfo = this.groupInfoMapper.selectByGroupId(contactId);
            chatSessionUser.setContactName(groupInfo.getGroupName());
            chatSessionUser.setContactType(UserContactTypeEnum.GROUP.getType());
            ChatSessionUserQuery chatSessionUserQuery = new ChatSessionUserQuery();
            chatSessionUserQuery.setSessionId(sessionId);
            chatSessionUserMapper.updateByParam(chatSessionUser, chatSessionUserQuery);

            //修改session信息
            //这里需要需要String.format 因为对于别人 你进入群聊需要展示你的昵称
            UserInfo applyUserInfo = userInfoMapper.selectByUserId(applyUserId);
            String message = String.format(MessageTypeEnum.ADD_GROUP.getInitMessage(), applyUserInfo.getNickName());
            //创建会话
            ChatSession addGroupChatSession = new ChatSession();
            addGroupChatSession.setSessionId(sessionId);
            addGroupChatSession.setLastMessage(message);
            addGroupChatSession.setLastReceiveTime(curDate.getTime());
            chatSessionMapper.updateBySessionId(addGroupChatSession, sessionId);

            //添加聊天信息
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSessionId(sessionId);
            chatMessage.setMessageType(MessageTypeEnum.ADD_GROUP.getType());
            chatMessage.setMessageContent(message);
            chatMessage.setSendTime(curDate.getTime());
            chatMessage.setContactId(contactId);
            chatMessage.setContactType(UserContactTypeEnum.GROUP.getType());
            chatMessage.setStatus(MessageStatusEnum.SENT.getType());
            chatMessageMapper.insert(chatMessage);






            //添加群组到redis
            redisComponent.addUserContact(applyUserId, contactId);

            //添加群组通道
            channelContextUtil.add2GroupByUserId(contactId, applyUserId);

            //发送消息
            MessageSendDto messageSendDto = CopyTools.copy(chatMessage, MessageSendDto.class);
            messageSendDto.setContactId(contactId);
            //TODO 这里整体与创建群聊逻辑相似 可以抽成一个方法
            //获取群的数量
            UserContactQuery userContactQuery = new UserContactQuery();
            userContactQuery.setContactId(groupInfo.getGroupId());
            userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            Integer memberCount = this.userContactMapper.selectCount(userContactQuery);

            messageSendDto.setMemberCount(memberCount);
            messageSendDto.setContactName(groupInfo.getGroupName());
            //实现发消息
            handlerMessage.sendMessage(messageSendDto);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addContact4Robot(String userId) {
        Date date = new Date();
        SysSettingDto sysSetting = redisComponent.getSysSetting();
        String robotUid = sysSetting.getRobotUid();
        String robotNickName = sysSetting.getRobotNickName();
        String robotWelcome = sysSetting.getRobotWelcome();
        //防html注入
        robotWelcome = StringTools.cleanHtmlTag(robotWelcome);
        //添加好友
        UserContact userContact = new UserContact();
        userContact.setUserId(userId);
        userContact.setContactId(robotUid);
        userContact.setContactName(robotNickName);
        userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        userContact.setCreateTime(date);
        userContact.setLastUpdateTime(date);
        userContact.setContactType(UserContactTypeEnum.USER.getType());

        userContactMapper.insert(userContact);
        //增加会话id

        String sessionId = StringTools.getChatSessionId4User(new String[]{userId, robotUid});
        ChatSession chatSession = new ChatSession();
        chatSession.setSessionId(sessionId);
        chatSession.setLastMessage(robotWelcome);
        chatSession.setLastReceiveTime(date.getTime());
        chatSessionMapper.insert(chatSession);

        //增加联系人信息
        ChatSessionUser chatSessionUser = new ChatSessionUser();
        chatSessionUser.setUserId(userId);
        chatSessionUser.setContactId(robotUid);
        chatSessionUser.setContactName(robotNickName);
        chatSessionUser.setSessionId(sessionId);
        chatSessionUserMapper.insert(chatSessionUser);

        //增加聊天信息
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSessionId(sessionId);
        chatMessage.setMessageType(MessageTypeEnum.CHAT.getType());
        chatMessage.setMessageContent(robotWelcome);
        chatMessage.setSendUserNickName(robotNickName);
        chatMessage.setSendTime(date.getTime());
        //这里是机器人给我发消息 所以发送方是机器人
        chatMessage.setSendUserId(robotUid);
        chatMessage.setContactId(userId);
        chatMessage.setContactType(UserContactTypeEnum.USER.getType());
        chatMessage.setStatus(MessageStatusEnum.SENT.getType());

        chatMessageMapper.insert(chatMessage);
    }

    @Override
    @Transactional
    public void removeUserContact(String userId, String contactId, UserContactStatusEnum statusEnum) throws BusinessException {

        //先判断是否存在
        UserContact dbUser = userContactMapper.selectByUserIdAndContactId(userId, contactId);
        if (dbUser == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }


        UserContact userContact = new UserContact();
        userContact.setStatus(statusEnum.getStatus());
        userContactMapper.updateByUserIdAndContactId(userContact, userId, contactId);

        //如果是删除好友 移除自己的会话信息
        if(contactId.startsWith(UserContactTypeEnum.USER.getPrefix())) {
            chatSessionUserMapper.deleteByUserIdAndContactId(userId, contactId);
        }



        //对方同样需要将自己移除
        //被删除或被拉黑
        UserContact friendContact = new UserContact();
        if (UserContactStatusEnum.DELETE.equals(statusEnum)) {
            friendContact.setStatus(UserContactStatusEnum.DELETE_BE.getStatus());
        } else if (UserContactStatusEnum.BLACKLIST.equals(statusEnum)) {
            friendContact.setStatus(UserContactStatusEnum.BLACKLIST_BE.getStatus());
        }
        userContactMapper.updateByUserIdAndContactId(friendContact, contactId, userId);

        //redis中 从我的好友列表缓存中删除好友
        redisComponent.removeUserContact(userId, contactId);
        //对方也要删除我
        redisComponent.removeUserContact(contactId, userId);
        //移除


    }




}