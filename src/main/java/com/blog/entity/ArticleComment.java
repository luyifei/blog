package com.blog.entity;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import com.blog.common.TimeUtils;

public class ArticleComment {
	private Integer id;

	private Integer articleId;

	private String userIp;

	private String content;

	private Integer pid;

	private LocalDateTime createTime;

	private String createTimeStr;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getArticleId() {
		return articleId;
	}

	public void setArticleId(Integer articleId) {
		this.articleId = articleId;
	}

	public String getUserIp() {
		return userIp;
	}

	public void setUserIp(String userIp) {
		this.userIp = userIp == null ? null : userIp.trim();
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content == null ? null : content.trim();
	}

	public Integer getPid() {
		return pid;
	}

	public void setPid(Integer pid) {
		this.pid = pid;
	}

	public LocalDateTime getCreateTime() {
		return createTime;
	}

	public void setCreateTime(LocalDateTime createTime) {
		this.createTime = createTime;
	}

	public String getCreateTimeStr() {
		if (createTime != null) {
			return TimeUtils.formate2(createTime);
		}
		return createTimeStr;
	}

}