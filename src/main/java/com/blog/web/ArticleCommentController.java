package com.blog.web;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.blog.common.CusAccessObjectUtil;
import com.blog.common.Response;
import com.blog.entity.ArticleComment;
import com.blog.service.ArticleCommentService;

@RestController
@RequestMapping(value = "/articleComment")
public class ArticleCommentController {
	@Autowired
	ArticleCommentService articleCommentService;

	@RequestMapping(value = "/save", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
	public Response save(HttpServletRequest request, HttpServletResponse response) {
		String ip = CusAccessObjectUtil.getIpAddress(request);
		String a = request.getParameter("articleId");
		Integer articleId = Integer.valueOf(request.getParameter("articleId"));
		String comment = request.getParameter("comment");
		ArticleComment articleComment = new ArticleComment();
		articleComment.setArticleId(articleId);
		articleComment.setContent(comment);
		articleComment.setUserIp(ip);
		articleComment.setCreateTime(LocalDateTime.now());
		articleCommentService.saveArticleComment(articleComment);
		Integer commentCount = articleCommentService.countComment(articleId);
		Map<String, Object> result = new HashMap<>();
		result.put("commentCount", commentCount);
		result.put("articleComment", articleComment);
		return Response.ok(result);
	}

	@RequestMapping(value = "/list/{articleId}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
	public Response list(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("articleId") Integer articleId) {
		List<ArticleComment> result = articleCommentService.queryListByArticleId(articleId);
		return Response.ok(result);
	}
}
