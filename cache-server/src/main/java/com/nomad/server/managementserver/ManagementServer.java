package com.nomad.server.managementserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.communication.NetworkServer;
import com.nomad.communication.ServerService;
import com.nomad.communication.binders.ServerFactory;
import com.nomad.exception.SystemException;
import com.nomad.message.ManagementMessage;
import com.nomad.model.management.ManagementServerModel;
import com.nomad.server.ServerContext;
import com.nomad.server.sessionserver.SessionServer;

public class ManagementServer implements  ServerService <ManagementMessage, ManagementMessage> {
    @SuppressWarnings("unused")
    private static Logger LOGGER = LoggerFactory.getLogger(SessionServer.class);

    private  NetworkServer server ;
    private final ServerContext context ;
    private final ManagementServerModel serverModel;
    private Thread serverThread;

    public ManagementServer(final ManagementServerModel serverModel, final ServerContext context)  {
        this.context=context;
        this.serverModel=serverModel;

    }

    @Override
    public void stop() {
        if(server!=null){
            server.close();
        }

    }

    @Override
    public void start() throws SystemException {
        final String serverName= serverModel.getHost() + ":" + serverModel.getPort();
        server= ServerFactory.getServer(serverModel, context, "Management server", new ManagementMessageExecutorFactory());
        serverThread  = new Thread(server,"Session server:"+serverName);
        serverThread.setName(serverName);
        serverThread.start();


    }

}
