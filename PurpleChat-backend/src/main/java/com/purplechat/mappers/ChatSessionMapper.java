package com.purplechat.mappers;

import java.io.Serializable;
import org.apache.ibatis.annotations.Param;
/**
 * @Description:会话信息
 * @Author:文翔
 * @date:2024/07/28
 */
public interface ChatSessionMapper<T, P> extends BaseMapper {
	/**
	 * 根据SessionId查询
	 */
	T selectBySessionId(@Param("sessionId") String sessionId);
	/**
	 * 根据SessionId更新
	 */
	Integer updateBySessionId(@Param("bean") T t, @Param("sessionId") String sessionId);
	/**
	 * 根据SessionId删除
	 */
	Integer deleteBySessionId(@Param("sessionId") String sessionId);
}