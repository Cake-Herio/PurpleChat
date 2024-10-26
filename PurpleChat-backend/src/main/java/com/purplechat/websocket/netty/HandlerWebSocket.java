package com.purplechat.websocket.netty;

import com.purplechat.entity.constants.Constants;
import com.purplechat.entity.dto.TokenUserInfoDto;
import com.purplechat.redis.RedisComponent;
import com.purplechat.redis.RedisUtils;
import com.purplechat.utils.JsonUtils;
import com.purplechat.websocket.ChannelContextUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;


@Component("handlerWebSocket")
@ChannelHandler.Sharable
public class HandlerWebSocket extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Resource
    private RedisUtils redisUtils;
    @Resource
    private RedisComponent redisComponent;
    @Resource
    ChannelContextUtil channelContextUtil;

    private static final Logger logger = LoggerFactory.getLogger(HandlerWebSocket.class);
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        channelContextUtil.removeContext(ctx.channel());
        logger.info("连接断开");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("连接建立");
    }


    //处理具体事务逻辑
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame textWebSocketFrame) throws Exception {

        Channel channel = ctx.channel();
        //根据channelId获取userId
        Attribute<String> attribute = channel.attr(AttributeKey.valueOf(channel.id().toString()));
        String userId = attribute.get();
        logger.info("收到用户{}消息： {}", userId, textWebSocketFrame.text());
        redisComponent.saveUserHeart(userId);

    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof WebSocketServerProtocolHandler.HandshakeComplete){ //表示传进来的事件已经握手完成
            WebSocketServerProtocolHandler.HandshakeComplete complete = (WebSocketServerProtocolHandler.HandshakeComplete) evt;
            //拿url
            String url = complete.requestUri();
            logger.info("url: {}", url);
            //拿其中的token
            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(url);  // 直接用编写好的解析参数的函数
            Map<String, List<String>> parameters = queryStringDecoder.parameters();
            if(!parameters.containsKey("token")){
                ctx.close();
            }
            String token = parameters.get("token").get(0);
            logger.info("token: {}", token);
            //校验token redis中
            TokenUserInfoDto userInfoDto = (TokenUserInfoDto) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN + token);
            ctx.writeAndFlush(JsonUtils.convertObj2Json(userInfoDto));
            if(userInfoDto == null) {
                ctx.close();
            }
            if(null == userInfoDto.getUserId() ){
                logger.error("userId不存在");
            }

            //对新进来的channel建立映射
            channelContextUtil.addContext(userInfoDto.getUserId(), ctx.channel());

        }



    }
}
