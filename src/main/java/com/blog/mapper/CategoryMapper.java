package com.blog.mapper;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.blog.entity.Category;
import com.blog.entity.CategoryQuery;
@Repository
public interface CategoryMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Category record);

    int insertSelective(Category record);

    Category selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Category record);

    int updateByPrimaryKey(Category record);
    
    List<Category> queryBySelective(CategoryQuery record);
}