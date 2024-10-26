package com.purplechat.entity.query;


/**
 * @Description:会话信息查询对象
 * @Author:文翔
 * @date:2024/07/28
 */
public class ChatSessionQuery extends BaseQuery{
	/**
	 * 会话ID查询对象
	 */
	private String sessionId;
	private String sessionIdFuzzy;

	/**
	 * 最后接受的消息查询对象
	 */
	private String lastMessage;
	private String lastMessageFuzzy;

	/**
	 * 最后接受消息时间毫秒查询对象
	 */
	private Long lastReceiveTime;

	public void setSessionIdFuzzy(String sessionIdFuzzy) { this.sessionIdFuzzy = sessionIdFuzzy; }
	public String getSessionIdFuzzy() { return sessionIdFuzzy; }
	public void setLastMessageFuzzy(String lastMessageFuzzy) { this.lastMessageFuzzy = lastMessageFuzzy; }
	public String getLastMessageFuzzy() { return lastMessageFuzzy; }
	public void setSessionId(String sessionId) { this.sessionId = sessionId; }
	public String getSessionId() { return sessionId; }
	public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
	public String getLastMessage() { return lastMessage; }
	public void setLastReceiveTime(Long lastReceiveTime) { this.lastReceiveTime = lastReceiveTime; }
	public Long getLastReceiveTime() { return lastReceiveTime; }
}