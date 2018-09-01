package com.up72.server.mina.tcp;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

public class MyProtocolFactory implements ProtocolCodecFactory {

	protected MinaDecoder decode;
	protected MinaEncoder encode;

	public MyProtocolFactory() {
		// TODO Auto-generated constructor stub
		decode = new MinaDecoder();
		encode = new MinaEncoder();
	}

        @Override
	public ProtocolEncoder getEncoder(IoSession session) throws Exception {
		// TODO Auto-generated method stub
		return encode;
	}

        @Override
	public ProtocolDecoder getDecoder(IoSession session) throws Exception {
		// TODO Auto-generated method stub
		return decode;
	}

}
