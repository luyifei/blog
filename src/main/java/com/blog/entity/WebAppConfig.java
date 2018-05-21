package com.blog.entity;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration // 必须存在
@EnableSwagger2 // 必须存在
@EnableWebMvc // 必须存在
@ComponentScan(basePackages = { "com.blog.web" }) // 必须存在 扫描的API Controller
													// package name 也可以直接扫描class
													// (basePackageClasses)
public class WebAppConfig {
	@Bean
	public Docket customDocket() {
		//
		return new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo());
	}

	private ApiInfo apiInfo() {
		Contact contact = new Contact("卢义飞", "", "abc@qq.com");
		return new ApiInfo("卢义飞的个人站", // 大标题 title
				"API接口列表", // 小标题
				"0.0.3", // 版本
				"", // termsOfServiceUrl
				contact, // 作者
				"Copyright © 2018  卢义飞的个人站点", // 链接显示文字
				""// 网站链接
		);
	}
}