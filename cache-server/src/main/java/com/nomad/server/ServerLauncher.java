package com.nomad.server;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.InternalTransactDataStore;
import com.nomad.client.ServerManagerClient;
import com.nomad.configuration.StoreFactory;
import com.nomad.datadefinition.DataDefinitionServiceImpl;
import com.nomad.exception.SystemException;
import com.nomad.io.serializer.SerializerFactory;
import com.nomad.message.MessageSenderReceiverImpl;
import com.nomad.model.ListenerModel;
import com.nomad.model.ServerModel;
import com.nomad.model.idgenerator.IdGeneratorService;
import com.nomad.model.saveserver.SaveServerModel;
import com.nomad.model.session.SessionClientModel;
import com.nomad.model.session.SessionServerModel;
import com.nomad.model.session.SessionServerModelImp;
import com.nomad.server.ServerContext.ServiceName;
import com.nomad.server.idgenerator.IdGeneratorServer;
import com.nomad.server.managementserver.ManagementServer;
import com.nomad.server.mbean.StatusMBean;
import com.nomad.server.mbean.StatusMXBean;
import com.nomad.server.service.ChildrenServerService;
import com.nomad.server.service.ManagementService;
import com.nomad.server.service.blockservice.LocalBlockServiceImpl;
import com.nomad.server.service.childserver.ChildrenServiceConnectionImpl;
import com.nomad.server.service.commandplugin.CommandPluginService;
import com.nomad.server.service.idgenerator.IdGeneratorServiceFactory;
import com.nomad.server.service.management.ManagementServiceImpl;
import com.nomad.server.service.saveservice.SaveServer;
import com.nomad.server.service.saveservice.SaveServiceImpl;
import com.nomad.server.service.session.SessionServiceFactory;
import com.nomad.server.service.storemodelservice.StoreModelServiceImpl;
import com.nomad.server.sessionserver.SessionServer;
import com.nomad.server.sessionserver.sessionclient.SessionCallBackServer;
import com.nomad.server.statistic.JavaVMInformationImplMBean;
import com.nomad.store.transaction.TransactElementImpl;
import com.nomad.store.transaction.TransactElementSerializer;

public class ServerLauncher {
    private static Logger LOGGER = LoggerFactory.getLogger(ServerLauncher.class);
    private CommandServer cServer = null;
    private SessionServer sessionServer = null;
    private final List<SaveServer> saveServers = new ArrayList<>();

    private Timer cleanerTimer;

    private ManagementServer managementServer = null;
    private List<ServerListener> listenerImplements;
    private ServerContext context;
    private final ServerModel serverModel;
    private ScheduledFuture<?> statisticFuture;
    private ServerContext sessionContext;

    private IdGeneratorServer idGeneratorServer;
    private SessionCallBackServer sessionCallBack;

    public ServerLauncher(final ServerModel model) {
        serverModel = model;
    }

    private ServerListener getListener(final ListenerModel listener, final ServerContext context) throws SystemException {
        final ServerListener listenerImpl = CacheServerFactory.getServer(listener);
        listenerImpl.setServerContext(context);
        listenerImpl.setListener(listener);
        return listenerImpl;
    }

    public void start() throws SystemException {
        SerializerFactory.registerSerializer(TransactElementImpl.class.getName(), TransactElementSerializer.class.getName());
        start(serverModel);
    }

    private void start(final ServerModel serverModel) throws SystemException {

        try {
            if (serverModel.getServerId() == 0) {
                serverModel.setServerId(System.nanoTime());
            }
            LOGGER.info("Start " + serverModel.getServerName());

            checkSessionModels(serverModel);

            SerializerFactory.registerSerializer(TransactElementImpl.class.getName(), TransactElementSerializer.class.getName());
            context = new ServerContextImpl();
            StatusMXBean status = new StatusMBean();
            
            final StoreModelService storeModelService = new StoreModelServiceImpl(serverModel);
            context.put(ServiceName.STORE_MODEL_SERVICE, storeModelService);
            
            status.setStatus(Status.STARTING.name());
            context.getInformationPublisherService().publicData(status, serverModel.getServerName(), "Status", "");
            final ManagementService managementService = new ManagementServiceImpl(context);
            context.put(ServiceName.MANAGEMENT_SERVICE, managementService);

            try {
                final StatisticPublisher statisticPublisher = new StatisticPublisher(serverModel.getServerName());
                statisticFuture = context.getScheduledExecutorService().scheduleAtFixedRate(statisticPublisher, 10, TimeUnit.SECONDS);
            } catch (final Throwable e) {
                LOGGER.error(e.getMessage(), e);
            }

            final DataDefinitionService dataDefinitionService = new DataDefinitionServiceImpl(null, "model.xml", null);
            dataDefinitionService.start();
            context.putDataDefinitionService(dataDefinitionService, null);

            // init dataSources
            storeModelService.start();

            if (serverModel.getSaveClientModels().size() > 0) {
                final SaveService saveService = new SaveServiceImpl(serverModel.getSaveClientModels(), context);
                saveService.start();
                context.put(ServiceName.SAVE_SERVICE, saveService);
            }
            context.put(ServiceName.BLOCK_SERVICE, new LocalBlockServiceImpl(serverModel, context));

            // may be last
            final StoreFactory factory = new StoreFactory(context);
            final InternalTransactDataStore store = factory.getStore(StoreFactory.StoreType.TRANSACT);
            context.put(ServiceName.INTERNAL_TRANSACT_DATA_STORE, store);

            final SessionServerModel sessionServerModel = serverModel.getSessionServerModel();

            if (sessionServerModel != null && sessionServerModel.getPort() > 0) {
                LOGGER.info("Start Session server");
                sessionContext = new ServerContextImpl();
                sessionContext.putDataDefinitionService(dataDefinitionService, null);
                sessionContext.put(ServiceName.STORE_MODEL_SERVICE, storeModelService);
                sessionServer = new SessionServer(serverModel.getSessionServerModel(), sessionContext);
                sessionServer.start();
            }

            final SessionService sessionService = SessionServiceFactory.getSessionService(serverModel.getSessionClientModels(), sessionServerModel, context,
                    serverModel.isTrustSessions());
            System.out.println("serverModel:"+serverModel.getServerName()+" sessionService:"+sessionService);
            context.put(ServiceName.SESSION_SERVICE, sessionService);
            if (sessionService != null) {
                sessionService.start();
            }

            final ChildrenServerService childrenService = new ChildrenServiceConnectionImpl(serverModel, context);
            context.put(ServiceName.CHILDREN_SERVICE, childrenService);

            if (serverModel.getSaveServerModels().size() > 0) {
                for (final SaveServerModel server : serverModel.getSaveServerModels()) {
                    final SaveServer saveServer = new SaveServer(server, context);
                    saveServers.add(saveServer);
                    saveServer.start();

                }

            }

            final List<ListenerModel> listeners = serverModel.getListeners();
            listenerImplements = new ArrayList<>(listeners.size());
            LOGGER.info("Start object server");
            for (final ListenerModel listener : listeners) {
                listenerImplements.add(getListener(listener, context));
            }

            for (final ServerListener serverListener : listenerImplements) {
                LOGGER.info("Start listener: " + serverListener.getListener());
                if (serverListener.getListener().getStatus() == 1) {
                    serverListener.start();
                    final Thread thread = new Thread(serverListener, "Listener: host " + serverListener.getThreadName());
                    thread.start();
                }
            }

            // wait start listener!
            for (final ServerListener serverListener : listenerImplements) {
                while (!Status.READY.equals(serverListener.getStatus())) {
                    try {
                        Thread.sleep(100);
                    } catch (final InterruptedException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }

            }
            context.put(ServerContext.ServiceName.LISTENERS, listenerImplements);

            LOGGER.info("Start object server");

            if (serverModel.getCommandServerModel() != null) {
                LOGGER.info("Start command server");
                cServer = new CommandServer(serverModel, store);
                final Thread commandServer = new Thread(cServer);
                commandServer.setName("command server host:" + serverModel.getCommandServerModel().getHost() + " port:" + serverModel.getCommandServerModel().getPort());
                commandServer.start();
            }

            if (serverModel.getManagementServerModel() != null) {
                LOGGER.info("Start management server " + serverModel.getManagementServerModel());
                managementServer = new ManagementServer(serverModel.getManagementServerModel(), context);
                managementServer.start();

            }

            loadPlugins(serverModel, context);
            // try to restore stored sessions
            // resstoreOldSessions(store, serverModel.getProperties(),
            // dataDefinitionService);

            final ServerManagerClient serverManagerClient = new ServerManagerClient(context);

            serverManagerClient.registerServerAndClient(serverModel);

            // sessioncallBack
            if (!SessionServiceFactory.checkLocal(serverModel.getSessionClientModels())) {
                int threads = 0;
                for (SessionClientModel sessionModel : serverModel.getSessionClientModels()) {
                    threads += sessionModel.getThreads();
                }
                LOGGER.info("Start call back Session server");
                sessionCallBack = new SessionCallBackServer(serverModel, context, threads);

                sessionCallBack.start();
                // wait start

                while (sessionCallBack.getStatus().equals(Status.SHUTDOWN)) {
                    Thread.sleep(100);
                }
            }
            // start idGenerator
            if (serverModel.getIdGeneratorServerModel() != null) {
                if (serverModel.getIdGeneratorServerModel().getPort() > 0) {
                    LOGGER.info(context.getServerName() + ": Start IdGenerator server :" + serverModel.getIdGeneratorServerModel());
                    idGeneratorServer = new IdGeneratorServer(serverModel.getIdGeneratorServerModel(), context);
                    idGeneratorServer.start();
                }
            }
            IdGeneratorService idGeneratorService = IdGeneratorServiceFactory.getIdGeneratorService(serverModel, context);
            if (idGeneratorService != null) {
                LOGGER.info(context.getServerName() + ": Start IdGenerator service :" + idGeneratorService.getClass().getName());
                idGeneratorService.start();
                context.put(ServiceName.ID_GENERATOR_SERVICE, idGeneratorService);
            }
            //
            LOGGER.info("Try register in Session Call back Server:" + serverModel.getSessionCallBackServerModel());

            if (serverModel.getSessionCallBackServerModel() != null) {
                LOGGER.info(" Register in Session Call back Server:" + sessionService.getClass().getName());
                sessionService.serverRegistering(serverModel.getSessionCallBackServerModel());
            }
            LOGGER.info(" Object server started");
            status.setStatus(Status.STARTED.name());
            context.getInformationPublisherService().publicData(status, serverModel.getServerName(), "Status", "");

        } catch (final Throwable e) {
            LOGGER.error(e.getMessage(), e);
            stop();
        }

    }

    private void checkSessionModels(final ServerModel serverModel) {
        SessionServerModel sessionServerModel = serverModel.getSessionServerModel();
        if (sessionServerModel == null && serverModel.isLocalSessions()) {
            sessionServerModel = new SessionServerModelImp();
            sessionServerModel.setSessionTimeLive(30000);
            serverModel.setSessionServerModel(sessionServerModel);
        }

    }

    public void stop() {
        try {
            StatusMXBean status = new StatusMBean();
            status.setStatus(Status.SHUTDOWNING.name());
            if (context != null && serverModel != null) {
                context.getInformationPublisherService().publicData(status, serverModel.getServerName(), "Status", "");
            }

            LOGGER.info("Store server stopped:" + serverModel.getServerName());
            if (statisticFuture != null && context != null) {
                context.getScheduledExecutorService().stop(statisticFuture);
            }
            if (context != null) {
                if (sessionCallBack != null) {
                    sessionCallBack.stop();
                }
            }

            if (managementServer != null) {
                managementServer.stop();
            }

            if (cleanerTimer != null) {
                cleanerTimer.cancel();
            }

            if (cServer != null) {
                cServer.stop();
            }

            if (sessionServer != null) {
                sessionServer.stop();
            }
            if (listenerImplements != null) {
                for (final ServerListener listener : listenerImplements) {
                    listener.stop();
                }
            }
            if (saveServers != null) {
                for (final SaveServer sserver : saveServers) {
                    sserver.stop();
                }
            }
            if (sessionContext != null) {
                sessionContext.close();
            }

            status.setStatus(Status.SHUTDOWN.name());
            if (context != null && serverModel != null) {
                context.getInformationPublisherService().publicData(status, serverModel.getServerName(), "Status", "");
            }
            if (context != null) {
                final CommandPluginService commandPluginService = (CommandPluginService) context.get(ServiceName.PROXY_PLUGIN);
                if (commandPluginService != null) {
                    commandPluginService.stop();
                }

                context.close();
            }

            if (context != null) {
                context.clear();
            }

        } catch (final Throwable e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.info("Store server stopped well");
    }

    private void loadPlugins(final ServerModel serverModel, final ServerContext context) {

        CommandPluginService commandPluginService = (CommandPluginService) context.get(ServiceName.PROXY_PLUGIN);
        if (commandPluginService == null) {
            commandPluginService = new CommandPluginService();
            context.put(ServiceName.PROXY_PLUGIN, commandPluginService);
        }

        commandPluginService.loadPlugins(serverModel.getCommandPlugins(), context);

    }

    // try to restore stored sessions
    @SuppressWarnings("unused")
    private void restoreOldSessions(final InternalTransactDataStore store, final Properties properties, final DataDefinitionService dataDefinition) {
        final String sessionStorePath = properties.getProperty("SessionStorePath", "~/sessionStore");
        final File sessionDirectory = new File(sessionStorePath);
        final FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                return name.endsWith(".session");
            }
        };

        final File[] sessions = sessionDirectory.listFiles(filter);
        if (sessions != null) {
            for (final File file : sessions) {
                try {
                    LOGGER.info("restore:{}", file.getAbsolutePath());
                    new MessageSenderReceiverImpl(dataDefinition);
                } catch (final Exception e) {
                    LOGGER.error(e.getMessage(), e);
                } finally {
                    file.delete();
                }
            }
        }
    }

    private class StatisticPublisher implements Runnable {
        private final JavaVMInformationImplMBean statisticInfo;

        public StatisticPublisher(final String serverName) {
            statisticInfo = new JavaVMInformationImplMBean();
            context.getInformationPublisherService().publicData(statisticInfo, serverName, "Java Info", null);

        }

        @Override
        public void run() {
            final Runtime runtime = Runtime.getRuntime();
            statisticInfo.updateDate(runtime.freeMemory(), runtime.totalMemory(), runtime.maxMemory());
            statisticInfo.setAvailableProcessors(runtime.availableProcessors());

        }
    }

}
