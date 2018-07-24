package com.blog.entity;

import java.time.LocalDateTime;
import java.util.Arrays;

import com.blog.common.TimeUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Article {
	private Integer id;

	private String title;

	private Integer categoryId;

	private Integer readCount;

	private String summary;
	@JsonIgnore
	private LocalDateTime createTime;

	@JsonIgnore
	private byte[] content;

	private String contentStr;

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

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	public String getContentStr() {
		if (content != null) {
			contentStr = new String(content);
		}
		return contentStr;
	}

	public String getCreateTimeStr() {
		if (createTime != null) {
			createTimeStr = TimeUtils.formate(createTime);
		}
		return createTimeStr;
	}

    @Override
    public String toString() {
        return "Article [id=" + id + ", title=" + title + ", categoryId=" + categoryId + ", readCount=" + readCount
                + ", summary=" + summary + ", createTime=" + createTime + ", content=" + Arrays.toString(content)
                + ", contentStr=" + contentStr + ", createTimeStr=" + createTimeStr + "]";
    }
	
	

}