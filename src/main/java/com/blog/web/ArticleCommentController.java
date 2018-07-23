package com.blog.web;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
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
		for (ArticleComment articleComment : result) {
			String[] elements = articleComment.getUserIp().split("\\.");
			String star = "*";
			String e1 = "";
			String e2 = "";
			for (int i = 0; i < elements[1].length(); i++) {
				e1 += star;
			}
			for (int i = 0; i < elements[2].length(); i++) {
				e2 += star;
			}
			elements[1] = e1;
			elements[2] = e2;
			String ip = StringUtils.join(elements, ".");
			articleComment.setUserIp(ip);
		}
		return Response.ok(result);
	}
}
