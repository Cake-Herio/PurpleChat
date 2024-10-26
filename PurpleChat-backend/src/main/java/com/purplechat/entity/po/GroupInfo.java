package com.purplechat.entity.po;

import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import com.purplechat.utils.DateUtils;
import com.purplechat.enums.DateTimePatternEnum;

/**
 * @Description:
 * @Author:文翔
 * @date:2024/07/16
 */
public class GroupInfo implements Serializable {
	/**
	 * 群ID
	 */
	private String groupId;

	/**
	 * 群组名
	 */
	private String groupName;

	/**
	 * 群主id
	 */
	private String groupOwnerId;

	private String groupOwnerName;

	public String getGroupOwnerName() {
		return groupOwnerName;
	}

	public void setGroupOwnerName(String groupOwnerName) {
		this.groupOwnerName = groupOwnerName;
	}

	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH-mm-ss", timezone = "GMT+8")
	@DateTimeFormat (pattern = "yyyy-MM-dd HH-mm-ss")
	private Date createTime;

	/**
	 * 群公告
	 */
	private String groupNotice;

	/**
	 * 0:直接加入 1:管理员同意后加入
	 */
	private Integer joinType;

	/**
	 * 状态 1:正常 0:解散
	 */
	private Integer status;

	private Integer memberCount;

	public Integer getMemberCount() {
		return memberCount;
	}

	public void setMemberCount(Integer memberCount) {
		this.memberCount = memberCount;
	}

	public void setGroupId(String groupId) { this.groupId = groupId; }
	public String getGroupId() { return this.groupId; }
	public void setGroupName(String groupName) { this.groupName = groupName; }
	public String getGroupName() { return this.groupName; }
	public void setGroupOwnerId(String groupOwnerId) { this.groupOwnerId = groupOwnerId; }
	public String getGroupOwnerId() { return groupOwnerId; }
	public void setCreateTime(Date createTime) { this.createTime = createTime; }
	public Date getCreateTime() { return createTime; }
	public void setGroupNotice(String groupNotice) { this.groupNotice = groupNotice; }
	public String getGroupNotice() { return this.groupNotice; }
	public void setJoinType(Integer joinType) { this.joinType = joinType; }
	public Integer getJoinType() { return this.joinType; }
	public void setStatus(Integer status) { this.status = status; }
	public Integer getStatus() { return status; }
	@Override
	public String toString(){
		return "群ID:" + (groupId == null ? "null" : groupId) + ", " + "群组名:" + (groupName == null ? "null" : groupName) + ", " + "群主id:" + (groupOwnerId == null ? "null" : groupOwnerId) + ", " + "创建时间:" + (DateUtils.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()) == null ? "null" : DateUtils.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + ", " + "群公告:" + (groupNotice == null ? "null" : groupNotice) + ", " + "0:直接加入 1:管理员同意后加入:" + (joinType == null ? "null" : joinType) + ", " + "状态 1:正常 0:解散:" + (status == null ? "null" : status);
	}
}