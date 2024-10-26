package com.purplechat.entity.po;

import java.io.Serializable;
import java.util.Date;

import com.purplechat.utils.StringTools;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import com.purplechat.utils.DateUtils;
import com.purplechat.enums.DateTimePatternEnum;

/**
 * @Description:app发布
 * @Author:文翔
 * @date:2024/07/26
 */
public class AppUpdate implements Serializable {
	/**
	 * 自增ID
	 */
	private Integer id;

	/**
	 * 版本号
	 */
	private String version;

	/**
	 * 更新描述
	 */
	private String updateDesc;

	private String[] updateDescArray;

	public String[] getUpdateDescArray() {

		if(!StringTools.isEmpty(updateDesc)){
			return updateDesc.split("\\|");
		}


		return updateDescArray;
	}

	public void setUpdateDescArray(String[] updateDescArray) {
		this.updateDescArray = updateDescArray;
	}

	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH-mm-ss", timezone = "GMT+8")
	@DateTimeFormat (pattern = "yyyy-MM-dd HH-mm-ss")
	private Date createTime;

	/**
	 * 0:灰度发布 2:全网发布
	 */
	private Integer status;

	/**
	 * 灰度uid
	 */
	private String grayscaleUid;

	/**
	 * 文件类型 0:本地文件 1:外链
	 */
	private Integer fileType;

	/**
	 * 外链地址
	 */
	private String outerLink;

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
	@Override
	public String toString(){
		return "自增ID:" + (id == null ? "null" : id) + ", " + "版本号:" + (version == null ? "null" : version) + ", " + "更新描述:" + (updateDesc == null ? "null" : updateDesc) + ", " + "创建时间:" + (DateUtils.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()) == null ? "null" : DateUtils.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + ", " + "0:灰度发布 2:全网发布:" + (status == null ? "null" : status) + ", " + "灰度uid:" + (grayscaleUid == null ? "null" : grayscaleUid) + ", " + "文件类型 0:本地文件 1:外链:" + (fileType == null ? "null" : fileType) + ", " + "外链地址:" + (outerLink == null ? "null" : outerLink);
	}
}