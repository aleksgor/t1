package com.nomad.cache.servermanager.server;

import com.nomad.cache.servermanager.server.connectpool.StoreConnectionPool;
import com.nomad.message.FullMessage;

public class MessageAnswer {

	private FullMessage message;
	private StoreConnectionPool pool ;
	
	
	public MessageAnswer(FullMessage message, StoreConnectionPool pool) {
		super();
		this.message = message;
		this.pool = pool;
	}
	public FullMessage getMessage() {
		return message;
	}

	
	public StoreConnectionPool getPool() {
		return pool;
	}
	
	
	@Override
	public String toString() {
		return "MessageAnswer [message=" + message + ", pool=" + pool + "]";
	}
}
