package com.blog.mapper;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.blog.common.Page;
import com.blog.entity.Article;

@Repository
public interface ArticleMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Article record);

    int insertSelective(Article record);

    Article selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Article record);

    int updateByPrimaryKey(Article record);

    List<Article> pageList(Page page);
}