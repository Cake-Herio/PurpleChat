package com.purplechat.service;

import java.io.IOException;

import com.purplechat.entity.po.AppUpdate;
import com.purplechat.entity.query.AppUpdateQuery;
import com.purplechat.entity.vo.PaginationResultVO;
import java.util.List;

import com.purplechat.exception.BusinessException;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Description:app发布Service
 * @Author:文翔
 * @date:2024/07/26
 */
public interface AppUpdateService {

	/**
	 * 根据条件查询列表
	 */
	List<AppUpdate> findListByParam (AppUpdateQuery query);

	/**
	 * 根据条件查询数量
	 */
	Integer findCountByParam (AppUpdateQuery query);

	/**
	 * 分页查询
	 */
	PaginationResultVO<AppUpdate> findListByPage (AppUpdateQuery query);

	/**
	 * 新增
	 */
	Integer add (AppUpdate bean);

	/**
	 * 批量新增
	 */
	Integer addBatch (List<AppUpdateQuery> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch (List<AppUpdateQuery> listBean);

	/**
	 * 根据Id查询
	 */
	AppUpdate getAppUpdateById(Integer id);

	/**
	 * 根据Id修改
	 */
	Integer updateAppUpdateById(AppUpdate bean, Integer id);

	/**
	 * 根据Id删除
	 */
	Integer deleteAppUpdateById(Integer id);

	/**
	 * 根据Version查询
	 */
	AppUpdate getAppUpdateByVersion(String version);

	/**
	 * 根据Version修改
	 */
	Integer updateAppUpdateByVersion(AppUpdate bean, String version);

	/**
	 * 根据Version删除
	 */
	Integer deleteAppUpdateByVersion(String version);

	void saveUpdate	(AppUpdate appUpdate, MultipartFile file) throws BusinessException, IOException;

	void postUpdate(Integer id, Integer status, String grayscaleUID) throws BusinessException, IOException;

	AppUpdate getLatestUpdate(String appVersion, String uid);


}