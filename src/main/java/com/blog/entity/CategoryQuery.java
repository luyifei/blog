package com.blog.entity;

import com.blog.common.Page;

public class CategoryQuery {
    private Integer id;

    private Integer pid;

    private Integer level;
    


    public CategoryQuery() {

    }

    public CategoryQuery(Integer level) {
        this.level = level;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    

}