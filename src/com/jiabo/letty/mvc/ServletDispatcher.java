package com.jiabo.letty.mvc;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jiabo.letty.mvc.annoation.ResponseBody;
import com.jiabo.letty.mvc.util.Constant;
import com.jiabo.letty.mvc.util.DateTimeUtil;
import com.jiabo.letty.mvc.util.FrameworkUtil;
import com.jiabo.letty.mvc.util.IOUtil;

public class ServletDispatcher extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1095192250678362142L;
	private final Logger log = LoggerFactory.getLogger(ServletDispatcher.class);
	private static ApplicationContext ac;

	static ApplicationContext getApplicationContext() {
		return ac;
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		ac = new ApplicationContext(config);
		ac.init();
	}

	@Override
	public void service(ServletRequest req, ServletResponse res)
			throws ServletException, IOException {
		HttpServletRequest hreq = (HttpServletRequest) req;
		HttpServletResponse hres = (HttpServletResponse) res;
		hreq.setCharacterEncoding("utf-8");
		hres.setCharacterEncoding("utf-8");
		try {
			ControllerWrapper cw = ac.getControllerWrapper(getURL(hreq),
					hreq.getMethod());
			handleRequest(hreq, hres, cw);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			log.error("invoke error", e);
		} catch (ContainerException e) {
			log.error("request ip:" + hreq.getRemoteAddr() + " "
					+ e.getMessage());
		}
	}

	private void handleRequest(HttpServletRequest hreq,
			HttpServletResponse hres, ControllerWrapper cw)
			throws IllegalAccessException, InvocationTargetException,
			IOException {
		hreq.setAttribute("application", cw.getApplication());
		for (HandlerInterceptorAdapter intercepotr : cw.getApplication()
				.getInterceptor()) {
			boolean result = intercepotr.postHandle(hreq, hres);
			if (!result)
				return;
		}
		Object[] args = adapterParams(cw, hreq, hres);
		Object obj = invoke(cw, args);
		for (HandlerInterceptorAdapter intercepotr : cw.getApplication()
				.getInterceptor()) {
			boolean result = intercepotr.afterHandle(hreq, hres);
			if (!result)
				return;
		}
		resolve(hreq, hres, cw, obj);
	}

	private String getURL(HttpServletRequest hreq) {
		return hreq.getRequestURI().substring(hreq.getContextPath().length());
	}

	private void resolve(HttpServletRequest hreq, HttpServletResponse hres,
			ControllerWrapper cw, Object obj) throws IOException {
		if (obj == null)
			return;
		if (cw.isREST()) {
			ServletOutputStream stream = hres.getOutputStream();
			stream.write(((String) obj).getBytes());
			stream.close();
			return;
		}
		if (cw.getMethod().getReturnType().equals(void.class))
			return;
		if (!cw.getMethod().getReturnType().equals(String.class)) {
			log.warn("method return type is not String or void,could not be resolved.");
			return;
		}
		String result = (String) obj;
		if (result.startsWith("redirect:")) {
			String url = result.split(":")[1].trim();
			if (!url.startsWith("/")) {
				url = "/" + url;
			}
			if (cw.getApplication().getAppName() != null
					&& !"".equals(cw.getApplication().getAppName().trim())) {
				url = "/" + cw.getApplication().getAppName() + url;
			}
			hres.setHeader("Location", url);
			hres.setStatus(303);
			return;
		}
		if (result.startsWith("forward:")) {
			String url = result.split(":")[1].trim();
			if (!url.startsWith("/")) {
				url = "/" + url;
			}
			ControllerWrapper forwardCW = ac.getControllerWrapper(url, null);
			try {
				handleRequest(hreq, hres, forwardCW);
			} catch (IllegalAccessException | InvocationTargetException e) {
				log.error("", e);
			}
			return;
		}
		if (cw.getMethod().isAnnotationPresent(ResponseBody.class)) {
			ServletOutputStream stream = hres.getOutputStream();
			stream.write(result.getBytes());
			stream.close();
		} else {
			resolve(result, hreq, hres, cw);
		}
	}

	private Object invoke(ControllerWrapper cw, Object[] args)
			throws IllegalAccessException, InvocationTargetException {
		Object obj = cw.getMethod().invoke(
				ac.getBeanByClass(cw.getController()), args);
		return obj;
	}

	private void resolve(String path, HttpServletRequest hreq,
			HttpServletResponse hres, ControllerWrapper cw) {
		ViewResolver vr = cw.getApplication().getViewResolver();
		vr.resolve(hreq, hres, path);
	}

	private Object[] adapterParams(ControllerWrapper cw,
			HttpServletRequest hreq, HttpServletResponse hres) {
		Map<String, String> paramMap = new HashMap<String, String>();
		if (hreq.getContentType() != null
				&& hreq.getContentType().contains("application/json")) {
			try {
				ServletInputStream stream = hreq.getInputStream();
				String str = IOUtil.readToString(stream);
				JSONObject obj = JSON.parseObject(str);
				if (obj != null) {
					for (Entry<String, Object> entry : obj.entrySet()) {
						paramMap.put(entry.getKey(), (String) entry.getValue());
					}
				}
			} catch (IOException e) {
				log.error("", e);
			}
		}
		Enumeration<String> names = hreq.getParameterNames();
		while (names.hasMoreElements()) {
			String key = names.nextElement();
			paramMap.put(key, hreq.getParameter(key));
		}
		Method method = cw.getMethod();
		Object[] params = new Object[method.getParameterTypes().length];
		if (params.length == 0)
			return params;
		int index = 0;
		addPathVars(hreq, cw, paramMap);
		for (Class<?> c : method.getParameterTypes()) {
			if (c.equals(int.class) || c.equals(Integer.class)) {
				params[index] = new Integer(paramMap.get(FrameworkUtil
						.getMethodParamsName(method, index)));
			} else if (c.equals(String.class)) {
				params[index] = paramMap.get(FrameworkUtil.getMethodParamsName(
						method, index));
			} else if (c.equals(HttpServletRequest.class)) {
				params[index] = hreq;
			} else if (c.equals(Date.class)) {
				String date = paramMap.get(FrameworkUtil.getMethodParamsName(
						method, index));
				if (date != null && !"".equals(date.trim())) {
					params[index] = DateTimeUtil.parseDate(date, null);
				}
			} else if (c.equals(HttpServletResponse.class)) {
				params[index] = hres;
			} else {
				Object obj = null;
				try {
					obj = c.newInstance();
				} catch (InstantiationException | IllegalAccessException e1) {
					log.error("", e1);
					continue;
				}
				for (Field f : c.getDeclaredFields()) {
					if (paramMap.containsKey(f.getName())) {
						try {
							f.setAccessible(true);
							String str = paramMap.get(f.getName());
							if (f.getType().equals(Integer.class)
									|| f.getType().equals(int.class)) {
								f.set(obj,
										str == null || "".equals(str.trim()) ? null
												: new Integer(str));
							} else if (f.getType().equals(Double.class)
									|| f.getType().equals(double.class)) {
								f.set(obj,
										str == null || "".equals(str.trim()) ? null
												: new Double(str));
							} else if (f.getType().equals(Date.class)) {
								f.set(obj, DateTimeUtil.parseDate(str, null));
							} else {
								f.set(obj, str);
							}
						} catch (IllegalAccessException e) {
							log.error("fill the params error", e);
							continue;
						}
					}
				}
				params[index] = obj;

			}
			index++;
		}
		return params;
	}

	private void addPathVars(HttpServletRequest hreq, ControllerWrapper cw,
			Map<String, String> paramsMap) {
		if (cw.isREST()) {
			Pattern pattern = Pattern.compile(cw.getRegUrl());
			Matcher matcher = pattern.matcher(FrameworkUtil.removeAppPrefix(
					getURL(hreq), cw.getApplication().getAppName()));
			if (matcher.find() && matcher.groupCount() > 1) {
				paramsMap.put(Constant.RESOURCE_ID, matcher.group(2));
			}
		} else {
			if (cw.getPathVars() == null || cw.getPathVars().isEmpty())
				return;
			String url = FrameworkUtil.removeAppPrefix(getURL(hreq), cw
					.getApplication().getAppName());
			Pattern pattern = Pattern.compile(cw.getRegUrl());
			Matcher matcher = pattern.matcher(url);
			if (matcher.find()) {
				List<String> vars = cw.getPathVars();
				for (int i = 1; i < matcher.groupCount(); i++) {
					paramsMap.put(vars.get(i - 1), matcher.group(i));
				}
			}
		}
	}
}
