package com.nomad.cache.servermanager.server;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.message.FullMessage;
import com.nomad.message.MessageHeader;
import com.nomad.message.MessageSenderReceiver;
import com.nomad.message.MessageSenderReceiverImpl;
import com.nomad.message.ModelMessage.Result;
import com.nomad.model.StatisticData;
import com.nomad.server.ProxyServerContext;

import com.thoughtworks.xstream.io.StreamException;

public class ProxyThread implements Runnable {
  private Socket clientSocket = null;
  private static Logger logger = LoggerFactory.getLogger(ProxyThread.class);
  private volatile StatisticData statisticData;
  private ManagerImpl managerImpl;

  public ProxyThread(Socket clientSocket, ProxyServerContext context) {

    this.clientSocket = clientSocket;
    managerImpl = new ManagerImpl(context);
    this.statisticData = (StatisticData) context.get(ProxyServerContext.ServiceName.StatisticData.toString());

  }

  public void run() {

    InputStream input = null;
    OutputStream output = null;
    try {
      input = clientSocket.getInputStream();
      output = clientSocket.getOutputStream();
      logger.info("start thread:");

      while (true) {

        MessageSenderReceiver msr = new MessageSenderReceiverImpl();
        MessageHeader header = msr.getMessageHeader(input);
        logger.debug("proxy in  message{}", header);
        long start = System.currentTimeMillis();
        FullMessage result = managerImpl.sendMessage(header, input);
        if (!Result.OK.equals(result.getMessage().getResult())) {
          logger.error("ERROR:{}", result);
        }
        statisticData.registerRequest(System.currentTimeMillis() - start, result);
        logger.info("proxy result message" + result);
        msr.assembleFullMessage(result, output);
      }

    } catch (EOFException e) {
      logger.error("EOFException", e);
    } catch (StreamException e) {
      logger.info("StreamException timeout !");
    } catch (IOException e) {
      logger.error("IOException", e);
    } catch (Throwable e) {
      logger.error("Throwable", e);
    } finally {
      try {
        if (output != null) {
          output.close();
        }
      } catch (Throwable e) {
        e.printStackTrace();
      }
      try {
        if (input != null) {
          input.close();
        }
      } catch (Throwable e) {
        e.printStackTrace();
      }

    }
  }

}