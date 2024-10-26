package com.purplechat.aspect;


import com.purplechat.annotation.GlobalInterceptor;
import com.purplechat.entity.constants.Constants;
import com.purplechat.entity.dto.TokenUserInfoDto;
import com.purplechat.enums.ResponseCodeEnum;
import com.purplechat.exception.BusinessException;
import com.purplechat.redis.RedisComponent;
import com.purplechat.redis.RedisUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Aspect
@Component("globalOperationAspect")
public class GlobalOperationAspect {
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private RedisComponent redisComponent;

    private static final Logger logger =  LoggerFactory.getLogger(GlobalOperationAspect.class);

    @Before("@annotation(com.purplechat.annotation.GlobalInterceptor)")
    public void interceptorDo (JoinPoint point) throws BusinessException {
        try{
            Method method = ((MethodSignature) point.getSignature()).getMethod();
            GlobalInterceptor interceptor = method.getAnnotation(GlobalInterceptor.class);
            if(interceptor == null){
                return;
            }
            if(interceptor.checkLogin()) {
                checkLogin(interceptor.checkAdmin());
            }
        }catch(BusinessException e) {
            logger.error("全局拦截异常", e);
            throw e;
        } catch(Throwable e){
            logger.error("全局拦截异常" , e);
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        }





    }

    private void checkLogin(Boolean checkAdmin) throws BusinessException {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        String token = request.getHeader("token");
        TokenUserInfoDto tokenUserInfoDto = (TokenUserInfoDto) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN + token);
        if(null == tokenUserInfoDto){
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        Long a = redisComponent.getUserHeart(tokenUserInfoDto.getUserId());
        System.out.println(a);
        //检查心跳
        if(redisComponent.getUserHeart(tokenUserInfoDto.getUserId()) == null){
            throw new BusinessException(ResponseCodeEnum.CODE_505);
        }


        if(checkAdmin&&!tokenUserInfoDto.isAdmin()) {
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }






    }


}
