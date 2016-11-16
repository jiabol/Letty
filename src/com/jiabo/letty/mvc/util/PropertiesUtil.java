package com.jiabo.letty.mvc.util;

import java.util.Properties;

/**
 * The util for the properties.
 * 
 * @author jialong
 *
 */
public class PropertiesUtil {
	private static Properties props = new Properties();

	/**
	 * get property
	 * 
	 * @param key
	 *            key
	 * @return property
	 */
	public static String getProp(String key) {
		return props.getProperty(key);
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
		String str = props.getProperty(Constant.devMode);
		if (str != null)
			return "true".equals(str);
		String os = System.getProperty("os.name");
		return os.contains("Windows");
	}
}
