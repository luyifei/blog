package com.blog.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blog.entity.AccessLog;
import com.blog.mapper.AccessLogMapper;

@Service
public class AccessLogService {
	@Autowired
	AccessLogMapper accessLogMapper;

	/**
	 * 保存系统访问日志
	 * 
	 * @param log
	 */
	public void saveLog(AccessLog log) {
		accessLogMapper.insertSelective(log);
	}
}
