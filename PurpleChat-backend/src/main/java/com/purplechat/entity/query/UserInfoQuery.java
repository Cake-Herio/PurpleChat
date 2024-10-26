package com.purplechat.entity.query;

import java.util.Date;

/**
 * @Description:用户信息查询对象
 * @Author:文翔
 * @date:2024/06/10
 */
public class UserInfoQuery extends BaseQuery{
	/**
	 * 创建用户名查询对象
	 */
	private String userId;
	private String userIdFuzzy;

	/**
	 * 邮箱查询对象
	 */
	private String email;
	private String emailFuzzy;

	/**
	 * 昵称查询对象
	 */
	private String nickName;
	private String nickNameFuzzy;

	/**
	 * 0：直接加入 1：同意后加好友查询对象
	 */
	private Integer joinType;

	/**
	 * 性别 0：女 1：男查询对象
	 */
	private Integer sex;

	/**
	 * 密码查询对象
	 */
	private String password;
	private String passwordFuzzy;

	/**
	 * 个性签名查询对象
	 */
	private String personalSignature;
	private String personalSignatureFuzzy;

	/**
	 * 状态查询对象
	 */
	private Integer status;

	/**
	 * 创建时间查询对象
	 */
	private Date createTime;
	private String createTimeStart;
	private String createTimeEnd;

	/**
	 * 最后登录时间查询对象
	 */
	private Date lastLoginTime;
	private String lastLoginTimeStart;
	private String lastLoginTimeEnd;

	/**
	 * 地区查询对象
	 */
	private String areaName;
	private String areaNameFuzzy;

	/**
	 * 地区编号查询对象
	 */
	private String areaCode;
	private String areaCodeFuzzy;

	/**
	 * 最后离开时间查询对象
	 */
	private Long lastOffTime;

	public void setUserIdFuzzy(String userIdFuzzy) { this.userIdFuzzy = userIdFuzzy; }
	public String getUserIdFuzzy() { return userIdFuzzy; }
	public void setEmailFuzzy(String emailFuzzy) { this.emailFuzzy = emailFuzzy; }
	public String getEmailFuzzy() { return emailFuzzy; }
	public void setNickNameFuzzy(String nickNameFuzzy) { this.nickNameFuzzy = nickNameFuzzy; }
	public String getNickNameFuzzy() { return nickNameFuzzy; }
	public void setPasswordFuzzy(String passwordFuzzy) { this.passwordFuzzy = passwordFuzzy; }
	public String getPasswordFuzzy() { return passwordFuzzy; }
	public void setPersonalSignatureFuzzy(String personalSignatureFuzzy) { this.personalSignatureFuzzy = personalSignatureFuzzy; }
	public String getPersonalSignatureFuzzy() { return personalSignatureFuzzy; }
	public void setCreateTimeStart(String createTimeStart) { this.createTimeStart = createTimeStart; }
	public String getCreateTimeStart() { return createTimeStart; }
	public void setCreateTimeEnd(String createTimeEnd) { this.createTimeEnd = createTimeEnd; }
	public String getCreateTimeEnd() { return createTimeEnd; }
	public void setLastLoginTimeStart(String lastLoginTimeStart) { this.lastLoginTimeStart = lastLoginTimeStart; }
	public String getLastLoginTimeStart() { return lastLoginTimeStart; }
	public void setLastLoginTimeEnd(String lastLoginTimeEnd) { this.lastLoginTimeEnd = lastLoginTimeEnd; }
	public String getLastLoginTimeEnd() { return lastLoginTimeEnd; }
	public void setAreaNameFuzzy(String areaNameFuzzy) { this.areaNameFuzzy = areaNameFuzzy; }
	public String getAreaNameFuzzy() { return areaNameFuzzy; }
	public void setAreaCodeFuzzy(String areaCodeFuzzy) { this.areaCodeFuzzy = areaCodeFuzzy; }
	public String getAreaCodeFuzzy() { return areaCodeFuzzy; }
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
}