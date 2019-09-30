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
import com.nomad.server.ProxyServerContext;

public class ProxyServer implements Runnable {
	private ServerSocket serverSocket = null;
	private boolean isStopped = false;
	private Thread runningThread = null;
	private ExecutorService threadPool;
	protected static Logger logger = LoggerFactory.getLogger(ProxyServer.class);
	private ProxyServerContext context;
	private ProxyServerModel managerServerModel;

	public ProxyServer(   ProxyServerContext context ) throws Exception {
	  this.context= context;
	   managerServerModel =(ProxyServerModel)context.get(ProxyServerContext.ServiceName.ProxyServerModel.toString());
		threadPool = Executors.newFixedThreadPool(managerServerModel.getProxyThreads());

	}

	public void run() {
		logger.info("new ProxyServer thread " + new Date());
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
			this.threadPool.execute(new ProxyThread(clientSocket, context));
		}
		runningThread.interrupt();
		this.threadPool.shutdown();
		logger.info("Server Stopped.");

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
		logger.info("open proxy socket:" + managerServerModel.getProxyPort());
		try {
			this.serverSocket = new ServerSocket(managerServerModel.getProxyPort());
		} catch (IOException e) {
			throw new RuntimeException("Cannot open port: " + managerServerModel.getProxyPort(), e);
		}
	}


}
