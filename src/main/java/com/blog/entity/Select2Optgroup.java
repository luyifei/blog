package com.blog.entity;

import java.util.List;

public class Select2Optgroup {
    private String text;
    private List<Select2Option> children;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<Select2Option> getChildren() {
        return children;
    }

    public void setChildren(List<Select2Option> children) {
        this.children = children;
    }

}
