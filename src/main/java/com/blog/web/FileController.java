package com.blog.web;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.blog.service.CommonFileService;

@RestController
@RequestMapping("/file")
public class FileController {
	@Autowired
	CommonFileService commonFileService;

	@RequestMapping("/imageUpload")
	public Map<String, Object> imageUpload(MultipartHttpServletRequest request, HttpServletResponse response) {
		Iterator<String> item = request.getFileNames();
		Map<String, Object> map = new HashMap<>();
		while (item.hasNext()) {
			String fileName = item.next();
			MultipartFile file = request.getFile(fileName);
			commonFileService.saveImage(file, map);
		}
		map.put("error", 0);
		return map;
	}
}
