package com.up72.server.mina.bean;

import java.io.Serializable;

import com.alibaba.fastjson.JSON;

import org.apache.mina.core.session.IoSession;

public class ProtocolData implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8230945474084831712L;
	public int interfaceId;
	public String jsonString = "";
	public IoSession session;
        //private JSONObject result;
        
        //320
		public boolean isWebAccept=false;
        public boolean isWebAccept() {
		return isWebAccept;
	}
        public void setWebAccept(boolean isWebAccept) {
		this.isWebAccept = isWebAccept;
	}
        

	public ProtocolData() {
		super();
	}

	public ProtocolData(int interfaceId, String jsonString) {
		this.interfaceId = interfaceId;
		this.jsonString = jsonString;
	}

	public ProtocolData(int interfaceId, String jsonString, IoSession ss) {
		super();
		this.interfaceId = interfaceId;
		this.jsonString = jsonString;
		this.session = ss;
	}

	public ProtocolData(int interfaceId, EmptyProtocol protocol) {
		super();
		this.interfaceId = interfaceId;
		this.jsonString = JSON.toJSONString(protocol);
	}

	public int getInterfaceId() {
		return interfaceId;
	}

	public void setInterfaceId(int interfaceId) {
		this.interfaceId = interfaceId;
	}

	public String getJsonString() {
		return jsonString;
	}

	public void setJsonString(String t) {
		if (t != null) {
			this.jsonString = t;
		}
	}

	public IoSession getSession() {
		return session;
	}

	public void setSession(IoSession session) {
		this.session = session;
	}

	@Override
	public String toString() {
		return "ProtocolData [interfaceId=" + interfaceId + ", jsonString=" + jsonString + "]";
	}

}
