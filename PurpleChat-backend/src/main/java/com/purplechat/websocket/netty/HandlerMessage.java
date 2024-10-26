package com.purplechat.websocket.netty;


import com.purplechat.entity.dto.MessageSendDto;
import com.purplechat.utils.JsonUtils;
import com.purplechat.websocket.ChannelContextUtil;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Component("handlerMessage")
public class HandlerMessage {

    private static final Logger logger = LoggerFactory.getLogger(HandlerMessage.class);

    private static final String MESSAGE_TOPIC = "message_topic";
    @Resource
    RedissonClient redissonClient;

    @Resource
    private ChannelContextUtil channelContextUtil;


    //建立监听
    @PostConstruct //启动监听
    public void lisMessage(){
        logger.info("初始化消息监听器");
        RTopic rTopic = redissonClient.getTopic(MESSAGE_TOPIC);
        rTopic.addListener(MessageSendDto.class, (MessageSendDto, sendDTO) -> {
            logger.info("接收到消息：{}", JsonUtils.convertObj2Json(sendDTO));
            //监听时接收消息 并分发给所以集群服务器
            channelContextUtil.sendMessage(sendDTO);
        });


    }

    public void sendMessage(MessageSendDto sendDto){
        RTopic rtopic = redissonClient.getTopic(MESSAGE_TOPIC);
        rtopic.publish(sendDto);
        //这样操作后redis会监听到这个事件 自动将消息分发给所有监听的服务器
    }


}
