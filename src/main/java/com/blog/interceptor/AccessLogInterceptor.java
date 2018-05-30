package com.blog.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.blog.common.CusAccessObjectUtil;
import com.blog.entity.AccessLog;
import com.blog.service.AccessLogService;

public class AccessLogInterceptor implements HandlerInterceptor {

	@Autowired
	AccessLogService accessLogService;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		String uri = request.getRequestURI();
		String contextPath = request.getContextPath();// 工程路径

		AccessLog log = new AccessLog();
		log.setUrl(uri.substring(contextPath.length()));
		log.setIp(CusAccessObjectUtil.getIpAddress(request));//获取用户IP地址
		log.setParam(request.getQueryString());// 参数部分
		accessLogService.saveLog(log);
	}

}
