package com.purplechat.entity.po;

import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import com.purplechat.utils.DateUtils;
import com.purplechat.enums.DateTimePatternEnum;
/**
 * @Description:用户信息
 * @Author:文翔
 * @date:2024/06/10
 */
public class UserInfo implements Serializable {
	/**
	 * 创建用户名
	 */
	private String userId;

	/**
	 * 邮箱
	 */
	private String email;

	/**
	 * 昵称
	 */
	private String nickName;

	/**
	 * 0：直接加入 1：同意后加好友
	 */
	private Integer joinType;

	/**
	 * 性别 0：女 1：男
	 */
	private Integer sex;

	/**
	 * 密码
	 */
	private String password;

	/**
	 * 个性签名
	 */
	private String personalSignature;

	/**
	 * 状态
	 */
	private Integer status;

	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH-mm-ss", timezone = "GMT+8")
	@DateTimeFormat (pattern = "yyyy-MM-dd HH-mm-ss")
	private Date createTime;

	/**
	 * 最后登录时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH-mm-ss", timezone = "GMT+8")
	@DateTimeFormat (pattern = "yyyy-MM-dd HH-mm-ss")
	private Date lastLoginTime;

	/**
	 * 地区
	 */
	private String areaName;

	/**
	 * 地区编号
	 */
	private String areaCode;

	/**
	 * 最后离开时间
	 */
	private Long lastOffTime;


	private Integer onlineType;

	public Integer getOnlineType() {

		if(lastLoginTime != null && lastLoginTime.getTime() > lastOffTime){
			return 1;
		}
		else{
			return 0;
		}
	}

	public void setOnlineType(Integer onlineType) {
		this.onlineType = onlineType;
	}

	public void setUserId(String userId) { this.userId = userId; }

	public String getUserId() { return userId; }

	public void setEmail(String email) { this.email = email; }

	public String getEmail() { return email; }
	public void setNickName(String nickName) { this.nickName = nickName; }
	public String getNickName() { return nickName; }
	public void setJoinType(Integer joinType) { this.joinType = joinType; }
	public Integer getJoinType() { return joinType; }
	public void setSex(Integer sex) { this.sex = sex; }
	public Integer getSex() { return sex; }
	public void setPassword(String password) { this.password = password; }
	public String getPassword() { return password; }
	public void setPersonalSignature(String personalSignature) { this.personalSignature = personalSignature; }
	public String getPersonalSignature() { return personalSignature; }
	public void setStatus(Integer status) { this.status = status; }
	public Integer getStatus() { return status; }
	public void setCreateTime(Date createTime) { this.createTime = createTime; }
	public Date getCreateTime() { return createTime; }
	public void setLastLoginTime(Date lastLoginTime) { this.lastLoginTime = lastLoginTime; }
	public Date getLastLoginTime() { return lastLoginTime; }
	public void setAreaName(String areaName) { this.areaName = areaName; }
	public String getAreaName() { return areaName; }
	public void setAreaCode(String areaCode) { this.areaCode = areaCode; }
	public String getAreaCode() { return areaCode; }
	public void setLastOffTime(Long lastOffTime) { this.lastOffTime = lastOffTime; }
	public Long getLastOffTime() { return lastOffTime; }
	@Override
	public String toString(){
		return "创建用户名:" + (userId == null ? "null" : userId) + ", " + "邮箱:" + (email == null ? "null" : email) + ", " + "昵称:" + (nickName == null ? "null" : nickName) + ", " + "0：直接加入 1：同意后加好友:" + (joinType == null ? "null" : joinType) + ", " + "性别 0：女 1：男:" + (sex == null ? "null" : sex) + ", " + "密码:" + (password == null ? "null" : password) + ", " + "个性签名:" + (personalSignature == null ? "null" : personalSignature) + ", " + "状态:" + (status == null ? "null" : status) + ", " + "创建时间:" + (DateUtils.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()) == null ? "null" : DateUtils.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + ", " + "最后登录时间:" + (DateUtils.format(lastLoginTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()) == null ? "null" : DateUtils.format(lastLoginTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + ", " + "地区:" + (areaName == null ? "null" : areaName) + ", " + "地区编号:" + (areaCode == null ? "null" : areaCode) + ", " + "最后离开时间:" + (lastOffTime == null ? "null" : lastOffTime);
	}
}