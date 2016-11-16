package com.jiabo.letty.mvc;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * the class for the detail application to implement.
 * 
 * @author jialong
 *
 */
public abstract class Application {

	protected Properties props;
	private ViewResolver viewResolver;
	// key reg expression
	private Map<String, List<ControllerWrapper>> urlMapping;
	private List<HandlerInterceptorAdapter> interceptor;

	/**
	 * get application name
	 * 
	 * @return name
	 */
	public abstract String getAppName();

	/**
	 * application init
	 */
	public abstract void start();

	public List<HandlerInterceptorAdapter> getInterceptor() {
		return interceptor;
	}

	public void setInterceptor(List<HandlerInterceptorAdapter> interceptor) {
		this.interceptor = interceptor;
	}

	public ViewResolver getViewResolver() {
		return viewResolver;
	}

	public void setViewResolver(ViewResolver viewResolver) {
		this.viewResolver = viewResolver;
	}

	public Map<String, List<ControllerWrapper>> getUrlMapping() {
		return urlMapping;
	}

	public void setUrlMapping(Map<String, List<ControllerWrapper>> urlMapping) {
		this.urlMapping = urlMapping;
	}
}
