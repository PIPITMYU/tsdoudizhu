package com.up72.server.mina.bean;

/**
 * 基础协议
 * 
 * @author mac
 *
 */
public class EmptyProtocol {
	protected int result;

	public EmptyProtocol() {
		super();
	}

	public EmptyProtocol(int result) {
		super();
		this.result = result;
	}

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

}
