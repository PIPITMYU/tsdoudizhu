package com.up72.server.mina.main;

import com.up72.server.mina.tcp.MinaTCPServer;
import com.up72.server.mina.utils.BackFileUtil;
import com.up72.server.mina.utils.DataLoader;
import com.up72.server.mina.utils.ScheduledTask;
import com.up72.server.mina.utils.TaskUtil;
import com.up72.server.mina.utils.redis.MyRedis;

public class MinaServerManager {

	public static MinaTCPServer tcpServer;

	// protected MinaHttpServer httpServer;

	public MinaServerManager() {
		tcpServer = new MinaTCPServer();
		// httpServer = new MinaHttpServer();
	}

	public void startMinaTCPServer() {
		tcpServer.startServer();
	}

	public void startMinaHTTPServer() {
		// httpServer.startServer();
	}

	public void stopMinaTCPServer() {
		tcpServer.stopServer();
	}

	public void stopMinaHTTPServer() {
		// httpServer.stopServer();
	}

	public void broadcastMessage2TCPClient(Object message) {
		tcpServer.broadcast(message);
	}

	public void startMinaServer() {
		DataLoader.initMybatis();

		// 初始化redis
		MyRedis.initRedis();
		// 清理回放文件，避免服务器停服之后，json文件不完整
		
		BackFileUtil.deleteAllRecord();
		
		tcpServer.startServer();
		// 启动定时任务
		TaskUtil.initTaskSchdual();
//		httpServer.startServer();
    	MinaServerManager.printStr();
    	
//    	ScheduledTask.cleanPlayRecord();
	}

	public void stopMinaServer() {
		tcpServer.stopServer();
		// httpServer.startServer();
	}

	public static void main(String[] args) {
		MinaServerManager minaManager = new MinaServerManager();
		minaManager.startMinaServer();
	}

	static String[] words = "java hooray!".split("");//自定义，英文的

	public static void printStr() {
		int num = 0;
		for (float y = (float) 1.5; y > -1.5; y -= 0.1) {
			for (float x = (float) -1.5; x < 1.5; x += 0.05) {
				float a = x * x + y * y - 1;
				if ((a * a * a - x * x * y * y * y) <= 0.0) {
					if (num == words.length) {
						num = 0;
					}
					System.out.print(words[num++]);

				} else
					System.out.print(" ");
			}
			System.out.print("\n");
		}
	}

}
