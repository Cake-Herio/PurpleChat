package com.purplechat.service;

import com.purplechat.entity.po.ChatSessionUser;
import com.purplechat.entity.query.ChatSessionUserQuery;
import com.purplechat.entity.vo.PaginationResultVO;
import java.util.List;
/**
 * @Description:会话用户Service
 * @Author:文翔
 * @date:2024/07/28
 */
public interface ChatSessionUserService {

	/**
	 * 根据条件查询列表
	 */
	List<ChatSessionUser> findListByParam (ChatSessionUserQuery query);

	/**
	 * 根据条件查询数量
	 */
	Integer findCountByParam (ChatSessionUserQuery query);

	/**
	 * 分页查询
	 */
	PaginationResultVO<ChatSessionUser> findListByPage (ChatSessionUserQuery query);

	/**
	 * 新增
	 */
	Integer add (ChatSessionUser bean);

	/**
	 * 批量新增
	 */
	Integer addBatch (List<ChatSessionUserQuery> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch (List<ChatSessionUserQuery> listBean);

	/**
	 * 根据UserIdAndContactId查询
	 */
	ChatSessionUser getChatSessionUserByUserIdAndContactId(String userId, String contactId);

	/**
	 * 根据UserIdAndContactId修改
	 */
	Integer updateChatSessionUserByUserIdAndContactId(ChatSessionUser bean, String userId, String contactId);

	/**
	 * 根据UserIdAndContactId删除
	 */
	Integer deleteChatSessionUserByUserIdAndContactId(String userId, String contactId);


	/**
	 * 更新冗余信息 昵称
	 */
	public void updateNickName(String contactName, String contactId);




}