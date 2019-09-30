package com.nomad.server.service.childserver;

import com.nomad.message.RawMessage;

public class MessageAnswer {

	private RawMessage message;
	private StoreConnectionPool pool ;
	
	
	public MessageAnswer(RawMessage message, StoreConnectionPool pool) {
		super();
		this.message = message;
		this.pool = pool;
	}
	public RawMessage getMessage() {
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
