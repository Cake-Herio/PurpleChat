package com.purplechat.service;

import java.io.File;
import java.io.IOException;

import com.purplechat.entity.dto.MessageSendDto;
import com.purplechat.entity.dto.TokenUserInfoDto;
import com.purplechat.entity.po.ChatMessage;
import com.purplechat.entity.query.ChatMessageQuery;
import com.purplechat.entity.vo.PaginationResultVO;
import com.purplechat.exception.BusinessException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
/**
 * @Description:聊天消息表Service
 * @Author:文翔
 * @date:2024/07/28
 */
public interface ChatMessageService {

	/**
	 * 根据条件查询列表
	 */
	List<ChatMessage> findListByParam (ChatMessageQuery query);

	/**
	 * 根据条件查询数量
	 */
	Integer findCountByParam (ChatMessageQuery query);

	/**
	 * 分页查询
	 */
	PaginationResultVO<ChatMessage> findListByPage (ChatMessageQuery query);

	/**
	 * 新增
	 */
	Integer add (ChatMessage bean);

	/**
	 * 批量新增
	 */
	Integer addBatch (List<ChatMessageQuery> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch (List<ChatMessageQuery> listBean);

	/**
	 * 根据MessageId查询
	 */
	ChatMessage getChatMessageByMessageId(Long messageId);

	/**
	 * 根据MessageId修改
	 */
	Integer updateChatMessageByMessageId(ChatMessage bean, Long messageId);

	/**
	 * 根据MessageId删除
	 */
	Integer deleteChatMessageByMessageId(Long messageId);


	//处理聊天逻辑
	MessageSendDto saveMessage (ChatMessage chatMessage, TokenUserInfoDto tokenUserInfoDto) throws BusinessException;

	void saveMessageFile (String userId, Long messageId, MultipartFile file, MultipartFile cover) throws BusinessException, IOException;

	File downloadFile (TokenUserInfoDto tokenUserInfoDto, Long fileId , boolean showFile) throws BusinessException;


}