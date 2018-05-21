package com.blog.web;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.blog.common.Response;
import com.blog.entity.Category;
import com.blog.service.CategoryService;

@RestController
@RequestMapping(value = "/category")
public class CategoryController {

    @Autowired
    CategoryService categoryService;

    @RequestMapping(value = "/list/relation", method = RequestMethod.GET)
    public Response relation(HttpServletRequest request) {
        List<Category> result = categoryService.categoryRelationList();
        return Response.ok(result);
    }
}
