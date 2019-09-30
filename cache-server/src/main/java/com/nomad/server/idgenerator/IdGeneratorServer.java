package com.nomad.server.idgenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.communication.NetworkServer;
import com.nomad.communication.ServerService;
import com.nomad.communication.binders.ServerFactory;
import com.nomad.exception.SystemException;
import com.nomad.model.idgenerator.IdGeneratorServerModel;
import com.nomad.model.idgenerator.IdGeneratorService;
import com.nomad.model.session.SessionAnswer;
import com.nomad.model.session.SessionMessage;
import com.nomad.server.ServerContext;
import com.nomad.server.service.idgenerator.IdGeneratorServiceImpl;
import com.nomad.server.sessionserver.SessionServer;

public class IdGeneratorServer implements ServerService<SessionMessage, SessionAnswer> {
    private static Logger LOGGER = LoggerFactory.getLogger(SessionServer.class);

    private  NetworkServer server ;
    private final ServerContext context ;
    private Thread serverThread;
    private final IdGeneratorServerModel serverModel;
    private IdGeneratorService generatorService;


    public IdGeneratorServer(final IdGeneratorServerModel serverModel, final ServerContext context)  {

        this.context=context;
        this.serverModel = serverModel;

    }


    @Override
    public void stop() {
        if(server!=null){
            server.close();
        }
    }


    @Override
    public void start() throws SystemException {
        LOGGER.debug("start IdGeneratorServer");

        generatorService = new IdGeneratorServiceImpl(context, serverModel);
        generatorService.start();
        final String serverName = serverModel.getHost() + ":" + serverModel.getPort();
        server = ServerFactory.getServer(serverModel, context, "IdGeneratorServer", new IdGeneratorMessageExecutorFactory(generatorService));
        serverThread = new Thread(server, "IdGeneratorServer server:" + serverName);
        serverThread.setName(serverName + " session server ");
        serverThread.start();


        LOGGER.debug("start IdGeneratorServer OK");

    }

}
