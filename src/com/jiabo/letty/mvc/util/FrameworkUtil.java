package com.jiabo.letty.mvc.util;

import java.lang.reflect.Method;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiabo.letty.mvc.Application;

/**
 * framework baseutils.
 * 
 * @author jialong
 *
 */
public class FrameworkUtil {

	private static final Logger log = LoggerFactory
			.getLogger(FrameworkUtil.class);

	/**
	 * java reflect can not get the method's parameter's name. This method can
	 * get the name.
	 * 
	 * @param method
	 *            method
	 * @param index
	 *            parameter's index start from zero.
	 * @return parameter's name
	 *
	 */
	public static String getMethodParamsName(Method method, int index) {
		try {
			ClassPool pool = ClassPool.getDefault();
			pool.insertClassPath(new ClassClassPath(FrameworkUtil.class));
			CtClass cc = pool.get(method.getDeclaringClass().getName());
			CtMethod cm = cc.getDeclaredMethod(method.getName());

			MethodInfo methodInfo = cm.getMethodInfo();
			CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute
					.getAttribute(LocalVariableAttribute.tag);
			if (attr == null) {
				throw new RuntimeException("can not get the attributes");
			}
			int pos = Modifier.isStatic(cm.getModifiers()) ? 0 : 1;
			return attr.variableName(index + pos);
		} catch (NotFoundException e) {
			log.error("", e);
		}
		return null;
	}

	/**
	 * String is null or " "
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isEmpty(String str) {
		return str == null || "".equals(str.trim());
	}

	/**
	 * get the application from httpservletrequest.
	 * 
	 * @param hreq
	 * @return
	 */
	public static Application getApplication(HttpServletRequest hreq) {
		return (Application) hreq.getAttribute("application");
	}

	/**
	 * if config.properties not define use the default default development mode
	 * :the operation system is windows. default product mode:the operation
	 * system is not windows.
	 * 
	 * @return true dev mdoe .false not dev mode
	 *
	 */
	public static boolean isDevMode() {
		String os = System.getProperty("os.name");
		return os.contains("Windows");
	}

	public static String removeAppPrefix(String url, String appName) {
		if (url == null)
			return null;
		if (isEmpty(appName))
			return url;
		if (!url.startsWith(Constant.leftSplit))
			url = Constant.leftSplit + url;
		if (!url.startsWith(Constant.leftSplit + appName))
			return url;
		url = url.substring(appName.length() + 1);
		if ("".equals(url.trim()))
			url = "/";
		if (!url.startsWith("/"))
			url = "/" + url;
		return url;
	}
}
