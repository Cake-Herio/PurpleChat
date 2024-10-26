package com.purplechat.controller;


import com.purplechat.annotation.GlobalInterceptor;
import com.purplechat.entity.po.UserInfoBeauty;
import com.purplechat.entity.query.UserInfoBeautyQuery;
import com.purplechat.entity.vo.PaginationResultVO;
import com.purplechat.entity.vo.ResponseVO;
import com.purplechat.exception.BusinessException;
import com.purplechat.service.UserInfoBeautyService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

@RestController("adminUserInfoBeautyController")
@RequestMapping("/admin")
public class AdminUserInfoBeautyController extends ABaseController{

    @Resource
    UserInfoBeautyService userInfoBeautyService;

    @RequestMapping("/loadBeautyAccountList")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO loadBeautyAccountList(UserInfoBeautyQuery query){
        query.setOrderBy("id desc"); //按时间倒序排
        PaginationResultVO<UserInfoBeauty> resultVO = userInfoBeautyService.findListByPage(query);
        return getSuccessResponseVO(resultVO);
    }

    @RequestMapping("/saveBeautyAccount")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO saveBeautyAccount(UserInfoBeauty userInfoBeauty) throws BusinessException {

        userInfoBeautyService.saveBeautyAccount(userInfoBeauty);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/delBeautyAccount")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO delBeautyAccount(@NotNull String userId) {
            userInfoBeautyService.deleteUserInfoBeautyByUserId(userId);
        return getSuccessResponseVO(null);
    }


}
