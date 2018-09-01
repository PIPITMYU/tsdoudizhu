package com.up72.server.mina.tcp;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.SocketAcceptor;

import java.util.Map;

public abstract class MinaServerAdapter implements IMinaServer {

	protected SocketAcceptor acceptor = null;
	protected IoHandler ioHandler = null;

	@Override
	public SocketAcceptor getAcceptor() {
		return acceptor;
	}

	@Override
	public IoHandler getIoHandler() {
		return ioHandler;
	}

	@Override
	public boolean isRunning() {

		if (acceptor != null) {
			return acceptor.isActive();
		}
		return false;
	}

	@Override
	public boolean isStoped() {
		if (acceptor != null) {
			return !acceptor.isActive();
		}
		return true;
	}

	@Override
	public int getSessionCount() {
		if (acceptor != null) {
			return acceptor.getManagedSessionCount();
		}
		return 0;
	}

	@Override
	public Map<Long, IoSession> getSessions() {
		if (acceptor != null) {
			return acceptor.getManagedSessions();
		}
		return null;
	}

	@Override
	public void broadcast(Object message) {
		if (acceptor != null && message != null) {
			acceptor.broadcast(message);
		}
	}

}
