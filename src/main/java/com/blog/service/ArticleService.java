package com.blog.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blog.entity.Article;
import com.blog.entity.ArticleQuery;
import com.blog.mapper.ArticleMapper;

@Service
public class ArticleService {
    @Autowired
    ArticleMapper articleMapper;

    public List<Article> pageList(ArticleQuery query) {
        return articleMapper.pageList(query);
    }

    public Article saveArticle(Article article) {
        articleMapper.insert(article);
        articleMapper.saveContent(article);
        return article;
    }

    public Article queryById(Integer id) {
        return articleMapper.queryContentById(id);
    }
    
    public void updateReadCount(Integer id){
    	articleMapper.updateReadCount(id);
    }
}
