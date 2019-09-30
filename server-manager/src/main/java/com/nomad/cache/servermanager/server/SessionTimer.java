package com.nomad.cache.servermanager.server;

import java.util.List;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.cache.commonClientServer.MessageImpl;
import com.nomad.message.MessageHeader;
import com.nomad.model.BaseCommand;
import com.nomad.server.ProxyServerContext;
import com.nomad.cache.servermanager.models.SessionStorage;

public class SessionTimer extends TimerTask {
	protected static Logger logger = LoggerFactory.getLogger(ProxyServer.class);
	private volatile SessionStorage sessionStorage;
	private ManagerImpl executor;
	private ProxyServerContext context;

	private ManagerImpl getManagerImpl() {
		if (executor == null) {
			executor = new ManagerImpl(context);
		}
		return executor;
	}

	public SessionTimer(ProxyServerContext context) {
	  this.context=context;
		this.sessionStorage =(SessionStorage) context.get(ProxyServerContext.ServiceName.SessionStorage.toString());

	}

	@Override
	public void run() {
		logger.debug("try clean sessions");
		List<String> olsSessions = sessionStorage.getExpiredSessions();
		logger.debug("expired sessions: {}", olsSessions);
		if (!olsSessions.isEmpty()) {
			try {
			  MessageHeader header = new MessageHeader();
			  header.setCommand(BaseCommand.CloseSessions);
			  header.setSessionId(olsSessions.get(0));
				getManagerImpl().sendBroadcastMessage(header, new MessageImpl( olsSessions));
				sessionStorage.removeSession(olsSessions);
			} catch (Exception e) {
				logger.error("error close sessions", e);
			}

		}

	}
}
