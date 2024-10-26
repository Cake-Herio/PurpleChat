package com.purplechat.entity.query;


/**
 * @Description:靓号表查询对象
 * @Author:文翔
 * @date:2024/06/10
 */
public class UserInfoBeautyQuery extends BaseQuery{
	/**
	 * 自增id查询对象
	 */
	private Integer id;

	/**
	 * 用户邮箱查询对象
	 */
	private String email;
	private String emailFuzzy;

	/**
	 * 用户id查询对象
	 */
	private String userId;
	private String userIdFuzzy;

	/**
	 * 0：未使用1：使用查询对象
	 */
	private Integer status;

	public void setEmailFuzzy(String emailFuzzy) { this.emailFuzzy = emailFuzzy; }
	public String getEmailFuzzy() { return emailFuzzy; }
	public void setUserIdFuzzy(String userIdFuzzy) { this.userIdFuzzy = userIdFuzzy; }
	public String getUserIdFuzzy() { return userIdFuzzy; }
	public void setId(Integer id) { this.id = id; }
	public Integer getId() { return id; }
	public void setEmail(String email) { this.email = email; }
	public String getEmail() { return email; }
	public void setUserId(String userId) { this.userId = userId; }
	public String getUserId() { return userId; }
	public void setStatus(Integer status) { this.status = status; }
	public Integer getStatus() { return status; }
}