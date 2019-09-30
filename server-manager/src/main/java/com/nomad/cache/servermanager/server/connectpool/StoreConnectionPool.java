package com.nomad.cache.servermanager.server.connectpool;

import java.io.IOException;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.cache.commonClientServer.MessageImpl;
import com.nomad.message.FullMessage;
import com.nomad.message.MessageHeader;
import com.nomad.model.BaseCommand;
import com.nomad.model.ChildMainListener;
import com.nomad.model.ChildServer;
import com.nomad.utile.pool.ObjectPool;

public class StoreConnectionPool extends ObjectPool<PooledConnection> {

  private static Logger logger = LoggerFactory.getLogger(StoreConnectionPool.class);
  private   ChildServer server;
  private  ChildMainListener listener;
  private volatile int couter=0;

  public StoreConnectionPool(ChildServer server, long timeout, ChildMainListener listener) throws UnknownHostException, IOException {
    super(listener.getThreads(), timeout, 1000);
    this.server = server;
    this.listener=listener;

  }

  public ChildServer getServer() {
    return server;
  }

  @Override
  protected PooledConnection getNewPooledObject() {
    PooledConnection connection;

    try {
      connection = new PooledConnection(listener.getHost(), listener.getPort());
      MessageHeader header = new MessageHeader();
      header.setVersion(listener.getProtocolVersion());
      header.setCommand(BaseCommand.CloseSession);
      header.setSessionId(""+(++couter));
      FullMessage result =connection.execMessage(header, new MessageImpl(null));
      logger.debug(" init result:"+result);
      return connection;
    } catch (Exception e) {
      logger.error("Error create connection:", e);
      throw new RuntimeException();
    }

  }

 
}
