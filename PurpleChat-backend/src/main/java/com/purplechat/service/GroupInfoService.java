package com.purplechat.service;

import java.io.IOException;

import com.purplechat.entity.dto.TokenUserInfoDto;
import com.purplechat.entity.po.GroupInfo;
import com.purplechat.entity.query.GroupInfoQuery;
import com.purplechat.entity.vo.PaginationResultVO;
import java.util.List;

import com.purplechat.enums.MessageTypeEnum;
import com.purplechat.exception.BusinessException;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Description:Service
 * @Author:文翔
 * @date:2024/07/16
 */
public interface GroupInfoService {

	/**
	 * 根据条件查询列表
	 */
	List<GroupInfo> findListByParam (GroupInfoQuery query);

	/**
	 * 根据条件查询数量
	 */
	Integer findCountByParam (GroupInfoQuery query);

	/**
	 * 分页查询
	 */
	PaginationResultVO<GroupInfo> findListByPage (GroupInfoQuery query);

	/**
	 * 新增
	 */
	Integer add (GroupInfo bean);

	/**
	 * 批量新增
	 */
	Integer addBatch (List<GroupInfoQuery> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch (List<GroupInfoQuery> listBean);

	/**
	 * 根据GroupId查询
	 */
	GroupInfo getGroupInfoByGroupId(String groupId);

	/**
	 * 根据GroupId修改
	 */
	Integer updateGroupInfoByGroupId(GroupInfo bean, String groupId);

	/**
	 * 根据GroupId删除
	 */
	Integer deleteGroupInfoByGroupId(String groupId);

	void saveGroup(GroupInfo groupInfo, MultipartFile avatarFile,
				   MultipartFile avatarCover) throws BusinessException, IOException;

	void dissolutionGroup(String groupOwnerId, String groupId) throws BusinessException;

	void addOrRemoveGroupUser(TokenUserInfoDto tokenUserInfoDto, String groupId, String selectContacts, Integer type) throws BusinessException;

	void leaveGroup(String contactId, String groupId, MessageTypeEnum messageTypeEnum) throws BusinessException;


}