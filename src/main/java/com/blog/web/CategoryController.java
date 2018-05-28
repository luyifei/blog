package com.blog.web;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.blog.common.Response;
import com.blog.entity.Category;
import com.blog.entity.Select2Optgroup;
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

    @RequestMapping(value = "/list/select2", method = RequestMethod.GET)
    public Response select2(HttpServletRequest request) {
        String id = request.getParameter("id");
        System.out.println(id);
        List<Select2Optgroup> optgroup = categoryService.select2();
        String s = "[{\"text\": \"Alaskan/Hawaiian Time Zone\", \"children\" : [{id: \"AK\", text: 'Alaska'},{id: \"HI\", text: 'Hawaii'}]},{\"text\": \"abc12345\", \"children\" : [{id: \"AK\", text: 'Alaska'},{id: \"HI\", text: 'Hawaii'}]}]";
        String s2 = "[{\"children\":\"[{\"id\":8,\"text\":\"Java基础\"}]\",\"text\":\"Java\"},{\"children\":[{\"id\":11,\"text\":\"SpringMVC\"}],\"text\":\"Spiring\"}]";

        System.out.println(s2);
        return Response.ok(optgroup);

    }
}
