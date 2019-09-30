package com.nomad.cache.servermanager.server;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.message.ManagerMessage;
import com.nomad.message.ManagerMessage.Answer;
import com.nomad.message.MessageSenderReceiver;
import com.nomad.message.MessageSenderReceiverImpl;
import com.nomad.model.StatisticData;
import com.nomad.cache.servermanager.models.ProxyServerModel;
import com.nomad.cache.servermanager.models.SessionStorage;
import com.nomad.cache.servermanager.server.commands.GetListCommand;
import com.nomad.cache.servermanager.server.commands.RegisterCommand;
import com.thoughtworks.xstream.io.StreamException;

public class ManagerThread implements Runnable {
	private Socket clientSocket = null;
	
	private volatile ProxyServerModel managerServer;
	private static Logger logger = LoggerFactory.getLogger(ManagerThread.class);
	private volatile SessionStorage sessionStorage;
	private volatile StatisticData statistcData;

	public ManagerThread(Socket clientSocket, ProxyServerModel managerServer, SessionStorage sessionStorage,StatisticData statistcData) {

		this.clientSocket = clientSocket;
		this.managerServer = managerServer;
		this.sessionStorage = sessionStorage;
		this.statistcData=statistcData;

	}

	public void run() {
		logger.info("register: start");
		InputStream input = null;
		OutputStream output = null;
		try {
			input = clientSocket.getInputStream();
			output = clientSocket.getOutputStream();
			logger.info("start thread:");
			MessageSenderReceiver msr = new MessageSenderReceiverImpl();
			
			ManagerMessage message = (ManagerMessage) msr.getObject(input);
      logger.info("manager massage:"+message);

			switch (message.getCommand()) {
			case REGISTER:
			  msr.storeObject(new RegisterCommand().execute(managerServer, message), output, msr.getMessageVersion());
				break;
			case GETSERVERLIST:
			  msr.storeObject(new GetListCommand().execute(managerServer, message), output, msr.getMessageVersion());
				break;
			case UNREGISTER:
				break;
			case CHECKSESSIONS:
				@SuppressWarnings("unchecked")
				List<String> sessions = (List<String>) message.getData();
				List<String> result = new ArrayList<String>();
				for (String session : sessions) {
					if (!sessionStorage.checkseSsion(session)) {
						result.add(session);
					}
				}
				message.setData(result);
				message.setAnswer(Answer.OK);
				msr.storeObject(message, output, msr.getMessageVersion());
				break;
			case GETSTATISIC:
				Date date=(Date)message.getData();
				
				logger.info("satistic 01:" + date.getTime());
				logger.info("satistic 0:" + statistcData);
				message.setData(statistcData.getStatistcDataForTime(date));
				message.setAnswer(Answer.OK);
				logger.info("satistic:" + message.getData());
        msr.storeObject(message, output, msr.getMessageVersion());
			case GETMEMORYINFO:
				 Runtime runtime = Runtime.getRuntime();
				 Properties property= new Properties();
				 property.put("freeMemory", ""+runtime.freeMemory());
				 property.put("maxMemory", ""+runtime.maxMemory());
				 property.put("totalMemory", ""+runtime.totalMemory());
				 message.setData(property);
				 message.setAnswer(Answer.OK);
	        msr.storeObject(message, output, msr.getMessageVersion());
			default:
				break;
			}

		} catch (EOFException e) {
			logger.error("manager session closed!",e);
		} catch (StreamException e) {
			logger.debug("manager session closed!");
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