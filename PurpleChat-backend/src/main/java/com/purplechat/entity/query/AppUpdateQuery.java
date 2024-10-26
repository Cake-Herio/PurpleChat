package com.purplechat.entity.query;

import java.util.Date;

/**
 * @Description:app发布查询对象
 * @Author:文翔
 * @date:2024/07/26
 */
public class AppUpdateQuery extends BaseQuery{
	/**
	 * 自增ID查询对象
	 */
	private Integer id;

	/**
	 * 版本号查询对象
	 */
	private String version;
	private String versionFuzzy;

	/**
	 * 更新描述查询对象
	 */
	private String updateDesc;
	private String updateDescFuzzy;

	/**
	 * 创建时间查询对象
	 */
	private Date createTime;
	private String createTimeStart;
	private String createTimeEnd;

	/**
	 * 0:灰度发布 2:全网发布查询对象
	 */
	private Integer status;

	/**
	 * 灰度uid查询对象
	 */
	private String grayscaleUid;
	private String grayscaleUidFuzzy;

	/**
	 * 文件类型 0:本地文件 1:外链查询对象
	 */
	private Integer fileType;

	/**
	 * 外链地址查询对象
	 */
	private String outerLink;
	private String outerLinkFuzzy;

	public void setVersionFuzzy(String versionFuzzy) { this.versionFuzzy = versionFuzzy; }
	public String getVersionFuzzy() { return versionFuzzy; }
	public void setUpdateDescFuzzy(String updateDescFuzzy) { this.updateDescFuzzy = updateDescFuzzy; }
	public String getUpdateDescFuzzy() { return updateDescFuzzy; }
	public void setCreateTimeStart(String createTimeStart) { this.createTimeStart = createTimeStart; }
	public String getCreateTimeStart() { return createTimeStart; }
	public void setCreateTimeEnd(String createTimeEnd) { this.createTimeEnd = createTimeEnd; }
	public String getCreateTimeEnd() { return createTimeEnd; }
	public void setGrayscaleUidFuzzy(String grayscaleUidFuzzy) { this.grayscaleUidFuzzy = grayscaleUidFuzzy; }
	public String getGrayscaleUidFuzzy() { return grayscaleUidFuzzy; }
	public void setOuterLinkFuzzy(String outerLinkFuzzy) { this.outerLinkFuzzy = outerLinkFuzzy; }
	public String getOuterLinkFuzzy() { return outerLinkFuzzy; }
	public void setId(Integer id) { this.id = id; }
	public Integer getId() { return id; }
	public void setVersion(String version) { this.version = version; }
	public String getVersion() { return version; }
	public void setUpdateDesc(String updateDesc) { this.updateDesc = updateDesc; }
	public String getUpdateDesc() { return updateDesc; }
	public void setCreateTime(Date createTime) { this.createTime = createTime; }
	public Date getCreateTime() { return createTime; }
	public void setStatus(Integer status) { this.status = status; }
	public Integer getStatus() { return status; }
	public void setGrayscaleUid(String grayscaleUid) { this.grayscaleUid = grayscaleUid; }
	public String getGrayscaleUid() { return grayscaleUid; }
	public void setFileType(Integer fileType) { this.fileType = fileType; }
	public Integer getFileType() { return fileType; }
	public void setOuterLink(String outerLink) { this.outerLink = outerLink; }
	public String getOuterLink() { return outerLink; }
}