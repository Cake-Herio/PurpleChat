package com.purplechat.mappers;

import java.io.Serializable;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Property;

/**
 * @Description:app发布
 * @Author:文翔
 * @date:2024/07/26
 */
public interface AppUpdateMapper<T, P> extends BaseMapper {
	/**
	 * 根据Id查询
	 */
	T selectById(@Param("id") Integer id);
	/**
	 * 根据Id更新
	 */
	Integer updateById(@Param("bean") T t, @Param("id") Integer id);
	/**
	 * 根据Id删除
	 */
	Integer deleteById(@Param("id") Integer id);
	/**
	 * 根据Version查询
	 */
	T selectByVersion(@Param("version") String version);
	/**
	 * 根据Version更新
	 */
	Integer updateByVersion(@Param("bean") T t, @Param("version") String version);
	/**
	 * 根据Version删除
	 */
	Integer deleteByVersion(@Param("version") String version);

	T selectLastedUpdate(@Param("appVersion") String appVersion, @Param("uid") String uid);
}