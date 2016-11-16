package com.jiabo.letty.mvc;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ViewResolver {
	void init(ServletContext context);

	void resolve(HttpServletRequest hreq, HttpServletResponse hres, String path);
}
