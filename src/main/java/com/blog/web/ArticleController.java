package com.blog.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.blog.common.Response;

@RestController
@RequestMapping(value = "/article")
public class ArticleController {
    @RequestMapping(value = "/save", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
    public Response save() {
        return Response.ok();
    }

    @RequestMapping(value = "/detail/{id}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public Response detail(@PathVariable("id") Integer id) {
        return Response.ok();
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public Response list(HttpServletRequest request) {
        String search = request.getParameter("search");
        return Response.ok(search);
    }

}
