package com.purplechat.entity.po;

import java.io.Serializable;

/**
 * @Description:联系人申请
 * @Author:文翔
 * @date:2024/07/16
 */
public class UserContactApply implements Serializable {
	/**
	 * 自增ID
	 */
	private Integer applyId;

	/**
	 * 申请人ID
	 */
	private String applyUserId;

	/**
	 * 接收人ID
	 */
	private String receiveUserId;

	/**
	 * 联系人类型 0:好友 1:群组
	 */
	private Integer contactType;

	/**
	 * 联系人群组ID
	 */
	private String contactId;

	/**
	 * 最后申请时间
	 */
	private Long lastApplyTime;

	/**
	 * 状态 0:待处理 1:已同意 2:已拒绝 3:已拉黑
	 */
	private Integer status;

	/**
	 * 申请信息
	 */
	private String applyInfo;

	private String contactName;

	private String statusName;


	public String getStatusName() {
		return statusName;
	}

	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

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
	@Override
	public String toString(){
		return "自增ID:" + (applyId == null ? "null" : applyId) + ", " + "申请人ID:" + (applyUserId == null ? "null" : applyUserId) + ", " + "接收人ID:" + (receiveUserId == null ? "null" : receiveUserId) + ", " + "联系人类型 0:好友 1:群组:" + (contactType == null ? "null" : contactType) + ", " + "联系人群组ID:" + (contactId == null ? "null" : contactId) + ", " + "最后申请时间:" + (lastApplyTime == null ? "null" : lastApplyTime) + ", " + "状态 0:待处理 1:已同意 2:已拒绝 3:已拉黑:" + (status == null ? "null" : status) + ", " + "申请信息:" + (applyInfo == null ? "null" : applyInfo);
	}
}