package com.purplechat.redis;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component("redisUtils")
public class RedisUtils<V> {

    @Resource
    private RedisTemplate<String, V> redisTemplate;
    private static final Logger logger = LoggerFactory.getLogger(RedisUtils.class);

    /**
     * 删除集合
     *
     * @param key 可以传一个或多个值
     */

    public void delete(String... key){
        if(key!=null && key.length > 0){
            if(key.length == 1){
                redisTemplate.delete(key[0]);
            } else {
                redisTemplate.delete((Collection<String>) CollectionUtils.arrayToList(key));
            }
        }
    }

    public V get(String key){
        return key == null?null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 普通缓存放入
     *
     *@param key 键
     *@param value 值
     * @return true 存储成功  false 存储失败
     */

    public boolean set (String key, V value){
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        }catch (Exception e){
            logger.error("设置redisKey:{}, value:{}失败", key, value);
            return false;
        }
    }

    /**
     * 普通缓存放入并设置存储时间
     *
     *@param key 键
     *@param value 值
     *@return true 存储成功  false 存储失败
     */

    public boolean setex (String key, V value, long time){
        try {
            redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            return true;
        }catch (Exception e){
            logger.error("设置redisKey:{}, value:{}失败", key, value);
            return false;
        }
    }

    /**
     * 设置已经存储的key的过期时间
     *
     */

    public boolean expire(String key, long time){
        try {
            if(time > 0){
                redisTemplate.expire(key, time, TimeUnit.SECONDS);

            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 取队列
     *
     */

    public List<V> getQueueList(String key){
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    public boolean lPush(String key, V value, long time){
        try {
            redisTemplate.opsForList().leftPush(key, value);
            if(time > 0){
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public long remove(String key, Object value){ //移除list中等于value的元素 ， 并返回
        try {
            return redisTemplate.opsForList().remove(key, 1, value); //1表示从头到尾搜索，移除最多一个值为value的元素
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    public boolean lPushAll(String key, List<V> values, long time){
        try {
            redisTemplate.opsForList().leftPushAll(key, values);
            if(time > 0){
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }





}
