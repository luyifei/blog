package com.blog.entity;

public class FileConfig {
    private Integer id;

    private String prefixPath;

    private String suffixPath;

    private String module;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPrefixPath() {
        return prefixPath;
    }

    public void setPrefixPath(String prefixPath) {
        this.prefixPath = prefixPath == null ? null : prefixPath.trim();
    }

    public String getSuffixPath() {
        return suffixPath;
    }

    public void setSuffixPath(String suffixPath) {
        this.suffixPath = suffixPath == null ? null : suffixPath.trim();
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module == null ? null : module.trim();
    }
}