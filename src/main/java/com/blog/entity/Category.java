package com.blog.entity;

import java.util.List;

public class Category {
    private Integer id;

    private String name;

    private Integer pid;

    private Integer level;

    private Integer sort;
    
    private List<Category> subordinates;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public List<Category> getSubordinates() {
        return subordinates;
    }

    public void setSubordinates(List<Category> subordinates) {
        this.subordinates = subordinates;
    }

    
    
}