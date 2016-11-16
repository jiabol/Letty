package com.jiabo.letty.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface HandlerInterceptorAdapter {
	boolean postHandle(HttpServletRequest request, HttpServletResponse response);

	boolean afterHandle(HttpServletRequest request, HttpServletResponse response);
}
