package com.jiabo.letty.mvc;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import javax.servlet.ServletConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiabo.letty.mvc.annoation.Autowired;
import com.jiabo.letty.mvc.annoation.Controller;
import com.jiabo.letty.mvc.annoation.RequestMapping;
import com.jiabo.letty.mvc.util.ClassUtil;
import com.jiabo.letty.mvc.util.Constant;
import com.jiabo.letty.mvc.util.FrameworkUtil;
import com.jiabo.letty.rest.annoation.DELETE;
import com.jiabo.letty.rest.annoation.GET;
import com.jiabo.letty.rest.annoation.POST;
import com.jiabo.letty.rest.annoation.PUT;
import com.jiabo.letty.rest.annoation.REST;

/**
 * the applicationContext of all the applications.
 * 
 * @author jialong
 * 
 */
public class ApplicationContext {

	private final Logger log = LoggerFactory
			.getLogger(ApplicationContext.class);
	private ServletConfig servletConfig;
	private Map<Class<?>, Object> beanFactory = new HashMap<Class<?>, Object>();
	private List<Application> runningApplications = new ArrayList<Application>();

	/**
	 * mapping the url with the controllerWrapper
	 * 
	 * @param url
	 * @param httpMethod
	 * @return the mapped controllerWrapper
	 */
	public ControllerWrapper getControllerWrapper(String url, String httpMethod) {
		if ("".equals(url))
			url = Constant.leftSplit;
		Application app = getApplicationByUrl(url);
		if (app == null)
			throw new ContainerException("no application found for " + url);
		url = FrameworkUtil.removeAppPrefix(url, app.getAppName());
		List<ControllerWrapper> cws = app.getUrlMapping().get(url);
		if (cws != null) {
			if (cws.size() == 1) {
				ControllerWrapper cw = cws.get(0);
				if (cw.getHttpMethod() == null
						|| httpMethod.equals(cw.getHttpMethod())) {
					return cw;
				}
			} else {
				for (ControllerWrapper cw : cws) {
					if (httpMethod.equalsIgnoreCase(cw.getHttpMethod()))
						return cw;
				}
			}
		}
		return regMapping(app, url, httpMethod);
	}

	/**
	 * mapping the app with url
	 * 
	 * @param url
	 * @return
	 */
	private Application getApplicationByUrl(String url) {
		Application noAppNameApp = null;
		for (Application app : runningApplications) {
			if (FrameworkUtil.isEmpty(app.getAppName())) {
				noAppNameApp = app;
			} else {
				String appName = app.getAppName();
				if (url.startsWith(Constant.leftSplit + appName))
					return app;
			}
		}
		return noAppNameApp;
	}

	/**
	 * matches the URL with all controllerWrapper
	 * 
	 * @param app
	 *            app
	 * @param url
	 *            httpRequest URL
	 * @param httpMethod
	 * @return matched controllerWrapper
	 */
	private ControllerWrapper regMapping(Application app, String url,
			String httpMethod) {
		Map<String, List<ControllerWrapper>> urlMapping = app.getUrlMapping();
		for (String u : urlMapping.keySet()) {
			if (url.matches("^" + u + "$")) {
				for (ControllerWrapper cw : urlMapping.get(u)) {
					if (httpMethod.equalsIgnoreCase(cw.getHttpMethod()))
						return cw;
				}
			}

		}
		throw new ContainerException("the url " + url + " not matched");
	}

	public ApplicationContext(ServletConfig servletConfig) {
		this.servletConfig = servletConfig;
	}

	/**
	 * ApplicationContext init
	 * 
	 */
	public void init() {
		log.info("initRunningApplication...");
		scannerApplications();
	}

	private void scannerApplications() {
		String packages = servletConfig.getInitParameter("scanPackage");
		if (packages == null || "".equals(packages.trim()))
			throw new ContainerException(
					"no application found...you need add scanPackage params in the servlet");
		for (String pack : packages.split(Constant.split)) {
			initapp(pack);
		}
	}

	private void initInterceptor(Application app, Set<Class<?>> set) {
		List<HandlerInterceptorAdapter> list = app.getInterceptor();
		if (list == null) {
			list = new ArrayList<HandlerInterceptorAdapter>();
			app.setInterceptor(list);
		}
		for (Class<?> c : set) {
			if (HandlerInterceptorAdapter.class.isAssignableFrom(c)) {
				list.add((HandlerInterceptorAdapter) getBean(c));
			}
		}

	}

	private void initapp(String pack) {
		log.info("get the package:{}", pack);
		Set<Class<?>> set = ClassUtil.getClasses(pack);
		log.info("scan the application...");
		Application app = getApplication(set);
		if (app == null) {
			log.error("no application find in {}", pack);
			return;
		}
		runningApplications.add(app);
		log.info("scan the viewResolver...");
		ViewResolver vr = getViewResolver(set);
		if (vr == null) {
			log.warn("no viewResolver find in {}", pack);
		}
		app.setViewResolver(vr);
		log.info("scan the interceptor...");
		initInterceptor(app, set);
		log.info("scan the url mapping...");
		initURLMapping(app, set);
		log.info("call the application start...");
		app.start();
	}

	private ViewResolver getViewResolver(Set<Class<?>> set) {
		for (Class<?> c : set) {
			if (ViewResolver.class.isAssignableFrom(c)) {
				ViewResolver vr = (ViewResolver) getBean(c);
				vr.init(servletConfig.getServletContext());
				return vr;
			}
		}
		return null;
	}

	private Application getApplication(Set<Class<?>> set) {
		Application app = null;
		for (Class<?> c : set) {
			if (Application.class.isAssignableFrom(c)) {
				log.info("init the application");
				app = (Application) getBean(c);
			}
		}
		return app;
	}

	/**
	 * getBean from beanContainer.
	 * 
	 * @param clazz
	 * @return
	 */
	public static <T> T getBean(Class<T> clazz) {
		return ServletDispatcher.getApplicationContext().getBeanByClass(clazz);
	}

	/**
	 * use the exist obj replace the beanfactory
	 * 
	 * @param obj
	 * @param clazz
	 */
	public static <T> void proxyBean(T obj, Class<T> clazz) {
		ServletDispatcher.getApplicationContext().updateBean(obj, clazz);
	}

	@SuppressWarnings("unchecked")
	public <T> T getBeanByClass(Class<T> clazz) {
		Object obj = beanFactory.get(clazz);
		if (obj == null) {
			obj = initBean(clazz);
		}
		return (T) obj;
	}

	public <T> void updateBean(T obj, Class<T> clazz) {
		beanFactory.put(clazz, obj);
	}

	/**
	 * init bean
	 * 
	 * @param clazz
	 * @return
	 */
	private <T> T initBean(Class<T> clazz) {
		T obj = null;
		try {
			obj = clazz.newInstance();
			beanFactory.put(clazz, obj);
			initField(obj);
		} catch (InstantiationException | IllegalAccessException e) {
			log.error("can not init the bean " + clazz, e);
		}
		return obj;
	}

	/**
	 * init the fileds of the given bean
	 * 
	 * @param obj
	 */
	private void initField(Object obj) {
		for (Field field : obj.getClass().getDeclaredFields()) {
			if (field.isAnnotationPresent(Autowired.class)) {
				field.setAccessible(true);
				try {
					field.set(obj, getBeanByClass(field.getType()));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					log.error("can not init the field " + field.getName(), e);
				}
			}
		}
	}

	private void initURLMapping(Application app, Set<Class<?>> set) {
		initController(app, set);
		initRest(app, set);
	}

	private void initRest(Application app, Set<Class<?>> set) {
		for (Class<?> c : set) {
			if (!c.isAnnotationPresent(REST.class))
				continue;
			String url = c.getAnnotation(REST.class).value();
			if (FrameworkUtil.isEmpty(url))
				continue;
			String regUrl = url + "(/(\\w*))?";
			Method[] methods = c.getMethods();
			for (Method m : methods) {
				ControllerWrapper cw = new ControllerWrapper();
				cw.setController(c);
				cw.setMethod(m);
				cw.setApplication(app);
				cw.setREST(true);
				if (m.isAnnotationPresent(POST.class)) {
					cw.setHttpMethod("POST");
					cw.setRegUrl(regUrl);
					addControllerWapper(cw);
				}
				if (m.isAnnotationPresent(GET.class)) {
					cw.setHttpMethod("GET");
					cw.setRegUrl(url);
					for (int i = 0; i < m.getParameterTypes().length; i++) {
						String name = FrameworkUtil.getMethodParamsName(m, i);
						if (Constant.RESOURCE_ID.equals(name)) {
							cw.setRegUrl(regUrl);
						}
					}
					addControllerWapper(cw);
				}
				if (m.isAnnotationPresent(PUT.class)) {
					cw.setHttpMethod("PUT");
					cw.setRegUrl(url);
					addControllerWapper(cw);
				}
				if (m.isAnnotationPresent(DELETE.class)) {
					cw.setHttpMethod("DELETE");
					cw.setRegUrl(regUrl);
					addControllerWapper(cw);
				}

			}
		}
	}

	private void initController(Application app, Set<Class<?>> set) {
		for (Class<?> c : set) {
			if (!c.isAnnotationPresent(Controller.class))
				continue;
			Method[] methods = c.getMethods();
			for (Method m : methods) {
				if (!m.isAnnotationPresent(RequestMapping.class))
					continue;
				String url = m.getAnnotation(RequestMapping.class).value();
				ControllerWrapper cw = new ControllerWrapper();
				cw.setController(c);
				cw.setMethod(m);
				cw.setApplication(app);
				setPathVars(cw, url);
				String regUrl = url.replaceAll(Constant.pattern.pattern(),
						"(\\\\w*)");
				cw.setRegUrl(regUrl);
				addControllerWapper(cw);
			}
		}
	}

	private void setPathVars(ControllerWrapper cw, String url) {
		List<String> vars = cw.getPathVars();
		if (vars == null) {
			vars = new ArrayList<String>();
			cw.setPathVars(vars);
		}
		Matcher matcher = Constant.pattern.matcher(url);
		while (matcher.find()) {
			String pathVar = matcher.group(1);
			vars.add(pathVar);
		}
	}

	private void addControllerWapper(ControllerWrapper cw) {
		Map<String, List<ControllerWrapper>> urlMapping = cw.getApplication()
				.getUrlMapping();
		if (urlMapping == null) {
			urlMapping = new HashMap<String, List<ControllerWrapper>>();
			cw.getApplication().setUrlMapping(urlMapping);
		}
		List<ControllerWrapper> cws = urlMapping.get(cw.getRegUrl());
		if (cws == null) {
			cws = new ArrayList<ControllerWrapper>();
			urlMapping.put(cw.getRegUrl(), cws);
		}
		cws.add(cw);
	}
}
