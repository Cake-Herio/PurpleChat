package com.purplechat.controller;

import com.purplechat.annotation.GlobalInterceptor;
import com.purplechat.entity.constants.Constants;
import com.purplechat.entity.dto.TokenUserInfoDto;
import com.purplechat.entity.vo.ResponseVO;
import com.purplechat.entity.vo.UserInfoVO;
import com.purplechat.exception.BusinessException;
import com.purplechat.redis.RedisComponent;
import com.purplechat.redis.RedisUtils;
import com.purplechat.service.EmailService;
import com.purplechat.service.UserInfoService;
import com.purplechat.websocket.netty.HandlerMessage;
import com.wf.captcha.ArithmeticCaptcha;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController("AccountController")
@RequestMapping("account")
@Validated
public class AccountController extends ABaseController {

    @Resource
    private RedisUtils redisUtils;
    @Resource
    private UserInfoService userInfoService;
    @Resource
    private EmailService emailService;
    @Resource
    private RedisComponent redisComponent;
    @Resource
    private HandlerMessage handlerMessage;


    Logger logger = LoggerFactory.getLogger(AccountController.class);

    @RequestMapping("checkCode")
    public ResponseVO checkCode(HttpServletRequest request) {
        String origin = request.getHeader("Origin");

        // 打印 Origin 信息
        logger.info("origin:{}", origin);
        //生成验证码
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(84, 42);
        String code = captcha.text();
        String base64 = captcha.toBase64();
        String checkCodeKey = UUID.randomUUID().toString();



        logger.info("验证码是： {}", code);

        //将验证码存入redis，需要记录保存时间
        redisUtils.setex(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey, code, Constants.REDIS_TIME_1MIN * 2);


        Map<String, String> response = new HashMap<>();
        response.put("checkCode", base64);
        response.put("checkCodeKey", checkCodeKey);
        return getSuccessResponseVO(response);
    }

    @RequestMapping("getEmailCode")
    public ResponseVO getEmailCode(@NotEmpty @Email String email, boolean isLoginByEmail) throws MessagingException, BusinessException {
        Map<String, String> response = emailService.sendEmail(email, isLoginByEmail);
        return getSuccessResponseVO(response);
    }

    @RequestMapping("register")
    public ResponseVO register(@NotEmpty String checkCodeKey,
                               @NotEmpty @Email String accountId,
                               @NotEmpty @Pattern(regexp = Constants.REGEX_PASSWORD) String password,
                               @NotEmpty String nickName,
                               @NotEmpty String checkCode,
                               @NotEmpty String checkEmailCodeKey,
                               @NotEmpty String checkEmailCode) throws BusinessException {
        //校验验证码
        //验证码不用区分大小写 所以不用equals
        if (!checkCode.equalsIgnoreCase((String) redisUtils.get(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey))) {
            throw new BusinessException("图片验证码不正确");
        }
        if (!checkEmailCode.equals(redisUtils.get(Constants.REDIS_KEY_CHECK_EMAIL_CODE + checkEmailCodeKey))) {
            throw new BusinessException("邮箱验证码不正确");
        }
        String email = accountId;
        userInfoService.register(email, password, nickName);
        //删除验证码
        redisUtils.delete(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey);
        redisUtils.delete(Constants.REDIS_KEY_CHECK_EMAIL_CODE + checkEmailCodeKey);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("login")
    public ResponseVO login(@NotEmpty String checkCodeKey,
                            String accountId,
                            @NotEmpty @Pattern(regexp = Constants.REGEX_PASSWORD) String password,
                            @NotEmpty String checkCode) throws BusinessException {
        //校验验证码
        //验证码不用区分大小写 所以不用equals
        if (!checkCode.equalsIgnoreCase((String) redisUtils.get(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey))) {
            throw new BusinessException("图片验证码不正确");
        }
        //删除验证码
        redisUtils.delete(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey);
        UserInfoVO userInfoVO = userInfoService.login(accountId, password);

        return getSuccessResponseVO(userInfoVO);
    }

    @RequestMapping("loginDirect")
    public ResponseVO loginDirect(@NotEmpty String token) throws BusinessException {
        TokenUserInfoDto userInfo = (TokenUserInfoDto) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN + token);
        if(null == userInfo){
            throw new BusinessException("身份失效，请使用密码登录");
        }
        UserInfoVO userInfoVO = userInfoService.loginDirect(token);
        return getSuccessResponseVO(userInfoVO);
    }

    @RequestMapping("loginForget")
    public ResponseVO loginDirect(@NotEmpty String checkCodeKey,
                                  @NotEmpty String checkEmailCodeKey,
                                  @Email String email,
                                  @NotEmpty String checkEmailCode,
                                  @NotEmpty String checkCode) throws BusinessException {
        //校验验证码
        //验证码不用区分大小写 所以不用equals
        if (!checkCode.equalsIgnoreCase((String) redisUtils.get(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey))) {
            throw new BusinessException("图片验证码不正确");
        }
        //删除验证码
        if (!checkEmailCode.equals(redisUtils.get(Constants.REDIS_KEY_CHECK_EMAIL_CODE + checkEmailCodeKey))) {
            throw new BusinessException("邮箱验证码不正确");
        }
        UserInfoVO userInfoVO = userInfoService.loginForget(email);

        redisUtils.delete(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey);
        redisUtils.delete(Constants.REDIS_KEY_CHECK_EMAIL_CODE + checkEmailCodeKey);
        return getSuccessResponseVO(userInfoVO);
    }

    @RequestMapping("getSysSetting")
    @GlobalInterceptor
    public ResponseVO SysSetting(){
        return getSuccessResponseVO(redisComponent.getSysSetting());
    }


    @RequestMapping("test")
    public ResponseVO Test(){
        return getSuccessResponseVO("你好");
    }

}
