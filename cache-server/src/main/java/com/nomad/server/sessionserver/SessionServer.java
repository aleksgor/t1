package com.nomad.server.sessionserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.communication.NetworkServer;
import com.nomad.communication.ServerService;
import com.nomad.communication.binders.ServerFactory;
import com.nomad.exception.SystemException;
import com.nomad.model.session.SessionAnswer;
import com.nomad.model.session.SessionMessage;
import com.nomad.model.session.SessionServerModel;
import com.nomad.server.ServerContext;
import com.nomad.server.ServerContext.ServiceName;
import com.nomad.server.service.session.server.ServerLocalSessionService;

public class SessionServer implements ServerService<SessionMessage, SessionAnswer> {
    private static Logger LOGGER = LoggerFactory.getLogger(SessionServer.class);

    private volatile ServerLocalSessionService sessionService;
    private final SessionServerCallBackClient sessionCallBackClient;
    private  NetworkServer server ;
    private final ServerContext context ;
    private final SessionServerModel sessionServerModel;
    private Thread serverThread;


    public SessionServer(final SessionServerModel sessionServerModel, final ServerContext context)  {

        sessionCallBackClient = new SessionServerCallBackClient(context, sessionServerModel.getKeepAliveTime());
        sessionService = new ServerLocalSessionService(sessionServerModel, sessionCallBackClient, context);
        context.put(ServiceName.SESSION_SERVICE, sessionService);
        context.put(ServiceName.SESSION_CALLBACK_CLIENT, sessionCallBackClient);
        this.context=context;
        this.sessionServerModel=sessionServerModel;

    }


    @Override
    public void stop() {
        if(server!=null){
            server.close();
        }
        if (sessionCallBackClient != null) {
            sessionCallBackClient.stop();
        }
        if (sessionService != null) {
            sessionService.stop();
        }
    }


    @Override
    public void start() throws SystemException {
        LOGGER.debug("start SessionServer");

        final String serverName= sessionServerModel.getHost() + ":" + sessionServerModel.getPort();
        server= ServerFactory.getServer(sessionServerModel, context, "SessionServer", new SessionMessageExecutorFactory());
        serverThread  = new Thread(server,"Session server:"+serverName);
        serverThread.setName(serverName + " session server ");
        serverThread.start();

        sessionService.start();
        sessionCallBackClient.start();

        LOGGER.debug("start SessionServer OK");

    }

}
