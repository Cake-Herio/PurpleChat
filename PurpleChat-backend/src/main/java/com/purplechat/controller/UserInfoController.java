package com.purplechat.controller;


import com.purplechat.annotation.GlobalInterceptor;
import com.purplechat.entity.constants.Constants;
import com.purplechat.entity.dto.TokenUserInfoDto;
import com.purplechat.entity.po.UserInfo;
import com.purplechat.entity.vo.ResponseVO;
import com.purplechat.entity.vo.UserInfoVO;
import com.purplechat.enums.ResponseCodeEnum;
import com.purplechat.exception.BusinessException;
import com.purplechat.redis.RedisComponent;
import com.purplechat.service.UserInfoService;
import com.purplechat.utils.CopyTools;
import com.purplechat.utils.StringTools;
import com.purplechat.websocket.ChannelContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.io.IOException;

@RestController("userInfoController")
@RequestMapping("userInfo")
public class UserInfoController extends ABaseController {

    private static final Logger logger = LoggerFactory.getLogger(UserInfoController.class);

    @Resource
    private UserInfoService userInfoService;
    @Resource
    private ChannelContextUtil channelContextUtil;
    @Resource
    private RedisComponent redisComponent;


    @RequestMapping("/getUserInfo")
    @GlobalInterceptor
    public ResponseVO getUserInfo(HttpServletRequest request, String userId){

        TokenUserInfoDto userInfoByToken = getUserInfoByToken(request);
        if(userId == null) {
            userId = userInfoByToken.getUserId();
        }
        UserInfo userInfoByUserId = userInfoService.getUserInfoByUserId(userId);


        UserInfoVO userInfoVO = CopyTools.copy(userInfoByUserId, UserInfoVO.class);
//        userInfoVO.setAdmin(userInfoByUserId.isAdmin());
        return getSuccessResponseVO(userInfoVO);
    }

    @RequestMapping("/saveUserInfo")
    @GlobalInterceptor
    public ResponseVO saveUserInfo(HttpServletRequest request, UserInfo userInfo, MultipartFile avatarFile, MultipartFile coverFile) throws BusinessException, IOException {
        TokenUserInfoDto userInfoByToken = getUserInfoByToken(request);
        //重要信息 界面中不能修改的部分应该重置
        userInfo.setUserId(userInfoByToken.getUserId());
        userInfo.setPassword(null);
        userInfo.setEmail(null);
        userInfo.setCreateTime(null);
        userInfo.setStatus(null);

        //然后数据库上传
        userInfoService.updateUserInfo(userInfo, avatarFile, coverFile);
        return getUserInfo(request, userInfo.getUserId());
    }

    @RequestMapping("/updatePassword")
    @GlobalInterceptor
    public ResponseVO updatePassword(HttpServletRequest request, @NotEmpty String originPassword, @Pattern(regexp = Constants.REGEX_PASSWORD)String password) throws BusinessException {
        TokenUserInfoDto userInfoByToken = getUserInfoByToken(request);
        UserInfo userInfo = new UserInfo();
        userInfo.setPassword(StringTools.encodeMd5(password));
        //查询是否和原密码一致
        UserInfo userIdById = userInfoService.getUserInfoByUserId(userInfoByToken.getUserId());
        if(userIdById == null){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if(!userIdById.getPassword().equals(StringTools.encodeMd5(originPassword))){
            throw new BusinessException("初始密码不正确");
        }

        userInfoService.updateUserInfoByUserId(userInfo, userInfoByToken.getUserId());
        //修改密码后强制退出
        channelContextUtil.removeContextById(userInfoByToken.getUserId());
        //刪除dto
        redisComponent.removeTokenUserInfoDto(userInfoByToken.getUserId());

        return getSuccessResponseVO(null);
    }
    @RequestMapping("/forgetPassword")
    @GlobalInterceptor
    public ResponseVO updatePassword(HttpServletRequest request,
                                     @NotEmpty String checkEmailCodeKey,
                                     @NotEmpty String checkEmailCode,
                                     @NotEmpty String email,
                                     @Pattern(regexp = Constants.REGEX_PASSWORD)String password) throws BusinessException {
        TokenUserInfoDto userInfoByToken = getUserInfoByToken(request);
        if(userInfoByToken == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //查看链接查者的邮箱是否和上传的邮箱一样
        UserInfo userInfoByUserId = this.userInfoService.getUserInfoByUserId(userInfoByToken.getUserId());
        if(userInfoByUserId == null || !userInfoByUserId.getEmail().equals(email)){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //查询验证码是否正确
        if (!checkEmailCode.equals(redisUtils.get(Constants.REDIS_KEY_CHECK_EMAIL_CODE + checkEmailCodeKey))) {
            throw new BusinessException("邮箱验证码不正确");
        }
        //删除验证码
        redisUtils.delete(Constants.REDIS_KEY_CHECK_EMAIL_CODE + checkEmailCodeKey);

        UserInfo userInfo = new UserInfo();
        userInfo.setPassword(StringTools.encodeMd5(password));

        userInfoService.updateUserInfoByUserId(userInfo, userInfoByToken.getUserId());
        //修改密码后强制退出
        channelContextUtil.removeContextById(userInfoByToken.getUserId());
        //刪除dto
        redisComponent.removeTokenUserInfoDto(userInfoByToken.getUserId());

        return getSuccessResponseVO(null);
    }

    @RequestMapping("/logout")
    @GlobalInterceptor
    public ResponseVO logout(HttpServletRequest request) {
        TokenUserInfoDto userInfoByToken = getUserInfoByToken(request);
        //退出 关闭ws连接
        channelContextUtil.removeContextById(userInfoByToken.getUserId());
        //是否删除dto 只有改密码时需要强制删除dto

        return getSuccessResponseVO(null);
    }

}
