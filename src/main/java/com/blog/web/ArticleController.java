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

	/**
	 * 保存文章
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/save", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
	public Response save(HttpServletRequest request) {
		String title = request.getParameter("title");
		String category = request.getParameter("categoryVal");
		String content = request.getParameter("content");
		String summary = content.replaceAll("</?[^>]+>", ""); // 剔出<html>的标签
		summary = summary.replaceAll("<a>\\s*|\t|\r|\n</a>", "");// 去除字符串中的
		Article article = new Article();
		article.setTitle(title);
		article.setSummary(summary);
		article.setContent(content.getBytes());
		article.setCategoryId(Integer.valueOf(category));
		article = articleService.saveArticle(article);
		return Response.ok(article.getId());
	}

	/**
	 * 详细内容
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/detail/{id}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
	public Response detail(@PathVariable("id") Integer id) {
		Article article = articleService.queryById(id);
		return Response.ok(article);
	}

	/**
	 * 文章列表查询
	 * 
	 * @param request
	 * @param curPage
	 * @param categoryId
	 * @return
	 */
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
