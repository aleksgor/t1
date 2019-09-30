package com.nomad.cache.servermanager.externalinterface;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.nomad.cache.servermanager.server.connectpool.StoreConnectionPool;

public class BestServerCalculator {

	public List<StoreConnectionPool> getPools() {
		return servers;
	}

	private final AtomicInteger position = new AtomicInteger(0);
	private volatile int listSize;
	private volatile List<StoreConnectionPool> servers;

	public BestServerCalculator(List<StoreConnectionPool> servers) {
		listSize = servers.size();
		this.servers = servers;

	}

	
	public void addConnectPool(StoreConnectionPool server) {
		servers.add(server);
		listSize++;
	}

	public StoreConnectionPool getBestServer() {
		return servers.get(getNextPosition());
	}

	public final int getNextPosition() {
		for (;;) {
			int current = position.get();
			int next = current + 1;
			if (next >= listSize) {
				next = 0;
			}
			if (position.compareAndSet(current, next)){
				return current;
			}
		}
	}
	
	@Override
	public String toString() {
		return "BestServerCalculator [position=" + position + ", listSize=" + listSize + ", servers=" + servers + "]";
	}

}
