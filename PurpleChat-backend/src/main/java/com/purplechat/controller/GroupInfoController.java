package com.purplechat.controller;

import com.purplechat.annotation.GlobalInterceptor;
import com.purplechat.entity.dto.TokenUserInfoDto;
import com.purplechat.entity.po.GroupInfo;
import com.purplechat.entity.po.UserContact;
import com.purplechat.entity.query.GroupInfoQuery;
import com.purplechat.entity.query.UserContactQuery;
import com.purplechat.entity.vo.GroupInfoVO;
import com.purplechat.entity.vo.ResponseVO;
import com.purplechat.enums.GroupStatusEnum;
import com.purplechat.enums.MessageTypeEnum;
import com.purplechat.enums.ResponseCodeEnum;
import com.purplechat.enums.UserContactStatusEnum;
import com.purplechat.exception.BusinessException;
import com.purplechat.service.GroupInfoService;
import com.purplechat.service.UserContactService;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;

/**
 * @Description:Service
 * @Author:文翔
 * @date:2024/07/16
 */
@RestController("GroupInfoController")
@RequestMapping("group")
public class GroupInfoController extends ABaseController{

	@Resource
	private GroupInfoService groupInfoService;
	@Resource
	@Lazy
	private UserContactService userContactService;

	@RequestMapping("saveGroup")
	@GlobalInterceptor
	public ResponseVO saveGroup (HttpServletRequest request,
								 String groupId,
								 @NotEmpty String groupName,
								 String groupNotice,
								 @NotNull Integer joinType,
								 MultipartFile avatarFile,
								 MultipartFile avatarCover) throws BusinessException, IOException {
		TokenUserInfoDto userInfo = getUserInfoByToken(request);
		if(null == userInfo){
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}

		GroupInfo groupInfo = new GroupInfo();
		groupInfo.setGroupName(groupName);
		groupInfo.setJoinType(joinType);
		groupInfo.setGroupNotice(groupNotice);
		groupInfo.setGroupOwnerId(userInfo.getUserId());
		groupInfo.setGroupId(groupId);
		groupInfoService.saveGroup(groupInfo, avatarFile, avatarCover);
		return getSuccessResponseVO(null);
	}

	@RequestMapping("/loadMyGroup")
	@GlobalInterceptor
	public ResponseVO loadMyGroup(HttpServletRequest request){
		TokenUserInfoDto userInfo = getUserInfoByToken(request);
		GroupInfoQuery groupInfoQuery = new GroupInfoQuery();
		groupInfoQuery.setGroupOwnerId(userInfo.getUserId());
		groupInfoQuery.setStatus(GroupStatusEnum.NORMAL.getStatus());
		groupInfoQuery.setOrderBy("create_time desc");
		List<GroupInfo> groupInfoList = this.groupInfoService.findListByParam(groupInfoQuery);
		return getSuccessResponseVO(groupInfoList);
	}

	@RequestMapping("/getGroupInfo")
	@GlobalInterceptor
	public ResponseVO getGroupInfo(HttpServletRequest request, @NotEmpty String groupId) throws BusinessException {
		GroupInfo groupInfo = getGroupDetailCommon(request, groupId);
		//查群成员
		UserContactQuery userContactQuery = new UserContactQuery();
		userContactQuery.setContactId(groupId);
		userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
		Integer memberCount = this.userContactService.findCountByParam(userContactQuery);
		groupInfo.setMemberCount(memberCount);
		return getSuccessResponseVO(groupInfo);
	}


	//查询具体的联系人
	@RequestMapping("/getGroupInfo4Chat")
	@GlobalInterceptor
	public ResponseVO getGroupInfo4chat(HttpServletRequest request, String groupId) throws BusinessException {
		GroupInfo groupInfo = getGroupDetailCommon(request, groupId);

		UserContactQuery userContactQuery = new UserContactQuery();
		userContactQuery.setContactId(groupInfo.getGroupId());
		userContactQuery.setOrderBy("create_time asc");
		userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
		//关联查询 用户的昵称
		userContactQuery.setQueryUserInfo(true);
		List<UserContact> userContactList = this.userContactService.findListByParam(userContactQuery);

		GroupInfoVO groupInfoVO = new GroupInfoVO();
		groupInfoVO.setGroupInfo(groupInfo);
		groupInfoVO.setUserContactList(userContactList);//也可以通过放到map中，然后再返回
		return getSuccessResponseVO(groupInfoVO);
	}


	//管理员添加或移除
	@RequestMapping("/leaveGroup")
	@GlobalInterceptor
	public ResponseVO leaveGroup(HttpServletRequest request, @NotEmpty String groupId) throws BusinessException {
		TokenUserInfoDto userInfoByToken = getUserInfoByToken(request);
		groupInfoService.leaveGroup(userInfoByToken.getUserId(), groupId, MessageTypeEnum.LEAVE_GROUP);
		return getSuccessResponseVO(null);
	}

	//退群
	@RequestMapping("/addOrRemoveGroupUser")
	@GlobalInterceptor
	public ResponseVO addOrRemoveGroupUser(HttpServletRequest request, @NotEmpty String groupId, @NotEmpty String selectContacts, @NotNull Integer type) throws BusinessException {
		groupInfoService.addOrRemoveGroupUser(getUserInfoByToken(request), groupId, selectContacts, type);
		return getSuccessResponseVO(null);
	}

	@RequestMapping("/dissolutionGroup")
	@GlobalInterceptor
	public ResponseVO dissolutionGroup(HttpServletRequest request, @NotEmpty String groupId) throws BusinessException {
		TokenUserInfoDto userInfoByToken = getUserInfoByToken(request);
		groupInfoService.dissolutionGroup(userInfoByToken.getUserId(), groupId);
		return getSuccessResponseVO(null);
	}



 



	//保证查询的人只能是群成员
	private GroupInfo getGroupDetailCommon(HttpServletRequest request, String groupId) throws BusinessException {
		TokenUserInfoDto userInfo = getUserInfoByToken(request);

		UserContact userContact = this.userContactService.getUserContactByUserIdAndContactId(userInfo.getUserId(), groupId);
		if(null == userContact|| !UserContactStatusEnum.FRIEND.getStatus().equals(userContact.getStatus())){
			throw  new BusinessException("你不在群聊中，亦或是群状态不正常");  
		}

		GroupInfo groupInfo= this.groupInfoService.getGroupInfoByGroupId(groupId);
		if(null == groupId|| GroupStatusEnum.ABNORMAL.getStatus().equals(groupInfo.getStatus())){
			throw  new BusinessException("群状态不正常");
		}
		return groupInfo;
	}

}