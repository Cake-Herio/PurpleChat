package com.purplechat.controller;


import com.purplechat.annotation.GlobalInterceptor;
import com.purplechat.entity.po.UserInfo;
import com.purplechat.entity.query.UserInfoQuery;
import com.purplechat.entity.vo.PaginationResultVO;
import com.purplechat.entity.vo.ResponseVO;
import com.purplechat.exception.BusinessException;
import com.purplechat.service.UserInfoBeautyService;
import com.purplechat.service.UserInfoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@RestController("adminController")
@RequestMapping("/admin")
public class AdminController extends ABaseController{

    @Resource
    UserInfoService userInfoService;
    @Resource
    UserInfoBeautyService userInfoBeautyService;

    @RequestMapping("/loadUser")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO loadUser(UserInfoQuery query){
        query.setOrderBy("create_time desc"); //按时间倒序排
        PaginationResultVO<UserInfo> resultVO = userInfoService.findListByPage(query);
        return getSuccessResponseVO(resultVO);
    }



    @RequestMapping("/updateUserStatus")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO updateUserStatus(@NotNull Integer status, @NotEmpty String userId) throws BusinessException {
        userInfoService.updateUserStatus(status, userId);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/forceOffLine")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO forceOffLine(@NotEmpty String userId) throws BusinessException {
        userInfoService.forceOffLine(userId);
        return getSuccessResponseVO(null);
    }










}
