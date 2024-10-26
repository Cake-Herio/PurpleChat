package com.purplechat.entity.query;


/**
 * @Description:联系人申请查询对象
 * @Author:文翔
 * @date:2024/07/16
 */
public class UserContactApplyQuery extends BaseQuery{
	/**
	 * 自增ID查询对象
	 */
	private Integer applyId;

	/**
	 * 申请人ID查询对象
	 */
	private String applyUserId;
	private String applyUserIdFuzzy;

	/**
	 * 接收人ID查询对象
	 */
	private String receiveUserId;
	private String receiveUserIdFuzzy;

	/**
	 * 联系人类型 0:好友 1:群组查询对象
	 */
	private Integer contactType;

	/**
	 * 联系人群组ID查询对象
	 */
	private String contactId;
	private String contactIdFuzzy;

	/**
	 * 最后申请时间查询对象
	 */
	private Long lastApplyTime;

	/**
	 * 状态 0:待处理 1:已同意 2:已拒绝 3:已拉黑查询对象
	 */
	private Integer status;

	/**
	 * 申请信息查询对象
	 */
	private String applyInfo;
	private String applyInfoFuzzy;

	/**
	 *
	 * 是否关联查询nickname 用于展示
	 */

	private boolean queryContactInfo;


	//只查询一部分时间的数据
	private Long lastApplyTimeStamp;

	public Long getLastApplyTimeStamp() {
		return lastApplyTimeStamp;
	}

	public void setLastApplyTimeStamp(Long lastApplyTimeStamp) {
		this.lastApplyTimeStamp = lastApplyTimeStamp;
	}

	public boolean isQueryContactInfo() {
		return queryContactInfo;
	}

	public void setQueryContactInfo(boolean queryContactInfo) {
		this.queryContactInfo = queryContactInfo;
	}

	public void setApplyUserIdFuzzy(String applyUserIdFuzzy) { this.applyUserIdFuzzy = applyUserIdFuzzy; }
	public String getApplyUserIdFuzzy() { return applyUserIdFuzzy; }
	public void setReceiveUserIdFuzzy(String receiveUserIdFuzzy) { this.receiveUserIdFuzzy = receiveUserIdFuzzy; }
	public String getReceiveUserIdFuzzy() { return receiveUserIdFuzzy; }
	public void setContactIdFuzzy(String contactIdFuzzy) { this.contactIdFuzzy = contactIdFuzzy; }
	public String getContactIdFuzzy() { return contactIdFuzzy; }
	public void setApplyInfoFuzzy(String applyInfoFuzzy) { this.applyInfoFuzzy = applyInfoFuzzy; }
	public String getApplyInfoFuzzy() { return applyInfoFuzzy; }
	public void setApplyId(Integer applyId) { this.applyId = applyId; }
	public Integer getApplyId() { return applyId; }
	public void setApplyUserId(String applyUserId) { this.applyUserId = applyUserId; }
	public String getApplyUserId() { return applyUserId; }
	public void setReceiveUserId(String receiveUserId) { this.receiveUserId = receiveUserId; }
	public String getReceiveUserId() { return receiveUserId; }
	public void setContactType(Integer contactType) { this.contactType = contactType; }
	public Integer getContactType() { return contactType; }
	public void setContactId(String contactId) { this.contactId = contactId; }
	public String getContactId() { return contactId; }
	public void setLastApplyTime(Long lastApplyTime) { this.lastApplyTime = lastApplyTime; }
	public Long getLastApplyTime() { return lastApplyTime; }
	public void setStatus(Integer status) { this.status = status; }
	public Integer getStatus() { return status; }
	public void setApplyInfo(String applyInfo) { this.applyInfo = applyInfo; }
	public String getApplyInfo() { return applyInfo; }
}