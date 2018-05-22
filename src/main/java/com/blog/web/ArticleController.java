package com.blog.web;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.blog.common.Page;
import com.blog.common.Response;
import com.blog.entity.Article;
import com.blog.service.ArticleService;

@RestController
@RequestMapping(value = "/article")
public class ArticleController {
    @Autowired
    ArticleService articleService;

    @RequestMapping(value = "/save", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
    public Response save() {
        return Response.ok();
    }

    @RequestMapping(value = "/detail/{id}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public Response detail(@PathVariable("id") Integer id) {
        return Response.ok();
    }

    @RequestMapping(value = "/list/{curPage}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public Response list(HttpServletRequest request, @PathVariable("curPage") Integer curPage) {
        Page page = Page.getPage(curPage);
        String search = request.getParameter("search");
        List<Article> result = articleService.pageList(page);
        return Response.ok(result);
    }

}
