package com.nomad.server.sessionserver.sessionclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.communication.NetworkServer;
import com.nomad.communication.ServerService;
import com.nomad.communication.binders.ServerFactory;
import com.nomad.exception.SystemException;
import com.nomad.model.ListenerModel;
import com.nomad.model.ServerModel;
import com.nomad.model.SessionCallBackServerModel;
import com.nomad.model.SessionCallBackServerModelImp;
import com.nomad.model.session.SessionAnswer;
import com.nomad.model.session.SessionMessage;
import com.nomad.server.ExecutorServiceProvider;
import com.nomad.server.ServerContext;
import com.nomad.server.Status;
import com.nomad.server.sessionserver.SessionCallBackWorkerFactory;

public class SessionCallBackServer implements ServerService<SessionMessage, SessionAnswer> {

    private static Logger LOGGER = LoggerFactory.getLogger(SessionCallBackServer.class);
    private SessionCallBackServerModel sessionCallBackServerModel;
    private final ServerContext context;
    private Thread serverThread;
    private NetworkServer server;
    private ExecutorServiceProvider executorProvider;

    public SessionCallBackServer(final ServerModel serverModel, final ServerContext context, int threads)  {
        sessionCallBackServerModel = serverModel.getSessionCallBackServerModel();
        if (sessionCallBackServerModel == null) {
            sessionCallBackServerModel = new SessionCallBackServerModelImp();
            sessionCallBackServerModel.setPort(0);
            String host = "localhost";
            for (final ListenerModel listener : serverModel.getListeners()) {
                if (listener.getHost() != null && listener.getHost().length() > 0) {
                    host = listener.getHost();
                }
            }
            sessionCallBackServerModel.setHost(host);
            sessionCallBackServerModel.setMinThreads(threads);
            sessionCallBackServerModel.setMaxThreads(threads);
            serverModel.setSessionCallBackServerModel(sessionCallBackServerModel);

        }
        this.context = context;

    }

    @Override
    public void start() throws SystemException  {
        LOGGER.info("SessionCallBackServer starting");
        executorProvider = context.getExecutorServiceProvider();
        server = ServerFactory.getServer(sessionCallBackServerModel, context, "SessionCallBackServer", new SessionCallBackWorkerFactory<SessionMessage, SessionAnswer>(
                executorProvider));
        serverThread = new Thread(server);
        serverThread.start();
        LOGGER.info("SessionCallBackServer started");
    }

    @Override
    public void stop() {
        LOGGER.info("SessionCallBackServer stopping");
        server.close();
        LOGGER.info("SessionCallBackServer stopped");
    }

    public Status getStatus() {
        return server.getStatus();
    }
}
