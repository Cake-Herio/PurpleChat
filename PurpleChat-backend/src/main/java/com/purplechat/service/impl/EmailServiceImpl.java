package com.purplechat.service.impl;

import com.purplechat.entity.constants.Constants;
import com.purplechat.entity.po.UserInfo;
import com.purplechat.entity.query.UserInfoQuery;
import com.purplechat.exception.BusinessException;
import com.purplechat.mappers.UserInfoMapper;
import com.purplechat.redis.RedisUtils;
import com.purplechat.service.EmailAsyncService;
import com.purplechat.service.EmailService;
import com.purplechat.utils.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service("emailServiceImpl")
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired
    private JavaMailSender javaMailSender;

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    @Resource
    private EmailAsyncService emailAsyncService;

    public Map<String, String> sendEmail(String to, Boolean isLoginByEmail) throws MessagingException, BusinessException {
        //查数据库是否存在 存在则拒绝
        if(isLoginByEmail){
            UserInfo userInfo = userInfoMapper.selectByEmail(to);
            if (userInfo != null) {
                throw new BusinessException("邮箱已被注册");
            }
        }
        String emailCode = generateEmailCode();
        emailAsyncService.sendEmailAsync(to, emailCode); // 等待异步任务完成并获取结果
        //存redis
        String checkEmailCodeKey = UUID.randomUUID().toString();
        redisUtils.setex(Constants.REDIS_KEY_CHECK_EMAIL_CODE + checkEmailCodeKey, emailCode, Constants.REDIS_TIME_1MIN * 2);
        Map<String, String> response = new HashMap<>();
        logger.info("执行");
        response.put("checkEmailCodeKey", checkEmailCodeKey);
        return response;
    }

    private static String generateEmailCode() {
        return StringTools.getRandomNumber(6);
    }
}
