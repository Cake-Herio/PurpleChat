package com.purplechat.websocket.netty;

import com.purplechat.entity.config.AppConfig;
import com.purplechat.utils.StringTools;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component("nettyWebSocketStarter")
public class NettyWebSocketStarter implements Runnable {

    //定义两个Group 一个Boss 和 work boss负责控制 work负责具体业务
    private static EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private static EventLoopGroup workGroup = new NioEventLoopGroup();

    private static final Logger logger = LoggerFactory.getLogger(NettyWebSocketStarter.class);

    @Resource
    private HandlerWebSocket handlerWebSocket;

    @PreDestroy
    public void close(){
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
    }

    @Resource
    AppConfig appConfig;


    @Override
    public void run() {
        try {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            // 使用不安全的 SSL 上下文，忽略 SSL 验证
            SslContext sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey())
                    .trustManager(InsecureTrustManagerFactory.INSTANCE) // 允许不安全的 SSL 连接
                    .build();

            //构造启动器
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new ChannelInitializer() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();
                            pipeline.addLast(sslCtx.newHandler(channel.alloc()));   //忽略证书
                            //添加对http协议支持的编码器和解码器c
                            pipeline.addLast(new HttpServerCodec());

                            //聚合解码
                            //保证接受的http请求的完整性
                            pipeline.addLast(new HttpObjectAggregator(64 * 1024));

//

                            //添加处理器 用于监测心跳 即是否有心跳 检测是否超时 若超时则触发相应函数
                            pipeline.addLast(new IdleStateHandler(6, 0, 0, TimeUnit.SECONDS));
                            pipeline.addLast(new HandlerHeartBeat());
                            //将会http协议升级为ws协议 对websocket支持
                            pipeline.addLast(new WebSocketServerProtocolHandler("/ws", null, true, 65536,true,true));
                            pipeline.addLast(handlerWebSocket);
                        }
                    });
            //创建监听
            //TODO 创造集群 这里端口号应该不同
            Integer wsPort = appConfig.getWsPort();
            String wsPortStr = System.getProperty("ws.port");
            if(!StringTools.isEmpty(wsPortStr)){
                wsPort = Integer.parseInt(wsPortStr);
            }

            ChannelFuture sync = serverBootstrap.bind(appConfig.getWsPort()).sync();
            logger.info("netty启动成功, 端口号为：{}", appConfig.getWsPort());

            sync.channel().closeFuture().sync();

        }catch (Exception e){
            logger.error("启动netty失败", e);
        }finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
}
