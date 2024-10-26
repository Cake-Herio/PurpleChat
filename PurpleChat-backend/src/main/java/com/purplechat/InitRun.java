package com.purplechat;

import com.purplechat.redis.RedisUtils;
import com.purplechat.websocket.netty.NettyWebSocketStarter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.SQLException;

@Component("initRun")
public class InitRun implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(InitRun.class);
@Resource
DataSource dataSource;
@Resource
RedisUtils redisUtils;

@Resource
private NettyWebSocketStarter nettyWebSocketStarter;
    @Override
    public void run(ApplicationArguments args) {
        try{
            dataSource.getConnection();
            redisUtils.get("test");
            new Thread( nettyWebSocketStarter).start();

            logger.info("服务启动成功，可以开始开发啦！");
        }catch (SQLException e){
            logger.error("数据库连接失败：", e);
        }catch (RedisConnectionFailureException e){
            logger.error("Redis连接失败", e);
        }catch (Exception e){
            logger.error("服务器启动失败", e);
        }
    }
}
