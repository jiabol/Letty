package com.jiabo.letty.mvc.util;

import java.util.regex.Pattern;

public interface Constant {

	String scanPackage = "scanPackage";
	String devMode = "devMode";
	String split = ",";
	String leftSplit = "/";
	Pattern pattern = Pattern.compile("\\{(\\w*?)\\}");
	String RESOURCE_ID = "id";
}
