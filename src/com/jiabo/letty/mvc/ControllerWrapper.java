package com.jiabo.letty.mvc;

import java.lang.reflect.Method;
import java.util.List;

public class ControllerWrapper {
	private Method method;
	private Class<?> controller;
	private List<String> pathVars;
	private Application application;
	private boolean isREST;
	private String httpMethod;
	private String regUrl;

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public Class<?> getController() {
		return controller;
	}

	public void setController(Class<?> controller) {
		this.controller = controller;
	}

	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	public boolean isREST() {
		return isREST;
	}

	public void setREST(boolean isREST) {
		this.isREST = isREST;
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	public List<String> getPathVars() {
		return pathVars;
	}

	public void setPathVars(List<String> pathVars) {
		this.pathVars = pathVars;
	}

	public String getRegUrl() {
		return regUrl;
	}

	public void setRegUrl(String regUrl) {
		this.regUrl = regUrl;
	}

}
