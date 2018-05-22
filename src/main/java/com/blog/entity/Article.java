package com.blog.entity;

import java.time.LocalDateTime;
import java.util.Date;

import com.blog.common.TimeUtils;

public class Article {
	private Integer id;

	private String title;

	private Integer categoryId;

	private Integer readCount;

	private String summary;

	private LocalDateTime createTime;

	private String createTimeStr;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title == null ? null : title.trim();
	}

	public Integer getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}

	public Integer getReadCount() {
		return readCount;
	}

	public void setReadCount(Integer readCount) {
		this.readCount = readCount;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary == null ? null : summary.trim();
	}

	public LocalDateTime getCreateTime() {
		return createTime;
	}

	public void setCreateTime(LocalDateTime createTime) {
		this.createTime = createTime;
	}

	public String getCreateTimeStr() {
		if (createTime != null) {
			return TimeUtils.formate(createTime);
		}
		return "";
	}

}