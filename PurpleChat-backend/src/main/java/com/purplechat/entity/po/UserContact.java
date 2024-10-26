package com.purplechat.entity.po;

import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import com.purplechat.utils.DateUtils;
import com.purplechat.enums.DateTimePatternEnum;
/**
 * @Description:联系人
 * @Author:文翔
 * @date:2024/07/16
 */
public class UserContact implements Serializable {
	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 联系人ID或者群组ID
	 */
	private String contactId;

	/**
	 * 联系人类型 0:好友 1:群组
	 */
	private Integer contactType;

	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH-mm-ss", timezone = "GMT+8")
	@DateTimeFormat (pattern = "yyyy-MM-dd HH-mm-ss")
	private Date createTime;

	/**
	 * 状态 0:非好友 1:好友 2:已删除 3:拉黑
	 */
	private Integer status;

	/**
	 * 最后更新时间
	 */

	@JsonFormat(pattern = "yyyy-MM-dd HH-mm-ss", timezone = "GMT+8")
	@DateTimeFormat (pattern = "yyyy-MM-dd HH-mm-ss")
	private Date lastUpdateTime;


	private String contactName;
	private Integer sex;

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public Integer getSex() {
		return sex;
	}

	public void setSex(Integer sex) {
		this.sex = sex;
	}

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
	@Override
	public String toString(){
		return "用户ID:" + (userId == null ? "null" : userId) + ", " + "联系人ID或者群组ID:" + (contactId == null ? "null" : contactId) + ", " + "联系人类型 0:好友 1:群组:" + (contactType == null ? "null" : contactType) + ", " + "创建时间:" + (DateUtils.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()) == null ? "null" : DateUtils.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + ", " + "状态 0:非好友 1:好友 2:已删除 3:拉黑:" + (status == null ? "null" : status) + ", " + "最后更新时间:" + (DateUtils.format(lastUpdateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()) == null ? "null" : DateUtils.format(lastUpdateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}