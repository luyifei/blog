package com.blog.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blog.common.Page;
import com.blog.entity.Article;
import com.blog.mapper.ArticleMapper;

@Service
public class ArticleService {
    @Autowired
    ArticleMapper articleMapper;
    public List<Article> pageList(Page page){
        return articleMapper.pageList(page);
    }
}
