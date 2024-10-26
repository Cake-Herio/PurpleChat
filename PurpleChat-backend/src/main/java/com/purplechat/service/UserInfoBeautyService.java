package com.purplechat.service;

import com.purplechat.entity.po.UserInfoBeauty;
import com.purplechat.entity.query.UserInfoBeautyQuery;
import com.purplechat.entity.vo.PaginationResultVO;
import com.purplechat.exception.BusinessException;

import java.util.List;
/**
 * @Description:靓号表Service
 * @Author:文翔
 * @date:2024/06/10
 */
public interface UserInfoBeautyService {

	/**
	 * 根据条件查询列表
	 */
	List<UserInfoBeauty> findListByParam (UserInfoBeautyQuery query);

	/**
	 * 根据条件查询数量
	 */
	Integer findCountByParam (UserInfoBeautyQuery query);

	/**
	 * 分页查询
	 */
	PaginationResultVO<UserInfoBeauty> findListByPage (UserInfoBeautyQuery query);

	/**
	 * 新增
	 */
	Integer add (UserInfoBeauty bean);

	/**
	 * 批量新增
	 */
	Integer addBatch (List<UserInfoBeautyQuery> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch (List<UserInfoBeautyQuery> listBean);

	/**
	 * 根据Id查询
	 */
	UserInfoBeauty getUserInfoBeautyById(Integer id);

	/**
	 * 根据Id修改
	 */
	Integer updateUserInfoBeautyById(UserInfoBeauty bean, Integer id);

	/**
	 * 根据Id删除
	 */
	Integer deleteUserInfoBeautyById(Integer id);

	/**
	 * 根据UserId查询
	 */
	UserInfoBeauty getUserInfoBeautyByUserId(String userId);

	/**
	 * 根据UserId修改
	 */
	Integer updateUserInfoBeautyByUserId(UserInfoBeauty bean, String userId);

	/**
	 * 根据UserId删除
	 */
	Integer deleteUserInfoBeautyByUserId(String userId);

	/**
	 * 根据Email查询
	 */
	UserInfoBeauty getUserInfoBeautyByEmail(String email);

	/**
	 * 根据Email修改
	 */
	Integer updateUserInfoBeautyByEmail(UserInfoBeauty bean, String email);

	/**
	 * 根据Email删除
	 */
	Integer deleteUserInfoBeautyByEmail(String email);

	void saveBeautyAccount(UserInfoBeauty userInfoBeauty) throws BusinessException;


}