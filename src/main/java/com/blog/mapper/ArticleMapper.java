package com.blog.mapper;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.blog.entity.Article;
import com.blog.entity.ArticleQuery;

@Repository
public interface ArticleMapper {
	int deleteByPrimaryKey(Integer id);

	int insert(Article record);

	int insertSelective(Article record);

	Article selectByPrimaryKey(Integer id);

	int updateByPrimaryKeySelective(Article record);

	int updateByPrimaryKey(Article record);

	List<Article> pageList(ArticleQuery query);

	/**
	 * 保存内容
	 * 
	 * @param record
	 * @return
	 */
	int saveContent(Article record);

	/**
	 * 更新内容
	 * 
	 * @param record
	 * @return
	 */
	int updateContent(Article record);

	Article queryContentById(Integer id);

	void updateReadCount(Integer id);
}