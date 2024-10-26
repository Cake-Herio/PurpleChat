package com.purplechat.service;

import com.purplechat.entity.po.UserContactApply;
import com.purplechat.entity.query.UserContactApplyQuery;
import com.purplechat.entity.vo.PaginationResultVO;
import com.purplechat.exception.BusinessException;

import java.util.List;
/**
 * @Description:联系人申请Service
 * @Author:文翔
 * @date:2024/07/16
 */
public interface UserContactApplyService {

	/**
	 * 根据条件查询列表
	 */
	List<UserContactApply> findListByParam (UserContactApplyQuery query);

	/**
	 * 根据条件查询数量
	 */
	Integer findCountByParam (UserContactApplyQuery query);

	/**
	 * 分页查询
	 */
	PaginationResultVO<UserContactApply> findListByPage (UserContactApplyQuery query);

	/**
	 * 新增
	 */
	Integer add (UserContactApply bean);

	/**
	 * 批量新增
	 */
	Integer addBatch (List<UserContactApplyQuery> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch (List<UserContactApplyQuery> listBean);

	/**
	 * 根据ApplyId查询
	 */
	UserContactApply getUserContactApplyByApplyId(Integer applyId);

	/**
	 * 根据ApplyId修改
	 */
	Integer updateUserContactApplyByApplyId(UserContactApply bean, Integer applyId);

	/**
	 * 根据ApplyId删除
	 */
	Integer deleteUserContactApplyByApplyId(Integer applyId);

	/**
	 * 根据ApplyUserIdAndReceiveUserIdAndContactId查询
	 */
	UserContactApply getUserContactApplyByApplyUserIdAndReceiveUserIdAndContactId(String applyUserId, String receiveUserId, String contactId);

	/**
	 * 根据ApplyUserIdAndReceiveUserIdAndContactId修改
	 */
	Integer updateUserContactApplyByApplyUserIdAndReceiveUserIdAndContactId(UserContactApply bean, String applyUserId, String receiveUserId, String contactId);

	/**
	 * 根据ApplyUserIdAndReceiveUserIdAndContactId删除
	 */
	Integer deleteUserContactApplyByApplyUserIdAndReceiveUserIdAndContactId(String applyUserId, String receiveUserId, String contactId);

	void dealWithApply (String userId, Integer applyId, Integer status) throws BusinessException;




}