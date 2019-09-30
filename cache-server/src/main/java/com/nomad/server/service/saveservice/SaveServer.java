package com.nomad.server.service.saveservice;

import java.util.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.communication.NetworkServer;
import com.nomad.communication.ServerService;
import com.nomad.communication.binders.ServerFactory;
import com.nomad.exception.SystemException;
import com.nomad.io.serializer.SerializerFactory;
import com.nomad.message.SaveRequest;
import com.nomad.message.SaveResult;
import com.nomad.model.CommonServerModel;
import com.nomad.model.saveserver.SaveServerModel;
import com.nomad.server.SaveService;
import com.nomad.server.ServerContext;
import com.nomad.server.service.saveservice.model.SaveRequestImpl;
import com.nomad.server.service.saveservice.model.SaveRequestSerializer;
import com.nomad.server.service.saveservice.model.SaveResultImpl;
import com.nomad.server.service.saveservice.model.SaveResultSerializer;
public class  SaveServer  implements ServerService <SaveRequest  ,SaveResult > {
    private static Logger LOGGER = LoggerFactory.getLogger(SaveServer.class);

    private volatile SaveServerStore store;
    private Timer cleanerTimer;
    private NetworkServer server;
    private final SaveServerModel serverModel;
    private final ServerContext context;
    private Thread serverThread;

    public SaveServer(final SaveServerModel serverModel, final ServerContext context)  {
        this.serverModel=serverModel;
        this.context=context;
        SerializerFactory.registerSerializer(SaveRequestImpl.class, SaveRequestSerializer.class);
        SerializerFactory.registerSerializer(SaveResultImpl.class, SaveResultSerializer.class);
        final SaveService client = new SaveServiceImpl(serverModel.getMirrors(), context);
        store = new SaveServerStore(client, serverModel.getSessionTimeout());
        LOGGER.info("SaveServer created");
    }

    @Override
    public void start() throws SystemException {
        LOGGER.info("SaveServer starting");
        server = ServerFactory.getServer(serverModel, context, "SaveServer", new SaveServerWorkerFactory(store));
        serverThread = new Thread(server);
        serverThread.setName(serverModel.getHost()+":"+serverModel.getPort());
        serverThread.start();
        cleanerTimer = new Timer();
        cleanerTimer.schedule(new SaveSessionCleanerTimer(store, serverModel.getSessionTimeout(), context.getServerModel().getServerName()),
                serverModel.getSessionTimeout(), serverModel.getSessionTimeout());
        LOGGER.info("SaveServer started");
    }

    public CommonServerModel getSaveServerModel() {
        return serverModel;
    }

    @Override
    public void stop() {
        LOGGER.info("SaveServer stopping");
        if (server != null) {
            server.close();
        }
        if (cleanerTimer != null) {
            cleanerTimer.cancel();
        }
        LOGGER.info("SaveServer stopped");
    }

}
