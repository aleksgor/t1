package com.nomad.cache.servermanager.server.commands;

import com.nomad.message.ManagerMessage;
import com.nomad.cache.servermanager.models.ProxyServerModel;

public interface ManagerCommand {
	public ManagerMessage execute(ProxyServerModel managerServer, ManagerMessage message) ;
}
