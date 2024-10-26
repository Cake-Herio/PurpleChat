package com.purplechat.service.impl;

import com.purplechat.entity.po.ChatSession;
import com.purplechat.entity.query.ChatSessionQuery;
import com.purplechat.entity.vo.PaginationResultVO;
import com.purplechat.entity.query.SimplePage;
import com.purplechat.enums.PageSize;
import com.purplechat.mappers.ChatSessionMapper;
import com.purplechat.service.ChatSessionService;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
/**
 * @Description:会话信息Service
 * @Author:文翔
 * @date:2024/07/28
 */
@Service("chatSessionService")
public class ChatSessionServiceImpl implements ChatSessionService {

@Resource
private ChatSessionMapper<ChatSession, ChatSessionQuery> chatSessionMapper;
	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<ChatSession> findListByParam (ChatSessionQuery query) { return this.chatSessionMapper.selectList(query); }
	/**
	 * 根据条件查询数量
	 */
	@Override
	public Integer findCountByParam (ChatSessionQuery query) { return this.chatSessionMapper.selectCount(query); }

	/**
	 * 分页查询
	 */
	@Override
	public PaginationResultVO<ChatSession> findListByPage (ChatSessionQuery query) {
		Integer count = this.findCountByParam(query);
		Integer pageSize = query.getPageNo() == null?PageSize.SIZE15.getSize():query.getPageSize();
		SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
		query.setSimplePage(page);
		List<ChatSession> list = this.findListByParam(query);
		PaginationResultVO<ChatSession> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add (ChatSession bean) { return this.chatSessionMapper.insert(bean); }

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch (List<ChatSessionQuery> listBean){
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.chatSessionMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增/修改
	 */
	@Override
	public Integer addOrUpdateBatch (List<ChatSessionQuery> listBean){
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.chatSessionMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 根据SessionId查询
	 */
	 public ChatSession getChatSessionBySessionId(String sessionId) { return this.chatSessionMapper.selectBySessionId(sessionId); }

	/**
	 * 根据SessionId修改
	 */
	 public Integer updateChatSessionBySessionId(ChatSession bean, String sessionId) { return this.chatSessionMapper.updateBySessionId(bean, sessionId); }

	/**
	 * 根据SessionId删除
	 */
	 public Integer deleteChatSessionBySessionId(String sessionId) { return this.chatSessionMapper.deleteBySessionId(sessionId); }


}