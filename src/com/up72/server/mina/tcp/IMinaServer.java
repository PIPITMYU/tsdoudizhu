package com.up72.server.mina.tcp;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.SocketAcceptor;

import java.util.Map;

public interface IMinaServer extends ITCPServer, IHttpServer {

	public SocketAcceptor getAcceptor();

	public IoHandler getIoHandler();

	public Map<Long, IoSession> getSessions();

}
