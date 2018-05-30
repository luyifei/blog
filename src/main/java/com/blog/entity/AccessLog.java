package com.blog.entity;

import java.time.LocalDateTime;

public class AccessLog {
	private Integer id;

	private String ip;

	private String url;

	private String param;

	private LocalDateTime createTime;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip == null ? null : ip.trim();
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url == null ? null : url.trim();
	}

	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param == null ? null : param.trim();
	}

	public LocalDateTime getCreateTime() {
		return createTime;
	}

	public void setCreateTime(LocalDateTime createTime) {
		this.createTime = createTime;
	}

}