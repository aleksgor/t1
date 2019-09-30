package com.nomad.cache.servermanager.server.commands;


import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.model.ChildServer;
import com.nomad.message.ManagerMessage;
import com.nomad.cache.servermanager.models.ProxyServerModel;
import com.nomad.cache.servermanager.server.connectpool.StoreConnectionPool;

public class GetListCommand  implements ManagerCommand{
	private static Logger logger = LoggerFactory.getLogger(GetListCommand.class);

	public ManagerMessage execute(ProxyServerModel managerServer, ManagerMessage message) {
	
		List<StoreConnectionPool> pools=managerServer.getConnectPool();
		List<ChildServer> servers= new ArrayList<ChildServer>(pools.size());
		for (StoreConnectionPool connectPool : pools) {
			servers.add(connectPool.getServer());
		}
		logger.debug("GetListCommand: {}",servers);

		message.setData(servers);
		message.setAnswer(ManagerMessage.Answer.OK);
		return message;
		
	}
	
}
