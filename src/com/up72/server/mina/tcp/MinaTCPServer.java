package com.up72.server.mina.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.keepalive.KeepAliveFilter;
import org.apache.mina.filter.keepalive.KeepAliveMessageFactory;
import org.apache.mina.filter.keepalive.KeepAliveRequestTimeoutHandler;
import org.apache.mina.filter.logging.LogLevel;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import com.up72.game.constant.Cnst;
import com.up72.server.mina.bean.ProtocolData;
import com.up72.server.mina.function.TCPFunctionExecutor;
import com.up72.server.mina.handler.MinaServerHandler;
import com.up72.server.mina.utils.MyLog;

/**
 *
 * @author mac
 *
 */
public class MinaTCPServer extends MinaServerAdapter {


	/** 心跳包内容 */
	public static final String HEART_BEAT = "\"ping\"";
	
    private static final MyLog logger = MyLog.getLogger(MinaTCPServer.class);
    public static void main(String[] args) {
        new MinaTCPServer().startServer();
    }
    public MinaTCPServer() {
    }
    @Override
    public void startServer() {
        logger.I("Mina TCP Server 服务器开始启动");
        if (isStoped()) {
            try {
                acceptor = new NioSocketAcceptor(Runtime.getRuntime().availableProcessors() * 20);
                acceptor.getSessionConfig().setMaxReadBufferSize(Cnst.Session_Read_BufferSize);
                acceptor.getSessionConfig().setMinReadBufferSize(Cnst.Session_Read_BufferSize);
                acceptor.getSessionConfig().setReadBufferSize(2048);//320 ks
                acceptor.getSessionConfig().setReceiveBufferSize(2048);//接收缓冲区1M  320 ks
                acceptor.getSessionConfig().setTcpNoDelay(true);
                acceptor.getSessionConfig().setSendBufferSize(Cnst.Session_Read_BufferSize);
                acceptor.getSessionConfig().setWriteTimeout(Cnst.WriteTimeOut);
                acceptor.getSessionConfig().setKeepAlive(true);
//                acceptor.getSessionConfig().setIdleTime(IdleStatus.READER_IDLE,
//				60 * 60 * 2);//----0322
                acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE,
				30);//----0322
                acceptor.getSessionConfig().setSoLinger(0); // 这个是根本解决问题的设置  系统的TIME_WAIT过多，造成服务器的负载过高
//                acceptor.getFilterChain().addLast("thread_pool", new ExecutorFilter(Executors.newFixedThreadPool(500)));
                LoggingFilter loggingFilter = new LoggingFilter();
                loggingFilter.setMessageReceivedLogLevel(LogLevel.INFO);
                loggingFilter.setMessageSentLogLevel(LogLevel.INFO);
                acceptor.getFilterChain().addLast("logger", loggingFilter);

                acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new MyProtocolFactory()));
                
                /** 心跳设置 */

        		KeepAliveMessageFactory heartBeatFactory = new KeepAliveMessageFactoryImpl();
        		KeepAliveRequestTimeoutHandler heartBeatHandler = new KeepAliveRequestTimeoutHandlerImpl();
        		KeepAliveFilter heartBeat = new KeepAliveFilter(heartBeatFactory,
        				IdleStatus.BOTH_IDLE, heartBeatHandler);
        		/** 是否回发 */
        		heartBeat.setForwardEvent(false);
        		/** 发送频率 */
        		heartBeat.setRequestInterval(20);
        		acceptor.getFilterChain().addLast("heartbeat", heartBeat);
                
                ioHandler = new MinaServerHandler(acceptor);
                acceptor.setHandler(ioHandler);
                acceptor.bind(new InetSocketAddress(Integer.valueOf(Cnst.MINA_PORT)));
            } catch (IOException e) {
                logger.E("Mina TCP Server 启动异常", e);
            }
        } else {
            logger.I("Mina TCP Server is allready started...");
        }
        logger.I("Mina TCP Server 唐山斗地主服务器启动完成，端口："+Cnst.MINA_PORT);
    }

    @Override
    public void stopServer() {
        logger.I("Mina TCP Server stop ...");
        acceptor.unbind();
        acceptor.dispose();
    }
    
    /***
	 * @ClassName: KeepAliveMessageFactoryImpl
	 * @Description: 内部类，实现心跳工厂
	 * @author Minsc Wang ys2b7_hotmail_com
	 * @date 2011-3-7 下午04:09:02
	 * 
	 */
	private static class KeepAliveMessageFactoryImpl implements
			KeepAliveMessageFactory {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.apache.mina.filter.keepalive.KeepAliveMessageFactory#getRequest
		 * (org.apache.mina.core.session.IoSession)
		 */
		@Override
		public Object getRequest(IoSession session) {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.apache.mina.filter.keepalive.KeepAliveMessageFactory#getResponse
		 * (org.apache.mina.core.session.IoSession, java.lang.Object)
		 */
		@Override
		public Object getResponse(IoSession session, Object request) {
//			System.err.println("getResponse");
//			/** 返回预设语句 */
//			Map<String,String> info = new HashMap<String, String>();
//			info.put("interfaceId", "100000");
//			JSONObject result = TCPGameFunctions.getJSONObj(100000,1,info);
//	        ProtocolData pd = new ProtocolData(100000, result.toJSONString());
//			return pd;
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.apache.mina.filter.keepalive.KeepAliveMessageFactory#isRequest
		 * (org.apache.mina.core.session.IoSession, java.lang.Object)
		 */
		@Override
		public boolean isRequest(IoSession session, Object message) {
			if(((ProtocolData)message).getJsonString().equals(HEART_BEAT)){
				try {
					TCPFunctionExecutor.heart(session, (ProtocolData)message);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return true;
			}
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.apache.mina.filter.keepalive.KeepAliveMessageFactory#isResponse
		 * (org.apache.mina.core.session.IoSession, java.lang.Object)
		 */
		@Override
		public boolean isResponse(IoSession session, Object message) {
			if(((ProtocolData)message).getJsonString().equals(HEART_BEAT))
				return true;
			return false;
		}

	}

	/***
	 * @ClassName: KeepAliveRequestTimeoutHandlerImpl
	 * @Description: 当心跳超时时的处理，也可以用默认处理 这里like
	 *               KeepAliveRequestTimeoutHandler.LOG的处理
	 * @author Minsc Wang ys2b7_hotmail_com
	 * @date 2011-3-7 下午04:15:39
	 * 
	 */
	private static class KeepAliveRequestTimeoutHandlerImpl implements
			KeepAliveRequestTimeoutHandler {

		/*
		 * (non-Javadoc)
		 * 
		 * @seeorg.apache.mina.filter.keepalive.KeepAliveRequestTimeoutHandler#
		 * keepAliveRequestTimedOut
		 * (org.apache.mina.filter.keepalive.KeepAliveFilter,
		 * org.apache.mina.core.session.IoSession)
		 */
		@Override
		public void keepAliveRequestTimedOut(KeepAliveFilter filter,
				IoSession session) throws Exception {
			logger.I("心跳超时！");
			if (session!=null) {
				session.close(true);
			}
		}

	}
}