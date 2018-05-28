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
import com.blog.entity.ArticleQuery;
import com.blog.service.ArticleService;

@RestController
@RequestMapping(value = "/article")
public class ArticleController {
	@Autowired
	ArticleService articleService;

	@RequestMapping(value = "/save", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
	public Response save(HttpServletRequest request) {
		String category = request.getParameter("category");
		String content = request.getParameter("content");
		String summary = content.replaceAll("</?[^>]+>", ""); // 剔出<html>的标签
		summary = summary.replaceAll("<a>\\s*|\t|\r|\n</a>", "");// 去除字符串中的
		Article article = new Article();
		article.setSummary(summary);
		article.setContent(content);
		article.setCategoryId(Integer.valueOf(category));
		article = articleService.saveArticle(article);
		return Response.ok(article.getId());
	}

	@RequestMapping(value = "/detail/{id}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
	public Response detail(@PathVariable("id") Integer id) {
		return Response.ok();
	}

	@RequestMapping(value = "/list/{curPage}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
	public Response list(HttpServletRequest request, @PathVariable("curPage") Integer curPage, Integer categoryId) {
		Page page = Page.getPage(curPage);
		String search = request.getParameter("search");
		ArticleQuery query = new ArticleQuery();
		query.setTitle(search);
		query.setPage(page);
		query.setCategoryId(categoryId);
		List<Article> result = articleService.pageList(query);
		return Response.ok(result);
	}

}
