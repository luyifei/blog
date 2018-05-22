package com.blog.common;

public class Page {
    private Integer curPage = 1;
    private Integer pageSize = 5;
    private Integer pageIndex;

    public static Page getPage(Integer curPage) {
        Page page = new Page();
        page.setCurPage(curPage);
        Integer pageSize = 5;
        page.setPageIndex((curPage - 1) * pageSize);
        return page;
    }

    public Integer getCurPage() {
        return curPage;
    }

    public void setCurPage(Integer curPage) {
        this.curPage = curPage;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(Integer pageIndex) {
        this.pageIndex = pageIndex;
    }

}
