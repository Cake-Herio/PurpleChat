package com.purplechat.service;

import com.purplechat.entity.po.ChatSession;
import com.purplechat.entity.query.ChatSessionQuery;
import com.purplechat.entity.vo.PaginationResultVO;
import java.util.List;
/**
 * @Description:会话信息Service
 * @Author:文翔
 * @date:2024/07/28
 */
public interface ChatSessionService {

	/**
	 * 根据条件查询列表
	 */
	List<ChatSession> findListByParam (ChatSessionQuery query);

	/**
	 * 根据条件查询数量
	 */
	Integer findCountByParam (ChatSessionQuery query);

	/**
	 * 分页查询
	 */
	PaginationResultVO<ChatSession> findListByPage (ChatSessionQuery query);

	/**
	 * 新增
	 */
	Integer add (ChatSession bean);

	/**
	 * 批量新增
	 */
	Integer addBatch (List<ChatSessionQuery> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch (List<ChatSessionQuery> listBean);

	/**
	 * 根据SessionId查询
	 */
	ChatSession getChatSessionBySessionId(String sessionId);

	/**
	 * 根据SessionId修改
	 */
	Integer updateChatSessionBySessionId(ChatSession bean, String sessionId);

	/**
	 * 根据SessionId删除
	 */
	Integer deleteChatSessionBySessionId(String sessionId);


}