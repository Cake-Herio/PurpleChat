package com.purplechat.entity.query;


/**
 * @Description:会话用户查询对象
 * @Author:文翔
 * @date:2024/07/28
 */
public class ChatSessionUserQuery extends BaseQuery{
	/**
	 * 用户ID查询对象
	 */
	private String userId;
	private String userIdFuzzy;

	/**
	 * 联系人ID查询对象
	 */
	private String contactId;
	private String contactIdFuzzy;

	/**
	 * 会话ID查询对象
	 */
	private String sessionId;
	private String sessionIdFuzzy;

	/**
	 * 联系人名称查询对象
	 */
	private String contactName;
	private String contactNameFuzzy;

	public void setUserIdFuzzy(String userIdFuzzy) { this.userIdFuzzy = userIdFuzzy; }
	public String getUserIdFuzzy() { return userIdFuzzy; }
	public void setContactIdFuzzy(String contactIdFuzzy) { this.contactIdFuzzy = contactIdFuzzy; }
	public String getContactIdFuzzy() { return contactIdFuzzy; }
	public void setSessionIdFuzzy(String sessionIdFuzzy) { this.sessionIdFuzzy = sessionIdFuzzy; }
	public String getSessionIdFuzzy() { return sessionIdFuzzy; }
	public void setContactNameFuzzy(String contactNameFuzzy) { this.contactNameFuzzy = contactNameFuzzy; }
	public String getContactNameFuzzy() { return contactNameFuzzy; }
	public void setUserId(String userId) { this.userId = userId; }
	public String getUserId() { return userId; }
	public void setContactId(String contactId) { this.contactId = contactId; }
	public String getContactId() { return contactId; }
	public void setSessionId(String sessionId) { this.sessionId = sessionId; }
	public String getSessionId() { return sessionId; }
	public void setContactName(String contactName) { this.contactName = contactName; }
	public String getContactName() { return contactName; }
}