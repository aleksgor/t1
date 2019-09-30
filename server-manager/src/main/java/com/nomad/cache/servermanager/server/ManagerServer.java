package com.nomad.cache.servermanager.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.cache.servermanager.models.ProxyServerModel;
import com.nomad.cache.servermanager.models.SessionStorage;
import com.nomad.model.StatisticData;


public class ManagerServer implements Runnable {
	private ServerSocket serverSocket = null;
	private boolean isStopped = false;
	private Thread runningThread = null;
	private ExecutorService threadPool;
	private volatile ProxyServerModel managerServerModel; 
	protected static Logger logger = LoggerFactory.getLogger(ManagerServer.class);
	private volatile SessionStorage sessionStorage;
	private volatile StatisticData statistcData;
	
	
	public ManagerServer(ProxyServerModel server,SessionStorage sessionStorage, StatisticData statistcData) throws Exception {
		threadPool = Executors.newFixedThreadPool(server.getThreads());
		this.managerServerModel=server;
		this.sessionStorage=sessionStorage;
		this.statistcData=statistcData;
	}

	public void run() {
		logger.info("new manager thread "+new Date());
		synchronized (this) {
			this.runningThread = Thread.currentThread();
		}
		openServerSocket();
		while (!isStopped()) {
			Socket clientSocket = null;
			try {

				clientSocket = this.serverSocket.accept();

			} catch (IOException e) {
				if (isStopped()) {
					logger.info("Server Stopped.");
					return;
				}
				throw new RuntimeException("Error accepting client connection", e);
			}
			logger.info("new manager thread "+new Date());
			this.threadPool.execute(new ManagerThread(clientSocket,managerServerModel,sessionStorage,statistcData));
		}
		runningThread.interrupt();
		this.threadPool.shutdown();
		logger.info("Manager Server Stopped.");

		
	}

	private synchronized boolean isStopped() {
		return this.isStopped;
	}

	public synchronized void stop() {
		this.isStopped = true;
		try {
			this.serverSocket.close();
		} catch (IOException e) {
			throw new RuntimeException("Error closing server", e);
		}
	}

	private void openServerSocket() {
		logger.info("Manager Server open port:{}",managerServerModel.getPort());

		try {
			this.serverSocket = new ServerSocket(this.managerServerModel.getPort());
		} catch (IOException e) {
			throw new RuntimeException("Cannot open port: "+managerServerModel.getPort(), e);
		}
	}
}
