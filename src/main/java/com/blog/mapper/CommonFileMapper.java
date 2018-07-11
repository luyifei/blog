package com.blog.mapper;

import org.springframework.stereotype.Repository;

import com.blog.entity.CommonFile;
import com.blog.entity.FileConfig;

@Repository
public interface CommonFileMapper {
	int deleteByPrimaryKey(Integer id);

	int insert(CommonFile record);

	int insertSelective(CommonFile record);

	CommonFile selectByPrimaryKey(Integer id);

	int updateByPrimaryKeySelective(CommonFile record);

	int updateByPrimaryKeyWithBLOBs(CommonFile record);

	int updateByPrimaryKey(CommonFile record);

	FileConfig getFileConfigByModel(String model);
}