package com.nomad.cache.servermanager.server.commands;

import java.io.IOException;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.cache.servermanager.StringClient;
import com.nomad.model.ChildMainListener;
import com.nomad.model.ChildServer;
import com.nomad.model.ListenerModel;
import com.nomad.model.ServerModel;
import com.nomad.message.ManagerMessage;
import com.nomad.cache.servermanager.models.ProxyServerModel;
import com.nomad.cache.servermanager.server.connectpool.StoreConnectionPool;

public class RegisterCommand implements ManagerCommand {
	private static Logger logger = LoggerFactory.getLogger(RegisterCommand.class);

	public ManagerMessage execute(ProxyServerModel managerServer, ManagerMessage message) {
		ChildServer ch = new ChildServer();

		ServerModel server = (ServerModel) message.getData();

		for (ListenerModel listener : server.getListeners()) {
			ChildMainListener cml = new ChildMainListener();
			cml.setHost(listener.getHost());
			cml.setPort(listener.getPort());
			cml.setThreads(listener.getThreads());
			cml.setProtocolVersion(listener.getProtocolVersion());
			ch.getChildListners().add(cml);

		}
		ch.setCommandPort(server.getCommandPort());
		ch.setManagementPort(server.getManagementPort());

		StoreConnectionPool cp = initServer(ch);

		logger.info("register: server:{}", ch);
		managerServer.addConnectPool(cp);
		message.setAnswer(ManagerMessage.Answer.OK);
		return message;

	}

	private StoreConnectionPool initServer(ChildServer server) {
		StringClient sclient = new StringClient();
		String modelList = sclient.getInfo(server, "getListModels");
		String[] aModels = modelList.split(",");
		for (String string : aModels) {
			server.getModels().add(string.trim());
		}
		try {
			ChildMainListener listener = selectBestMethod(server);
			return new StoreConnectionPool(server, 3000, listener);
		} catch (UnknownHostException e) {
			logger.error("UnknownHostException", e);
		} catch (IOException e) {
			logger.error("IOException", e);
		}
		return null;
	}

	private ChildMainListener selectBestMethod(ChildServer server) {
		ChildMainListener result = null;
		server.getChildListners();
		int ressumm = 0;
		for (ChildMainListener listener : server.getChildListners()) {
			if (result == null) {
				result = listener;
				ressumm = getSum(listener.getProtocolVersion());
			} else {
				int summ = getSum(listener.getProtocolVersion());
				if (summ > ressumm) {
					ressumm = summ;
					result = listener;
				}
			}

		}
		return result;
	}

	private int getSum(String s) {
		int result = 0;
		for (int i = 0; i < s.length(); i++) {
			result += s.charAt(i);
		}
		return result;
	}
}
