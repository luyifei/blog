package com.blog.mapper;

import org.springframework.stereotype.Repository;

import com.blog.entity.AccessLog;

@Repository
public interface AccessLogMapper {
	int deleteByPrimaryKey(Integer id);

	int insert(AccessLog record);

	int insertSelective(AccessLog record);

	AccessLog selectByPrimaryKey(Integer id);

	int updateByPrimaryKeySelective(AccessLog record);

	int updateByPrimaryKey(AccessLog record);
}