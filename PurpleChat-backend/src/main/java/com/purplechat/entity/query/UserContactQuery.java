package com.purplechat.entity.query;

import java.util.Date;

/**
 * @Description:联系人查询对象
 * @Author:文翔
 * @date:2024/07/16
 */
public class UserContactQuery extends BaseQuery{
	/**
	 * 用户ID查询对象
	 */
	private String userId;
	private String userIdFuzzy;

	/**
	 * 联系人ID或者群组ID查询对象
	 */
	private String contactId;
	private String contactIdFuzzy;

	/**
	 * 联系人类型 0:好友 1:群组查询对象
	 */
	private Integer contactType;

	/**
	 * 创建时间查询对象
	 */
	private Date createTime;
	private String createTimeStart;
	private String createTimeEnd;

	/**
	 * 状态 0:非好友 1:好友 2:已删除 3:拉黑查询对象
	 */
	private Integer status;

	/**
	 * 最后更新时间查询对象
	 */
	private Date lastUpdateTime;
	private String lastUpdateTimeStart;
	private String lastUpdateTimeEnd;

	/**
	 *
	 * 是否要关联查询用户信息(发起人）
	 */
	private boolean queryUserInfo;

	/**
	 *
	 * 是否要关联查询用户信息(接收人）
	 */
	private boolean queryContactUserInfo;


	public boolean isQueryContactUserInfo() {
		return queryContactUserInfo;
	}

	public void setQueryContactUserInfo(boolean queryContactUserInfo) {
		this.queryContactUserInfo = queryContactUserInfo;
	}

	/**
	 *
	 * 是否要关联查询群组信息
	 */
	private boolean queryGroupInfo;

	/**
	 *
	 * 是否要排除我自己的群
	 */
	private boolean excludeMyGroup;

	public boolean isExcludeMyGroup() {

		return excludeMyGroup;
	}

	/**
	 *
	 * 状态为哪些时将显示
	 */

	private Integer[] statusArray;

	public Integer[] getStatusArray() {
		return statusArray;
	}

	public void setStatusArray(Integer[] statusArray) {
		this.statusArray = statusArray;
	}

	public void setExcludeMyGroup(boolean excludeMyGroup) {
		this.excludeMyGroup = excludeMyGroup;
	}

	public boolean isQueryGroupInfo() {
		return queryGroupInfo;
	}

	public void setQueryGroupInfo(boolean queryGroupInfo) {
		this.queryGroupInfo = queryGroupInfo;
	}

	public boolean isQueryUserInfo() {
		return queryUserInfo;
	}

	public void setQueryUserInfo(boolean queryUserInfo) {
		this.queryUserInfo = queryUserInfo;
	}

	public void setUserIdFuzzy(String userIdFuzzy) { this.userIdFuzzy = userIdFuzzy; }
	public String getUserIdFuzzy() { return userIdFuzzy; }
	public void setContactIdFuzzy(String contactIdFuzzy) { this.contactIdFuzzy = contactIdFuzzy; }
	public String getContactIdFuzzy() { return contactIdFuzzy; }
	public void setCreateTimeStart(String createTimeStart) { this.createTimeStart = createTimeStart; }
	public String getCreateTimeStart() { return createTimeStart; }
	public void setCreateTimeEnd(String createTimeEnd) { this.createTimeEnd = createTimeEnd; }
	public String getCreateTimeEnd() { return createTimeEnd; }
	public void setLastUpdateTimeStart(String lastUpdateTimeStart) { this.lastUpdateTimeStart = lastUpdateTimeStart; }
	public String getLastUpdateTimeStart() { return lastUpdateTimeStart; }
	public void setLastUpdateTimeEnd(String lastUpdateTimeEnd) { this.lastUpdateTimeEnd = lastUpdateTimeEnd; }
	public String getLastUpdateTimeEnd() { return lastUpdateTimeEnd; }
	public void setUserId(String userId) { this.userId = userId; }
	public String getUserId() { return userId; }
	public void setContactId(String contactId) { this.contactId = contactId; }
	public String getContactId() { return contactId; }
	public void setContactType(Integer contactType) { this.contactType = contactType; }
	public Integer getContactType() { return contactType; }
	public void setCreateTime(Date createTime) { this.createTime = createTime; }
	public Date getCreateTime() { return createTime; }
	public void setStatus(Integer status) { this.status = status; }
	public Integer getStatus() { return status; }
	public void setLastUpdateTime(Date lastUpdateTime) { this.lastUpdateTime = lastUpdateTime; }
	public Date getLastUpdateTime() { return lastUpdateTime; }
}