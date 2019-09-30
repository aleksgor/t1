package com.nomad.cache.servermanager.server.connectpool;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.cache.commonClientServer.FullMessageImpl;
import com.nomad.cache.commonClientServer.MessageImpl;
import com.nomad.message.FullMessage;
import com.nomad.message.ModelMessage;
import com.nomad.message.MessageHeader;
import com.nomad.message.MessageSenderReceiver;
import com.nomad.message.MessageSenderReceiverImpl;
import com.nomad.utile.pool.PooledObjectImpl;

public class PooledConnection extends PooledObjectImpl {

  private Socket client = null;
  private InputStream input;
  private OutputStream output;
  private String host;
  private int port;
  private static Logger logger = LoggerFactory.getLogger(PooledConnection.class);

  public PooledConnection(String host, int port) throws UnknownHostException, IOException {
    this.host = host;
    this.port = port;

  }

  public FullMessage execMessage(MessageHeader header, ModelMessage message) {
    try {
      checkConnect();
      MessageSenderReceiver msr= new MessageSenderReceiverImpl();

      msr.assembleFullMessage(new FullMessageImpl(header,message), output);
      FullMessage result = msr.parseFullMessage(input);
      return result;
    } catch (Throwable e) {
      logger.error(e.getMessage(), e);
    }

    return new FullMessageImpl(header, new MessageImpl( null, ModelMessage.Result.Error));
  }

  private void checkConnect() throws UnknownHostException, IOException {
    if (client == null) {
      client = new Socket(host, port);
      input = client.getInputStream();
      output = client.getOutputStream();

      return;
    }
    if (client.isConnected()) {
      return;
    }
    // try to connect
    try {
      client.close();
    } catch (Throwable t) {

    }

    client = new Socket(host, port);
    input = client.getInputStream();
    output = client.getOutputStream();

  }

  @Override
  public void closeObject() {
    try {
      input.close();
      output.close();
      client.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
