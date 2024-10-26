package com.purplechat.service;

import java.io.IOException;

import com.purplechat.entity.po.UserInfo;
import com.purplechat.entity.query.UserInfoQuery;
import com.purplechat.entity.vo.PaginationResultVO;
import java.util.List;

import com.purplechat.entity.vo.UserInfoVO;
import com.purplechat.exception.BusinessException;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Description:用户信息Service
 * @Author:文翔
 * @date:2024/06/10
 */
public interface UserInfoService {

	/**
	 * 根据条件查询列表
	 */
	List<UserInfo> findListByParam (UserInfoQuery query);

	/**
	 * 根据条件查询数量
	 */
	Integer findCountByParam (UserInfoQuery query);

	/**
	 * 分页查询
	 */
	PaginationResultVO<UserInfo> findListByPage (UserInfoQuery query);

	/**
	 * 新增
	 */
	Integer add (UserInfo bean);

	/**
	 * 批量新增
	 */
	Integer addBatch (List<UserInfoQuery> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch (List<UserInfoQuery> listBean);

	/**
	 * 根据UserId查询
	 */
	UserInfo getUserInfoByUserId(String userId);

	/**
	 * 根据UserId修改
	 */
	Integer updateUserInfoByUserId(UserInfo bean, String userId);

	/**
	 * 根据UserId删除
	 */
	Integer deleteUserInfoByUserId(String userId);

	/**
	 * 根据Email查询
	 */
	UserInfo getUserInfoByEmail(String email);

	/**
	 * 根据Email修改
	 */
	Integer updateUserInfoByEmail(UserInfo bean, String email);

	/**
	 * 根据Email删除
	 */
	Integer deleteUserInfoByEmail(String email);

	void register(String email,
						String password,
						String nickName)throws BusinessException;

	UserInfoVO login(String accountId,
					 String password)throws BusinessException;

	UserInfoVO loginDirect(String token) throws BusinessException;

	UserInfoVO loginForget(String email)throws BusinessException;

	void updateUserInfo(UserInfo userInfo,  MultipartFile avatarFile, MultipartFile avatarCover) throws BusinessException, IOException;

	void updateUserStatus(Integer status, String userId) throws BusinessException;
	void forceOffLine(String userId) throws BusinessException;

}