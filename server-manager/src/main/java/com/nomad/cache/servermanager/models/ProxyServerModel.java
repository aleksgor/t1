package com.nomad.cache.servermanager.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.cache.servermanager.externalinterface.BestServerCalculator;
import com.nomad.cache.servermanager.server.connectpool.StoreConnectionPool;
import com.nomad.model.Model;
import com.nomad.server.ProxyPlugin;

public class ProxyServerModel {

	private int port;
	private String host;
	private int threads;
	private int proxyPort;
	private int proxyThreads;
	private int sessionTimeout;
	private int requestTimeout=500;
	private List<ProxyPlugin> plugins = new ArrayList<ProxyPlugin>();

	private volatile List<StoreConnectionPool> children = new Vector<StoreConnectionPool>();

	private volatile Map<String, BestServerCalculator> bestServerCalculators = new HashMap<String, BestServerCalculator>();
	private static Logger logger = LoggerFactory.getLogger(ProxyServerModel.class);

	public int getSessionTimeout() {
		return sessionTimeout;
	}

	public void setSessionTimeout(int sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public int getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}

	public int getProxyThreads() {
		return proxyThreads;
	}

	public void setProxyThreads(int proxyThreads) {
		this.proxyThreads = proxyThreads;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public List<StoreConnectionPool> getConnectPool() {
		return children;
	}

	public int getThreads() {
		return threads;
	}

	public void setThreads(int threads) {
		this.threads = threads;
	}

	public List<StoreConnectionPool> getPoolForModel(String  modelName) {
		return bestServerCalculators.get(modelName).getPools();
	}

	public int getRequestTimeout() {
		return requestTimeout;
	}

	public void setRequestTimeout(int requestTimeout) {
		this.requestTimeout = requestTimeout;
	}

	public List<ProxyPlugin> getPlugins() {
    return plugins;
  }

  @Override
  public String toString() {
    return "ManagerServerModel [port=" + port + ", host=" + host + ", threads=" + threads + ", proxyPort=" + proxyPort + ", proxyThreads=" + proxyThreads
        + ", sessionTimeout=" + sessionTimeout + ", requestTimeout=" + requestTimeout + ", plugins=" + plugins + ", children=" + children
        + ", bestServerCalculators=" + bestServerCalculators + "]";
  }

	public void addConnectPool(StoreConnectionPool pool) {
		logger.info("add into ConnectPool server: {}", pool);
		children.add(pool);
		
		for (String modelName : pool.getServer().getModels()) {
			BestServerCalculator calculator = bestServerCalculators.get(modelName);
			if (calculator == null) {
				calculator = new BestServerCalculator(new Vector<StoreConnectionPool>());
				bestServerCalculators.put(modelName, calculator);
			}

			calculator.addConnectPool(pool);
		}

	}

	public StoreConnectionPool getBestServer(Model model) {
		return getBestServer(model.getIdentifier().getModelName());

	}

	public StoreConnectionPool getBestServer(String modelName) {
		logger.debug("servers:{}", bestServerCalculators.get(modelName));
		return bestServerCalculators.get(modelName).getBestServer();

	}
}
