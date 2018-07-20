package com.blog.mapper;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.blog.entity.ArticleComment;

@Repository
public interface ArticleCommentMapper {
	int deleteByPrimaryKey(Integer id);

	int insert(ArticleComment record);

	int insertSelective(ArticleComment record);

	ArticleComment selectByPrimaryKey(Integer id);

	int updateByPrimaryKeySelective(ArticleComment record);

	int updateByPrimaryKey(ArticleComment record);

	/**
	 * 根据文章ID查询评论
	 * 
	 * @param articleId
	 * @return
	 */
	List<ArticleComment> queryListByArticleId(Integer articleId);

	/**
	 * 统计评论数量
	 * 
	 * @param articleId
	 * @return
	 */
	Integer countComment(Integer articleId);

}