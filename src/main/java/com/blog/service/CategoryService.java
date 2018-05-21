package com.blog.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blog.entity.Category;
import com.blog.entity.CategoryQuery;
import com.blog.mapper.CategoryMapper;

@Service
public class CategoryService {
    @Autowired
    CategoryMapper categoryMapper;

    public List<Category> categoryRelationList() {
        //查询一级菜单
        List<Category> level1 = categoryMapper.queryBySelective(new CategoryQuery(1));
        //查询二级菜单
        List<Category> level2 = categoryMapper.queryBySelective(new CategoryQuery(2));
        //查询三级菜单
        List<Category> level3 = categoryMapper.queryBySelective(new CategoryQuery(3));
        sortOut(level1, level2);
        sortOut(level2, level3);
        return level1;
    }

    private void sortOut(List<Category> superiors,List<Category> subordinates){
        for (Category superior : superiors) {
            List<Category> subs = new ArrayList<>(); 
            for (Category subordinate : subordinates) {
                if(superior.getId().equals(subordinate.getPid())){
                    subs.add(subordinate);
                }
            }
            superior.setSubordinates(subs);
        }
    }
}
