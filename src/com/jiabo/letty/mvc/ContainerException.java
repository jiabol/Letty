package com.jiabo.letty.mvc;

/**
 * framework exception
 * @author jialong
 *
 */
public class ContainerException extends RuntimeException {


	private static final long serialVersionUID = 1532974881955810842L;

	public ContainerException(String msg) {
		super(msg);
	}

	public ContainerException(String msg, Throwable e) {
		super(msg, e);
	}
}
