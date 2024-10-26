package com.purplechat.mappers;

import java.io.Serializable;
import org.apache.ibatis.annotations.Param;
/**
 * @Description:
 * @Author:文翔
 * @date:2024/07/16
 */
public interface GroupInfoMapper<T, P> extends BaseMapper {
	/**
	 * 根据GroupId查询
	 */
	T selectByGroupId(@Param("groupId") String groupId);
	/**
	 * 根据GroupId更新
	 */
	Integer updateByGroupId(@Param("bean") T t, @Param("groupId") String groupId);
	/**
	 * 根据GroupId删除
	 */
	Integer deleteByGroupId(@Param("groupId") String groupId);
}