package com.purplechat.entity.query;


import java.util.List;

/**
 * @Description:聊天消息表查询对象
 * @Author:文翔
 * @date:2024/07/28
 */
public class ChatMessageQuery extends BaseQuery{
	/**
	 * 消息自增ID查询对象
	 */
	private Long messageId;

	/**
	 * 会话ID查询对象
	 */
	private String sessionId;
	private String sessionIdFuzzy;

	/**
	 * 消息类型查询对象
	 */
	private Integer messageType;

	/**
	 * 消息内容查询对象
	 */
	private String messageContent;
	private String messageContentFuzzy;

	/**
	 * 发送人ID查询对象
	 */
	private String sendUserId;
	private String sendUserIdFuzzy;

	/**
	 * 发送人昵称查询对象
	 */
	private String sendUserNickName;
	private String sendUserNickNameFuzzy;

	/**
	 * 发送时间查询对象
	 */
	private Long sendTime;

	/**
	 * 接收联系人ID查询对象
	 */
	private String contactId;
	private String contactIdFuzzy;

	/**
	 * 联系人类型(0:单聊 1:群聊)查询对象
	 */
	private Integer contactType;

	/**
	 * 文件大小查询对象
	 */
	private Long fileSize;

	/**
	 * 文件名查询对象
	 */
	private String fileName;
	private String fileNameFuzzy;

	/**
	 * 文件类型查询对象
	 */
	private Integer fileType;

	/**
	 * 状态(0:正在发送 1:已发送)查询对象
	 */
	private Integer status;

	private List<String> contactIdList;

	private Long lastReceiveTime;

	public Long getLastReceiveTime() {
		return lastReceiveTime;
	}

	public void setLastReceiveTime(Long lastReceiveTime) {
		this.lastReceiveTime = lastReceiveTime;
	}

	public List<String> getContactIdList() {
		return contactIdList;
	}

	public void setContactIdList(List<String> contactIdList) {
		this.contactIdList = contactIdList;
	}

	public void setSessionIdFuzzy(String sessionIdFuzzy) { this.sessionIdFuzzy = sessionIdFuzzy; }
	public String getSessionIdFuzzy() { return sessionIdFuzzy; }
	public void setMessageContentFuzzy(String messageContentFuzzy) { this.messageContentFuzzy = messageContentFuzzy; }
	public String getMessageContentFuzzy() { return messageContentFuzzy; }
	public void setSendUserIdFuzzy(String sendUserIdFuzzy) { this.sendUserIdFuzzy = sendUserIdFuzzy; }
	public String getSendUserIdFuzzy() { return sendUserIdFuzzy; }
	public void setSendUserNickNameFuzzy(String sendUserNickNameFuzzy) { this.sendUserNickNameFuzzy = sendUserNickNameFuzzy; }
	public String getSendUserNickNameFuzzy() { return sendUserNickNameFuzzy; }
	public void setContactIdFuzzy(String contactIdFuzzy) { this.contactIdFuzzy = contactIdFuzzy; }
	public String getContactIdFuzzy() { return contactIdFuzzy; }
	public void setFileNameFuzzy(String fileNameFuzzy) { this.fileNameFuzzy = fileNameFuzzy; }
	public String getFileNameFuzzy() { return fileNameFuzzy; }
	public void setMessageId(Long messageId) { this.messageId = messageId; }
	public Long getMessageId() { return messageId; }
	public void setSessionId(String sessionId) { this.sessionId = sessionId; }
	public String getSessionId() { return sessionId; }
	public void setMessageType(Integer messageType) { this.messageType = messageType; }
	public Integer getMessageType() { return messageType; }
	public void setMessageContent(String messageContent) { this.messageContent = messageContent; }
	public String getMessageContent() { return messageContent; }
	public void setSendUserId(String sendUserId) { this.sendUserId = sendUserId; }
	public String getSendUserId() { return sendUserId; }
	public void setSendUserNickName(String sendUserNickName) { this.sendUserNickName = sendUserNickName; }
	public String getSendUserNickName() { return sendUserNickName; }
	public void setSendTime(Long sendTime) { this.sendTime = sendTime; }
	public Long getSendTime() { return sendTime; }
	public void setContactId(String contactId) { this.contactId = contactId; }
	public String getContactId() { return contactId; }
	public void setContactType(Integer contactType) { this.contactType = contactType; }
	public Integer getContactType() { return contactType; }
	public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
	public Long getFileSize() { return fileSize; }
	public void setFileName(String fileName) { this.fileName = fileName; }
	public String getFileName() { return fileName; }
	public void setFileType(Integer fileType) { this.fileType = fileType; }
	public Integer getFileType() { return fileType; }
	public void setStatus(Integer status) { this.status = status; }
	public Integer getStatus() { return status; }
}