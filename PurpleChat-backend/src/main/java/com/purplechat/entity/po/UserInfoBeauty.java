package com.purplechat.entity.po;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
/**
 * @Description:靓号表
 * @Author:文翔
 * @date:2024/06/10
 */
public class UserInfoBeauty implements Serializable {
	/**
	 * 自增id
	 */
	@JsonIgnore
	private Integer id;

	/**
	 * 用户邮箱
	 */
	private String email;

	/**
	 * 用户id
	 */
	private String userId;

	/**
	 * 0：未使用1：使用
	 */
	private Integer status;

	public void setId(Integer id) { this.id = id; }
	public Integer getId() { return id; }
	public void setEmail(String email) { this.email = email; }
	public String getEmail() { return email; }
	public void setUserId(String userId) { this.userId = userId; }
	public String getUserId() { return userId; }
	public void setStatus(Integer status) { this.status = status; }
	public Integer getStatus() { return status; }
	@Override
	public String toString(){
		return "自增id:" + (id == null ? "null" : id) + ", " + "用户邮箱:" + (email == null ? "null" : email) + ", " + "用户id:" + (userId == null ? "null" : userId) + ", " + "0：未使用1：使用:" + (status == null ? "null" : status);
	}
}