package com.purplechat.service;

import com.purplechat.entity.dto.TokenUserInfoDto;
import com.purplechat.entity.dto.UserContactSearchResultDto;
import com.purplechat.entity.po.UserContact;
import com.purplechat.entity.query.UserContactQuery;
import com.purplechat.entity.vo.PaginationResultVO;
import java.util.List;

import com.purplechat.enums.UserContactStatusEnum;
import com.purplechat.exception.BusinessException;

/**
 * @Description:联系人Service
 * @Author:文翔
 * @date:2024/07/16
 */
public interface UserContactService {

	/**
	 * 根据条件查询列表
	 */
	List<UserContact> findListByParam (UserContactQuery query);

	/**
	 * 根据条件查询数量
	 */
	Integer findCountByParam (UserContactQuery query);

	/**
	 * 分页查询
	 */
	PaginationResultVO<UserContact> findListByPage (UserContactQuery query);

	/**
	 * 新增
	 */
	Integer add (UserContact bean);

	/**
	 * 批量新增
	 */
	Integer addBatch (List<UserContactQuery> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch (List<UserContactQuery> listBean);

	/**
	 * 根据UserIdAndContactId查询
	 */
	UserContact getUserContactByUserIdAndContactId(String userId, String contactId);

	/**
	 * 根据UserIdAndContactId修改
	 */
	Integer updateUserContactByUserIdAndContactId(UserContact bean, String userId, String contactId);

	/**
	 * 根据UserIdAndContactId删除
	 */
	Integer deleteUserContactByUserIdAndContactId(String userId, String contactId);

	UserContactSearchResultDto searchUserContact(String userId, String contactId);


	Integer applyAdd(TokenUserInfoDto userInfo, String contactId, String applyInfo, Integer contactType) throws BusinessException;

	void removeUserContact(String userId, String contactId, UserContactStatusEnum statusEnum) throws BusinessException;

	void  addContact(String applyUserId, String receiveUserId, String contactId, Integer contactType, String applyInfo) throws BusinessException;



	void addContact4Robot(String userId);


}