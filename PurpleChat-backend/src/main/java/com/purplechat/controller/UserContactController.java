package com.purplechat.controller;


import com.purplechat.annotation.GlobalInterceptor;
import com.purplechat.entity.dto.TokenUserInfoDto;
import com.purplechat.entity.dto.UserContactSearchResultDto;
import com.purplechat.entity.po.UserContact;
import com.purplechat.entity.po.UserContactApply;
import com.purplechat.entity.po.UserInfo;
import com.purplechat.entity.query.UserContactApplyQuery;
import com.purplechat.entity.query.UserContactQuery;
import com.purplechat.entity.vo.PaginationResultVO;
import com.purplechat.entity.vo.ResponseVO;
import com.purplechat.entity.vo.UserInfoVO;
import com.purplechat.enums.*;
import com.purplechat.exception.BusinessException;
import com.purplechat.service.UserContactApplyService;
import com.purplechat.service.UserContactService;
import com.purplechat.service.UserInfoService;
import com.purplechat.utils.CopyTools;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController("userContactController")
@RequestMapping("contact")
public class UserContactController extends ABaseController{

    @Resource
    @Lazy
    UserContactService userContactService;
    @Resource
    @Lazy
    UserContactApplyService userContactApplyService;
    @Resource
    UserInfoService userInfoService;



    @RequestMapping("/search")
    @GlobalInterceptor
    public ResponseVO searchUserContact(HttpServletRequest request, @NotEmpty String contactId){
        TokenUserInfoDto userInfo = getUserInfoByToken(request);

        UserContactSearchResultDto userContactSearchResultDto = this.userContactService.searchUserContact(userInfo.getUserId(), contactId);
        return getSuccessResponseVO(userContactSearchResultDto);

    }


    //申请添加联系人
    @RequestMapping("/applyAdd")
    @GlobalInterceptor
    public ResponseVO applyAdd (HttpServletRequest request, @NotEmpty String contactId, String applyInfo, @NotEmpty Integer contactType) throws BusinessException {
        TokenUserInfoDto userInfo = getUserInfoByToken(request);
        Integer joinType = this.userContactService.applyAdd(userInfo, contactId, applyInfo, contactType);

        return getSuccessResponseVO(joinType);
    }


    //关联查nickname
    @RequestMapping("/loadApply")
    @GlobalInterceptor
    public ResponseVO loadApply (HttpServletRequest request, Integer pageNo) {
        TokenUserInfoDto userInfo = getUserInfoByToken(request);
        UserContactApplyQuery userContactApplyQuery = new UserContactApplyQuery();
        userContactApplyQuery.setOrderBy("last_apply_time desc");//根据时间倒排
        userContactApplyQuery.setReceiveUserId(userInfo.getUserId());
        userContactApplyQuery.setPageNo(pageNo);
        userContactApplyQuery.setPageSize(PageSize.SIZE15.getSize());
        userContactApplyQuery.setQueryContactInfo(true);
        PaginationResultVO<UserContactApply> listByPage = this.userContactApplyService.findListByPage(userContactApplyQuery);
        return getSuccessResponseVO(this.userContactApplyService.findListByPage(userContactApplyQuery));
    }


    @RequestMapping("/dealWithApply")
    @GlobalInterceptor
    public ResponseVO dealWithApply (HttpServletRequest request, @NotNull Integer applyId, @NotNull Integer status, String contactType) throws BusinessException {
        TokenUserInfoDto userInfo = getUserInfoByToken(request);

        this.userContactApplyService.dealWithApply(userInfo.getUserId(), applyId, status);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/loadContact")
    @GlobalInterceptor
    public ResponseVO loadContact (HttpServletRequest httpServletRequest, @NotNull String contactType) throws BusinessException {
        UserContactTypeEnum typeEnum = UserContactTypeEnum.getByName(contactType);
        if(null == typeEnum){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        TokenUserInfoDto userInfo = getUserInfoByToken(httpServletRequest);

        UserContactQuery userContactQuery = new UserContactQuery();
        userContactQuery.setUserId(userInfo.getUserId());
        userContactQuery.setStatusArray(new Integer[]{
                UserContactStatusEnum.FRIEND.getStatus(),
                UserContactStatusEnum.DELETE_BE.getStatus(),
                UserContactStatusEnum.BLACKLIST_BE.getStatus()
        });
        userContactQuery.setContactType(typeEnum.getType());
        if(UserContactTypeEnum.USER.getType().equals(typeEnum.getType())){
            userContactQuery.setQueryContactUserInfo(true);
        }else {
            userContactQuery.setQueryGroupInfo(true);
            userContactQuery.setExcludeMyGroup(true);
        }
        userContactQuery.setOrderBy("last_update_time desc");
        List<UserContact> list = this.userContactService.findListByParam(userContactQuery);

        return getSuccessResponseVO(list);
    }

    /**
     * 获取联系人详情 不一定是好友
     *
     */
    @RequestMapping("/getContactInfo")
    @GlobalInterceptor
    public ResponseVO getContactInfo (HttpServletRequest httpServletRequest, @NotEmpty String contactId) {
    //不是好友时也可以获取详情，因此联系人状态分开处理即可
        TokenUserInfoDto userInfo = getUserInfoByToken(httpServletRequest);
        UserInfo userInfoByUserId = this.userInfoService.getUserInfoByUserId(contactId);
        //拷贝到VO对象 敏感信息不外泄
        UserInfoVO userInfoVO = CopyTools.copy(userInfoByUserId, UserInfoVO.class);

        UserContact userContact = this.userContactService.getUserContactByUserIdAndContactId(userInfo.getUserId(), contactId);
        if(null == userContact){
            userInfoVO.setStatus(UserContactStatusEnum.NO_FRIEND.getStatus());
        }else{
            userInfoVO.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        }

        return getSuccessResponseVO(userInfoVO);
    }


    /**
     * 获取联系人详情 一定是好友
     *
     */
    @RequestMapping("/getContactUserInfo")
    @GlobalInterceptor
    public ResponseVO getContactUserInfo (HttpServletRequest httpServletRequest, @NotEmpty String contactId) throws BusinessException {
        TokenUserInfoDto userInfo = getUserInfoByToken(httpServletRequest);


        UserContact userContact = this.userContactService.getUserContactByUserIdAndContactId(userInfo.getUserId(), contactId);
        if(null == userContact || ArrayUtils.contains(new Integer[]{
                UserContactStatusEnum.NO_FRIEND.getStatus(),
                UserContactStatusEnum.DELETE_BE.getStatus(),
                UserContactStatusEnum.BLACKLIST_BE.getStatus()}, userContact.getStatus())){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        UserInfo userInfoByUserId = userInfoService.getUserInfoByUserId(contactId);
        UserInfoVO userInfoVO = CopyTools.copy(userInfoByUserId, UserInfoVO.class);
        return getSuccessResponseVO(userInfoVO);
    }

    @RequestMapping("/delContact")
    @GlobalInterceptor
    public ResponseVO delContact(HttpServletRequest httpServletRequest, @NotEmpty String contactId) throws BusinessException {
        TokenUserInfoDto userInfoByToken = getUserInfoByToken(httpServletRequest);
        userContactService.removeUserContact(userInfoByToken.getUserId(), contactId, UserContactStatusEnum.DELETE);
        return getSuccessResponseVO(null);
    }

    //彻底移除数据库中的contact
    @RequestMapping("/removeContact")
    @GlobalInterceptor
    public ResponseVO removeContact(HttpServletRequest httpServletRequest, @NotEmpty String contactId) throws BusinessException {
        TokenUserInfoDto userInfoByToken = getUserInfoByToken(httpServletRequest);
        userContactService.deleteUserContactByUserIdAndContactId(userInfoByToken.getUserId(), contactId);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/addContact2BlackList")
    @GlobalInterceptor
    public ResponseVO addContact2BlackList(HttpServletRequest httpServletRequest, @NotEmpty String contactId) throws BusinessException {
        TokenUserInfoDto userInfoByToken = getUserInfoByToken(httpServletRequest);
        userContactService.removeUserContact(userInfoByToken.getUserId(), contactId, UserContactStatusEnum.BLACKLIST);
        return getSuccessResponseVO(null);
    }



}
