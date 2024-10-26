package com.purplechat.entity.po;

import java.io.Serializable;

/**
 * @Description:会话信息
 * @Author:文翔
 * @date:2024/07/28
 */
public class ChatSession implements Serializable {
	/**
	 * 会话ID
	 */
	private String sessionId;

	/**
	 * 最后接受的消息
	 */
	private String lastMessage;

	/**
	 * 最后接受消息时间毫秒
	 */
	private Long lastReceiveTime;

	public void setSessionId(String sessionId) { this.sessionId = sessionId; }
	public String getSessionId() { return sessionId; }
	public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
	public String getLastMessage() { return lastMessage; }
	public void setLastReceiveTime(Long lastReceiveTime) { this.lastReceiveTime = lastReceiveTime; }
	public Long getLastReceiveTime() { return lastReceiveTime; }
	@Override
	public String toString(){
		return "会话ID:" + (sessionId == null ? "null" : sessionId) + ", " + "最后接受的消息:" + (lastMessage == null ? "null" : lastMessage) + ", " + "最后接受消息时间毫秒:" + (lastReceiveTime == null ? "null" : lastReceiveTime);
	}
}