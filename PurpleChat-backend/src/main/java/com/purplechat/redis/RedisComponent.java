package com.purplechat.redis;


import com.purplechat.entity.constants.Constants;
import com.purplechat.entity.dto.SysSettingDto;
import com.purplechat.entity.dto.TokenUserInfoDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.util.List;


@Component("redisComponent")
public class RedisComponent {
    private static final Logger logger = LoggerFactory.getLogger(RedisComponent.class);
    @Resource
    private RedisUtils redisUtils;

    /**
     * 获取心跳
     */
    public Long getUserHeart(String userId){
        return (Long) redisUtils.get(Constants.REDIS_KEY_WS_USER_HEART_BEAT + userId);
    }
    public void saveUserHeart(String userId){
        redisUtils.setex(Constants.REDIS_KEY_WS_USER_HEART_BEAT + userId, System.currentTimeMillis(), Constants.REDIS_KEY_EXPIRES_HEART_BEAT);
    }
    public void removeUserHeart(String userId){
        redisUtils.delete(Constants.REDIS_KEY_WS_USER_HEART_BEAT + userId);
    }


    public void saveTokenUserInfoDto (TokenUserInfoDto tokenUserInfoDto){
        redisUtils.setex(Constants.REDIS_KEY_WS_TOKEN + tokenUserInfoDto.getToken(), tokenUserInfoDto, Constants.REDIS_TIME_1DAY);
        redisUtils.setex(Constants.REDIS_KEY_WS_TOKEN_USERID + tokenUserInfoDto.getUserId(), tokenUserInfoDto.getToken(), Constants.REDIS_TIME_1DAY); //根据id查找token
    }

    public void removeTokenUserInfoDto (String userId){
        String token = (String) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN_USERID + userId);
        redisUtils.delete(Constants.REDIS_KEY_WS_TOKEN + token);
        redisUtils.delete(Constants.REDIS_KEY_WS_TOKEN_USERID + userId);
    }

    public TokenUserInfoDto getTokenUserInfoDtoById(String userId){
        String token = (String) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN_USERID + userId);
        if(token == null){
            logger.info("token为空");
            return null;
        }
        return (TokenUserInfoDto) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN + token);
    }






    public SysSettingDto getSysSetting(){
        SysSettingDto sysSettingDto = (SysSettingDto)redisUtils.get(Constants.REDIS_KEY_SYS_SETTING);
        sysSettingDto = sysSettingDto==null? new SysSettingDto():sysSettingDto; //new一个就是取的默认值的意思
        return sysSettingDto;
    }

    public void saveSysSetting(SysSettingDto sysSettingDto) {
        redisUtils.set(Constants.REDIS_KEY_SYS_SETTING, sysSettingDto);
    }

    //再单个添加联系人
    public void addUserContact(String userId, String contactId){
        List<String> userContactList = getUserContactList(userId);
        if(userContactList.contains(contactId)){
            logger.info("联系人已存在于列表中");
            return;
        }
        redisUtils.lPush(Constants.REDIS_KEY_USER_CONTACT + userId, contactId, Constants.REDIS_KEY_TOKEN_EXPIRE);
    }




    //批量添加联系人
    public void addUserContactBatch(String userId, List<String> contactList){
        redisUtils.lPushAll(Constants.REDIS_KEY_USER_CONTACT + userId, contactList, Constants.REDIS_KEY_TOKEN_EXPIRE);
    }

    public List<String> getUserContactList(String userId){
        return (List<String>)redisUtils.getQueueList(Constants.REDIS_KEY_USER_CONTACT + userId);
    }

    //清空联系人
    public void clearUserContact(String userId){
        redisUtils.delete(Constants.REDIS_KEY_USER_CONTACT + userId);
    }
    //获取联系人

    //移除联系人
    public void removeUserContact(String userId, String contactId){
        redisUtils.remove(Constants.REDIS_KEY_USER_CONTACT + userId,contactId); //将列表中的一个元素移出
    }


}
