package com.purplechat.websocket;
import com.purplechat.entity.constants.Constants;
import com.purplechat.entity.dto.MessageSendDto;
import com.purplechat.entity.dto.WSInitData;
import com.purplechat.entity.po.ChatMessage;
import com.purplechat.entity.po.ChatSessionUser;
import com.purplechat.entity.po.UserContactApply;
import com.purplechat.entity.po.UserInfo;
import com.purplechat.entity.query.*;
import com.purplechat.enums.MessageTypeEnum;
import com.purplechat.enums.UserContactStatusEnum;
import com.purplechat.enums.UserContactTypeEnum;
import com.purplechat.mappers.*;
import com.purplechat.redis.RedisComponent;
import com.purplechat.utils.JsonUtils;
import com.purplechat.utils.StringTools;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component("channelContextUtil")
public class ChannelContextUtil {
    private static final Logger logger = LoggerFactory.getLogger(ChannelContextUtil.class);

    private static final ConcurrentHashMap<String, Channel> USER_CONTEXT_MAP = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, ChannelGroup> GROUP_CONTEXT_MAP = new ConcurrentHashMap<>();
    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;
    @Resource
    private ChatSessionUserMapper<ChatSessionUser, ChatSessionQuery> chatSessionUserMapper;

    @Resource
    private ChatMessageMapper<ChatMessage, ChatMessageQuery> chatMessageMapper;
    @Resource
    private UserContactApplyMapper<UserContactApply, UserContactApplyQuery> userContactApplyMapper;

    @Resource
    private RedisComponent redisComponent;



    //建立channel与userId的映射
    public void addContext(String userId, Channel channel){
        //channel id 为key
        String channelId = channel.id().toString();
        AttributeKey attributeKey = null;
        if(!AttributeKey.exists(channelId)){//根据是否存在键值为channelId的实例
            attributeKey = AttributeKey.newInstance(channelId);
        } else{
            attributeKey = AttributeKey.valueOf(channelId);
        }

        logger.info("channelId是{}, UseId是{}", channelId, userId);
        channel.attr(attributeKey).set(userId);

        USER_CONTEXT_MAP.put(userId, channel);

        //立刻发心跳
        redisComponent.saveUserHeart(userId);

        //从redis中查找联系人
        List<String> contactIdList = redisComponent.getUserContactList(userId);
        //从里面搜是群聊的,并依次加入群聊
        for(String groupId : contactIdList){
            if(groupId.startsWith(UserContactTypeEnum.GROUP.getPrefix())){
                add2Group(groupId, channel);
            }
        }

        //记录最新连接时间 我们认为每次登录微信就会立刻发送ws请求到netty并创建一个channel 并且会通过不断发送心跳包来保持激活状态 当无法收到后则自动断开连接 并认为用户下线

        UserInfo userInfo = new UserInfo();
        userInfo.setLastLoginTime(new Date());
        userInfoMapper.updateByUserId(userInfo, userId);

        //给用户发消息  发送的消息时间段应当是用户最后离线时间到最新登录时间 也就是下线这部分时间
        UserInfo info = userInfoMapper.selectByUserId(userId);
        Long lastOffTime = info.getLastOffTime();
        //最晚不超过三天
        if(lastOffTime != null&&System.currentTimeMillis() - Constants.MillisSECOND_3DAY > lastOffTime){
            lastOffTime = System.currentTimeMillis() - Constants.MillisSECOND_3DAY;
        }
        //TODO 从数据库中查找会话消息
        ChatSessionUserQuery chatSessionUserQuery = new ChatSessionUserQuery();
        chatSessionUserQuery.setOrderBy("last_receive_time desc");
        chatSessionUserQuery.setUserId(userId);
        List<ChatSessionUser> chatSessionUserList = chatSessionUserMapper.selectList(chatSessionUserQuery);

        WSInitData wsInitData = new WSInitData();
        wsInitData.setChatSessionUsers(chatSessionUserList);

        //初始化
        /**
         * 查询聊天消息
         */
        ChatMessageQuery chatMessageQuery = new ChatMessageQuery();
        //流遍历得到所有群组的IdList
        List<String> queryIdList = contactIdList.stream().filter(groupId -> groupId.startsWith(UserContactTypeEnum.GROUP.getPrefix())).collect(Collectors.toList());
        //再加上自己
        queryIdList.add(userId);
        chatMessageQuery.setContactIdList(queryIdList);
        //只查询最晚三天前的信息
        chatMessageQuery.setLastReceiveTime(lastOffTime);
        List<ChatMessage> chatMessagesList = chatMessageMapper.selectList(chatMessageQuery);


        wsInitData.setChatMessageList(chatMessagesList);


        /**
         * 查询好友申请
         */
        UserContactApplyQuery userContactApplyQuery = new UserContactApplyQuery();
        userContactApplyQuery.setContactId(userId);
        userContactApplyQuery.setStatus(UserContactStatusEnum.NO_FRIEND.getStatus());
        //只查询离线时间的 因为离线无法接受到好友申请 因此登陆时需要进行同步
        userContactApplyQuery.setLastApplyTimeStamp(lastOffTime);

        Integer count = userContactApplyMapper.selectCount(userContactApplyQuery);


        wsInitData.setApplyCount(count);


        //发送消息 到前端
        MessageSendDto messageSendDto = new MessageSendDto();
        messageSendDto.setMessageType(MessageTypeEnum.INIT.getType());
        messageSendDto.setContactId(userId);
        messageSendDto.setExtendData(wsInitData); //拓展信息就是会话信息 用于展示左边的聊天列表

        sendMessage(messageSendDto); //这里的接收者就是自己 因为是初始化消息

    }





    //封装发送消息函数 需要传接收者的id 以获取对应的channel，并完成发送
    public void sendMessage(MessageSendDto messageSendDto){
        //判断群聊或单聊
        UserContactTypeEnum typeEnum = UserContactTypeEnum.getByPrefix(messageSendDto.getContactId());
        switch (typeEnum) {
            case USER:
                send2User(messageSendDto);
                break;
            case GROUP:
                send2Group(messageSendDto);
                break;

        }

    }


    private void send2User (MessageSendDto messageSendDto){
        String receiveId = messageSendDto.getContactId();
        if(null == receiveId){
            return;
        }

        //查询redis是否存在该用户 type 为 2 3 5 时
        Integer messageType = messageSendDto.getMessageType();
        if(messageType == 2 || messageType == 3 || messageType == 5){
            if(!redisComponent.getUserContactList(messageSendDto.getSendUserId()).contains(receiveId)){
                logger.info("用户不存在");
                return;
            }
        }


        Channel channel = USER_CONTEXT_MAP.get(receiveId);
        logger.info("将发送给{}， channel为{}", receiveId, channel);

        if(channel == null){
            return;
        }
//        if( MessageTypeEnum.ADD_FRIEND.getType().equals(messageSendDto.getMessageType())){
//            UserInfo userInfo = (UserInfo) messageSendDto.getExtendData();
//            messageSendDto.setMessageType(MessageTypeEnum.ADD_FRIEND.getType());
//            messageSendDto.setContactId(userInfo.getUserId());
//            messageSendDto.setExtendData(null);
//        }else{
//            messageSendDto.setContactId(messageSendDto.getSendUserId());
//            messageSendDto.setContactName(messageSendDto.getSendUserNickName());
//        }

        messageSendDto.setContactId(messageSendDto.getSendUserId());
        messageSendDto.setContactName(messageSendDto.getSendUserNickName());

        //发送
        channel.writeAndFlush(new TextWebSocketFrame(JsonUtils.convertObj2Json(messageSendDto)));


        //强制下线消息 发完后需要强制下线
        if(MessageTypeEnum.FORCE_OFF_LINE.getType().equals(messageSendDto.getMessageType())){
            channel.close();
        }

    }

    private void send2Group (MessageSendDto messageSendDto){
        if(StringTools.isEmpty(messageSendDto.getContactId())){
            logger.info("群聊id为空");
            return;
        }

        //在用户的redis中查找是否存在该channel
        Integer messageType = messageSendDto.getMessageType();

     //null说明是系统主动发的消息
        if(messageSendDto.getSendUserId() != null && (messageType == 2 || messageType == 3 || messageType == 5)) {
            List<String> list = redisComponent.getUserContactList(messageSendDto.getSendUserId());
            if (list == null || !list.contains(messageSendDto.getContactId())) {
                logger.info("已不是好友 无法发送消息");
                return;
            }
        }


        ChannelGroup channelGroup = GROUP_CONTEXT_MAP.get(messageSendDto.getContactId());
        if(null == channelGroup) {
            logger.info("map中未存储群聊信息");
            return;
        }






        logger.info("channelGroup为{}", channelGroup);
        //发送
        channelGroup.writeAndFlush(new TextWebSocketFrame(JsonUtils.convertObj2Json(messageSendDto)));

        //移除群聊 需要断开用户与群聊的连接
        MessageTypeEnum messageTypeEnum = MessageTypeEnum.getByType(messageSendDto.getMessageType());
        if(MessageTypeEnum.LEAVE_GROUP == messageTypeEnum || MessageTypeEnum.REMOVE_GROUP == messageTypeEnum){

            String userId = (String)messageSendDto.getExtendData();
            if(userId == null){
                logger.info("未查找到userId");
                return;
            }

            redisComponent.removeUserContact(userId, messageSendDto.getContactId());
            Channel channel = USER_CONTEXT_MAP.get(userId);
            if(channel == null){
                return;
            }
            channelGroup.remove(channel);
        }

        //如果是解散群聊 则将这个群的channel通道直接关闭
        if(MessageTypeEnum.DISSOLUTION_GROUP == messageTypeEnum){
            GROUP_CONTEXT_MAP.remove(messageSendDto.getContactId());
            channelGroup.clear();
        }
    }




    public void removeContext(Channel channel){
        //先通过channel拿到userId，然后在map中移除
        Attribute<String> attribute = channel.attr(AttributeKey.valueOf(channel.id().toString()));
        String userId = attribute.get();
        if(!StringTools.isEmpty(userId)){
            USER_CONTEXT_MAP.remove(userId);
        }
        //心跳断开 redis删除
        redisComponent.removeUserHeart(userId);
        //无需移除
//        redisComponent.removeTokenUserInfoDto(userId);


        //更新用户最后离线时间
        UserInfo userInfo = new UserInfo();
        userInfo.setLastOffTime(System.currentTimeMillis());
        userInfoMapper.updateByUserId(userInfo,userId);
    }

    public void removeContextById(String userId){
        if(!StringTools.isEmpty(userId)){
            USER_CONTEXT_MAP.remove(userId);
        }
        //心跳断开 redis删除
        redisComponent.removeUserHeart(userId);



        //更新用户最后离线时间
        UserInfo userInfo = new UserInfo();
        userInfo.setLastOffTime(System.currentTimeMillis());
        userInfoMapper.updateByUserId(userInfo,userId);
    }

    public void add2GroupByUserId(String groupId, String userId){
        Channel channel = USER_CONTEXT_MAP.get(userId);
        if(channel == null){
            return;
        }
        add2Group(groupId, channel);

    }




    public void add2Group(String groupId, Channel channel){
        ChannelGroup channelGroup = GROUP_CONTEXT_MAP.get(groupId);
        if(channelGroup == null){
            channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            GROUP_CONTEXT_MAP.put(groupId, channelGroup);
        }
        channelGroup.add(channel);
    }

    public void removeFromGroup(String groupId, Channel channel){
        ChannelGroup channelGroup = GROUP_CONTEXT_MAP.get(groupId);
        if(channelGroup == null){
            logger.error("groupId不存在");
        }
        channelGroup.remove(channel);
    }

    public void removeFromGroupByUserId(String groupId, String userId){
        Channel channel = USER_CONTEXT_MAP.get(userId);
        removeFromGroup(groupId, channel);
    }



}
