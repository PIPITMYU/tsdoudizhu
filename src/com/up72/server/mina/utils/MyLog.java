package com.up72.server.mina.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public final class MyLog {

	private Log  logger;
	private String tag;

	private MyLog(Class<?> clazz, String name) {
		tag = clazz.getName();
		logger = LogFactory.getLog(name);
	}

	private MyLog(String _tag, String name) {
		this.tag = _tag;
		logger = LogFactory.getLog(name);
	}

	public static MyLog getLogger(Class<?> clazz) {
		return new MyLog(clazz, "detail");
	}

	public static MyLog getChongZhiLogger(Class<?> clazz) {
		return new MyLog(clazz, "chongzhi");
	}

	public static MyLog getDetailiLogger(Class<?> clazz) {
		return new MyLog(clazz, "detail");
	}

	public static MyLog getPressureLogger(Class<?> clazz) {
		return new MyLog(clazz, "pressure");
	}

	public void E(String msg) {
		logger.error("{" + tag + "} -- " + msg);
	}

	public void E(String msg, Throwable e) {
		logger.error("{" + tag + "} -- " + msg, e);
	}

	public void D(String msg) {
		logger.debug("{" + tag + "} -- " + msg);
	}

	public void I(String msg) {
		logger.info("{" + tag + "} -- " + msg);
	}

	public void W(String msg) {
		logger.warn("{" + tag + "} -- " + msg);
	}

}
