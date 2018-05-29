package com.blog.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blog.entity.Category;
import com.blog.entity.CategoryQuery;
import com.blog.entity.Select2Optgroup;
import com.blog.entity.Select2Option;
import com.blog.mapper.CategoryMapper;

@Service
public class CategoryService {
    @Autowired
    CategoryMapper categoryMapper;

    public List<Category> categoryRelationList() {
        // 查询一级菜单
        List<Category> level1 = categoryMapper.queryBySelective(new CategoryQuery(1));
        // 查询二级菜单
        List<Category> level2 = categoryMapper.queryBySelective(new CategoryQuery(2));
        // 查询三级菜单
        List<Category> level3 = categoryMapper.queryBySelective(new CategoryQuery(3));
        sortOut(level1, level2);
        sortOut(level2, level3);
        return level1;
    }

    public List<Select2Optgroup> select2() {
        // 查询二级菜单
        List<Category> level2 = categoryMapper.queryBySelective(new CategoryQuery(2));
        // 查询三级菜单
        List<Category> level3 = categoryMapper.queryBySelective(new CategoryQuery(3));

        List<Select2Optgroup> groupList = new ArrayList<Select2Optgroup>();
        for (Category superior : level2) {
            Select2Optgroup optgroup = new Select2Optgroup();
            List<Select2Option> optionList = new ArrayList<Select2Option>();
            for (Category subordinate : level3) {
                if (superior.getId().equals(subordinate.getPid())) {
                    Select2Option option = new Select2Option();
                    option.setId(subordinate.getId());
                    option.setText(subordinate.getName());
                    optionList.add(option);
                }
            }
            if (optionList.isEmpty()) {
                continue;
            }
            optgroup.setText(superior.getName());
            optgroup.setChildren(optionList);
            groupList.add(optgroup);
        }
        return groupList;
    }

    private void sortOut(List<Category> superiors, List<Category> subordinates) {
        for (Category superior : superiors) {
            List<Category> subs = new ArrayList<>();
            for (Category subordinate : subordinates) {
                if (superior.getId().equals(subordinate.getPid())) {
                    subs.add(subordinate);
                }
            }
            superior.setSubordinates(subs);
        }
    }
}
