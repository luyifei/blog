package com.blog.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.blog.common.FileConfigModelEnum;
import com.blog.common.LocalDateTimeUtils;
import com.blog.entity.CommonFile;
import com.blog.entity.FileConfig;
import com.blog.mapper.CommonFileMapper;
import com.google.common.io.ByteStreams;

@Service
public class CommonFileService {
	@Autowired
	CommonFileMapper commonFileMapper;

	public void saveImage(MultipartFile file, Map<String, Object> map) {

		CommonFile commonFile = new CommonFile();
		FileConfig fileConfig = commonFileMapper.getFileConfigByModel(FileConfigModelEnum.IMAGE.getModel());
		String filePath = fileConfig.getPrefixPath() + fileConfig.getSuffixPath() + File.separator
				+ LocalDateTimeUtils.formatTime(LocalDateTime.now(), "yyyyMM");
		String ext = file.getOriginalFilename().split("\\.")[1];
		String systemFileName = LocalDateTimeUtils.getMilliByTime(LocalDateTime.now()).toString() + "." + ext;
		File folder = new File(filePath);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		File uploadedFile = new File(filePath, systemFileName);
		commonFile.setOriginalFileName(file.getOriginalFilename());
		commonFile.setSystemFileName(systemFileName);
		commonFile.setFilePath(filePath);
		commonFile.setType(1);
		try {
			ByteStreams.copy(file.getInputStream(), new FileOutputStream(uploadedFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		map.put("url", uploadedFile);
	}
}