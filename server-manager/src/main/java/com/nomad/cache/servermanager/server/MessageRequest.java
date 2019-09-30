package com.nomad.cache.servermanager.server;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.cache.commonClientServer.FullMessageImpl;
import com.nomad.cache.commonClientServer.MessageImpl;
import com.nomad.cache.servermanager.server.connectpool.PooledConnection;
import com.nomad.cache.servermanager.server.connectpool.StoreConnectionPool;
import com.nomad.message.FullMessage;
import com.nomad.message.ModelMessage;
import com.nomad.message.ModelMessage.Result;
import com.nomad.message.MessageHeader;

public class MessageRequest implements Callable<MessageAnswer> {
	private static Logger logger = LoggerFactory.getLogger(MessageRequest.class);

	private StoreConnectionPool pool;
	private MessageHeader header;
	private ModelMessage message;
	
	public MessageRequest (MessageHeader header, ModelMessage message, StoreConnectionPool pool){
    this.pool=pool;
    this.header=header;
    this.message=message;
  }
	
	@Override
	public MessageAnswer call() throws Exception {
		logger.debug("send message {} to {}",header, pool);	
		
		FullMessage result = executeMessage();
		return new MessageAnswer(result,pool);
	}

	public FullMessage executeMessage(){
		PooledConnection connect = pool.getObject();
		FullMessage result=null;
		if(connect!=null){
			try{
				result=connect.execMessage(header, message);
				logger.debug("take message {} from {}",header, pool);	

			}finally{
			  connect.freeObject();
			}
		}
		if(result==null){
			logger.error("cannot conect to server:"+pool.getServer());
			result= new FullMessageImpl(header, new MessageImpl(null, Result.Error));
		}
		return result;
	}

}
