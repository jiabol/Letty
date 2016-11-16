package com.jiabo.letty.servlet.container;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class LettyHttpServletRequest extends HttpServletRequestWrapper {

	public LettyHttpServletRequestWrapper(HttpServletRequest request) {
		super(request);
	}

}