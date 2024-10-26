package com.purplechat.entity.po;

import com.purplechat.enums.UserContactTypeEnum;

import java.io.Serializable;

/**
 * @Description:会话用户
 * @Author:文翔
 * @date:2024/07/28
 */
public class ChatSessionUser implements Serializable {

	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 联系人ID
	 */
	private String contactId;

	/**
	 * 会话ID
	 */
	private String sessionId;

	/**
	 * 联系人名称
	 */
	private String contactName;

	private String lastMessage;
	private Long lastReceiveTime;

	private Integer memberCount;

	private Integer contactType;

	public Integer getContactType() {
		return UserContactTypeEnum.getByPrefix(contactId).getType();
	}

	public void setContactType(Integer contactType) {
		this.contactType = contactType;
	}

	public Integer getMemberCount() {
		return memberCount;
	}

	public void setMemberCount(Integer memberCount) {
		this.memberCount = memberCount;
	}

	public String getLastMessage() {
		return lastMessage;
	}

	public void setLastMessage(String lastMessage) {
		this.lastMessage = lastMessage;
	}

	public Long getLastReceiveTime() {
		return lastReceiveTime;
	}

	public void setLastReceiveTime(Long lastReceiveTime) {
		this.lastReceiveTime = lastReceiveTime;
	}

	public void setUserId(String userId) { this.userId = userId; }
	public String getUserId() { return userId; }
	public void setContactId(String contactId) { this.contactId = contactId; }
	public String getContactId() { return contactId; }
	public void setSessionId(String sessionId) { this.sessionId = sessionId; }
	public String getSessionId() { return sessionId; }
	public void setContactName(String contactName) { this.contactName = contactName; }
	public String getContactName() { return contactName; }
	@Override
	public String toString(){
		return "用户ID:" + (userId == null ? "null" : userId) + ", " + "联系人ID:" + (contactId == null ? "null" : contactId) + ", " + "会话ID:" + (sessionId == null ? "null" : sessionId) + ", " + "联系人名称:" + (contactName == null ? "null" : contactName);
	}
}