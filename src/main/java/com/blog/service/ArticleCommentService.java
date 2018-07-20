package com.blog.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blog.entity.ArticleComment;
import com.blog.mapper.ArticleCommentMapper;

@Service
public class ArticleCommentService {
	@Autowired
	ArticleCommentMapper articleCommentMapper;

	public void saveArticleComment(ArticleComment articleComment) {
		articleCommentMapper.insertSelective(articleComment);
	}

	public List<ArticleComment> queryListByArticleId(Integer articleId) {
		return articleCommentMapper.queryListByArticleId(articleId);
	}

	public Integer countComment(Integer articleId) {
		return articleCommentMapper.countComment(articleId);
	}
}
