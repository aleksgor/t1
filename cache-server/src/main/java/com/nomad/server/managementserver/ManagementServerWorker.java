package com.nomad.server.managementserver;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.InternalTransactDataStore;
import com.nomad.cache.commonclientserver.ManagementMessageImpl;
import com.nomad.cache.commonclientserver.ResultImpl;
import com.nomad.communication.MessageExecutor;
import com.nomad.communication.NetworkServer;
import com.nomad.exception.SystemException;
import com.nomad.io.MessageInputStream;
import com.nomad.message.ManagementMessage;
import com.nomad.message.OperationStatus;
import com.nomad.message.Result;
import com.nomad.model.CommandPluginModelImpl;
import com.nomad.model.ConnectModel;
import com.nomad.model.ConnectModelImpl;
import com.nomad.model.ConnectStatus;
import com.nomad.model.Identifier;
import com.nomad.model.ListenerModel;
import com.nomad.model.ManagerCommand;
import com.nomad.model.MemoryInfo;
import com.nomad.model.ModelSource;
import com.nomad.model.ServerModel;
import com.nomad.model.management.ManagementServerModel;
import com.nomad.server.ServerContext;
import com.nomad.server.ServerContext.ServiceName;
import com.nomad.server.ServerListener;
import com.nomad.server.StoreModelService;
import com.nomad.server.service.ChildrenServerService;
import com.nomad.server.service.commandplugin.CommandPluginService;
import com.nomad.server.service.storemodelservice.StoreModelServiceImpl;

public class ManagementServerWorker implements MessageExecutor<ManagementMessage, ManagementMessage> {
    private static Logger LOGGER = LoggerFactory.getLogger(ManagementServerWorker.class);
    private final StoreModelService storeModelService;
    @SuppressWarnings("unused")
    private final ManagementServerModel serverModel;
    private volatile InternalTransactDataStore store;
    private final ServerContext context;
    private static final Class<?>[] parameters = new Class<?>[] { URL.class };
    @SuppressWarnings("unused")
    private final NetworkServer server;

    public ManagementServerWorker(final ServerContext context, final NetworkServer server, final int threadId, final ManagementServerModel serverModel) {
        storeModelService = (StoreModelService) context.get(ServerContext.ServiceName.STORE_MODEL_SERVICE);
        this.serverModel = serverModel;
        store = (InternalTransactDataStore) context.get(ServerContext.ServiceName.INTERNAL_TRANSACT_DATA_STORE);
        this.context = context;
        this.server = server;

    }

    @Override
    public ManagementMessage execute(final ManagementMessage messageIn) {
        final ManagerCommand managerCommand = ManagerCommand.valueOf(messageIn.getCommand());
        ManagementMessage answer = null;
        Result result = null;
        try {
            switch (managerCommand) {
            case GET_SERVER_INFO:
                answer = new ManagementMessageImpl(messageIn.getCommand(), storeModelService.getServerModel(), new ResultImpl(OperationStatus.OK));
                break;
            case CLEAN_CACHE:
                Integer percent = 100;
                final Object oPercent = messageIn.getData();
                if (oPercent != null) {
                    percent = Integer.parseInt(oPercent.toString());
                }
                store.cleanOldData(percent);
                answer = new ManagementMessageImpl(messageIn.getCommand(), oPercent, new ResultImpl(OperationStatus.OK));
                break;
            case GET_MEMORY_INFO:

                final Runtime runtime = Runtime.getRuntime();
                final MemoryInfo data = new MemoryInfo();
                data.setFreeMemory(runtime.freeMemory());
                data.setMaxMemory(runtime.maxMemory());
                data.setTotalMemory(runtime.totalMemory());
                data.setDate(new Date(System.currentTimeMillis()));
                answer = new ManagementMessageImpl(messageIn.getCommand(), data, new ResultImpl(OperationStatus.OK));

                break;
            case GET_OBJECT_COUNT_INFO:
                final Map<String, Integer> info = store.getObjectsCount();
                answer = new ManagementMessageImpl(messageIn.getCommand(), info, new ResultImpl(OperationStatus.OK));

                break;
            case GET_CACHE_KEYS:
                final String modelName = messageIn.getData().toString();
                final Set<Identifier> ids = store.getIdentifiers(modelName);
                answer = new ManagementMessageImpl(messageIn.getCommand(), ids, new ResultImpl(OperationStatus.OK));
                break;
            case PING:
                answer = new ManagementMessageImpl(messageIn.getCommand(), messageIn.getData(), new ResultImpl(OperationStatus.OK));
                break;
            case UPLOAD_PLUGIN:
                try {
                    uploadJar(new ByteArrayInputStream((byte[]) messageIn.getData()), messageIn.getModelName());
                    answer = new ManagementMessageImpl(messageIn.getCommand(), null, new ResultImpl(OperationStatus.OK));

                } catch (final Exception x) {
                    LOGGER.error(x.getMessage(), x);
                }

            case CHECK_SESSIONS:
                // TODO
                break;
            case REGISTER_COMMAND:
                final CommandPluginService commandPluginService = (CommandPluginService) context.get(ServerContext.ServiceName.PROXY_PLUGIN);
                commandPluginService.loadPlugin((CommandPluginModelImpl) messageIn.getData(), context);
                answer = new ManagementMessageImpl(messageIn.getCommand(), null, new ResultImpl(OperationStatus.OK));

                break;
            case REGISTER_CLIENT:
                try {
                    result = registerClient((ConnectModelImpl) messageIn.getData());
                } catch (final Exception x) {
                    LOGGER.error(x.getMessage(), x);
                    result = new ResultImpl(OperationStatus.ERROR, x.getMessage());
                    result.setMessage(x.getMessage());
                }
                answer = new ManagementMessageImpl(messageIn.getCommand(), messageIn.getData(), result);
                break;
            case REGISTER_SERVER:
                ConnectModel connectModel = null;
                try {
                    connectModel = registerServer(messageIn.getData());
                    result = new ResultImpl(OperationStatus.OK);
                } catch (final Exception x) {
                    LOGGER.error(x.getMessage(), x);
                    result = new ResultImpl(OperationStatus.ERROR, x.getMessage());
                }
                answer = new ManagementMessageImpl(messageIn.getCommand(), connectModel, result);

                break;
            case REGISTER_MODEL:
                final ModelSource modelSource = (ModelSource) messageIn.getData();
                store.registerModel(modelSource.getStoreModel(), modelSource.getDataSourceModel());
                answer = new ManagementMessageImpl(messageIn.getCommand(), modelSource, new ResultImpl(OperationStatus.OK));
                break;
            case CHANGE_LISTENER_STATUS:
                final int[] newStatus = (int[]) messageIn.getData(); // port/new
                // status
                final int status = setNewStatus(newStatus[0], newStatus[1]);
                answer = new ManagementMessageImpl(messageIn.getCommand(), status, new ResultImpl(OperationStatus.OK));
                break;
            }

        } catch (final Throwable e) {
            LOGGER.error(e.getMessage(), e);
            answer = new ManagementMessageImpl(messageIn.getCommand(), null, new ResultImpl(OperationStatus.ERROR, e.getMessage()));
        }

        return answer;
    }

    private void uploadJar(final InputStream input, final String filename) throws SystemException {
        final byte[] bytes = new byte[1024];
        int counter = 0;
        try {
            @SuppressWarnings("resource")
            final MessageInputStream messageInputStream = new MessageInputStream(input, context.getDataDefinitionService(null));
            long length = messageInputStream.readLong();

            final File newJar = new File(storeModelService.getServerModel().getPluginPath() + File.separator + filename);
            final OutputStream out = new FileOutputStream(newJar);
            while (length > 0) {
                counter = input.read(bytes);
                length = length - counter;
                out.write(bytes, 0, counter);
            }
            out.flush();
            out.close();

            addURL(newJar);
            Object o = Class.forName("com.nomad.plugin.IdGenerator");
            LOGGER.debug(o.getClass().getName());
            final Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass("com.nomad.plugin.IdGenerator");
            o = clazz.newInstance();
            LOGGER.debug(o.getClass().getName());
            LOGGER.debug(o.toString());
        } catch (IOException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new SystemException(e);
        }

    }

    public static void addURL(final File newJar) throws IOException {

        final URL u = newJar.toURI().toURL();
        final URLClassLoader systemLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        final Class<URLClassLoader> systemClass = URLClassLoader.class;

        try {
            final Method method = systemClass.getDeclaredMethod("addURL", parameters);
            method.setAccessible(true);
            method.invoke(systemLoader, new Object[] { u });
        } catch (final Throwable t) {
            LOGGER.error(t.getMessage(), t);
            throw new IOException("Error, could not add URL to system classloader"+ u);
        }

    }

    private Result registerClient(final ConnectModelImpl colleague)  {

        final StoreModelServiceImpl storeService = (StoreModelServiceImpl) context.get(ServiceName.STORE_MODEL_SERVICE);
        final ServerModel thisServer = storeService.getServerModel();

        final List<ListenerModel> listeners = thisServer.getListeners();
        for (final ListenerModel listenerModel : listeners) {
            if (listenerModel.getHost().equals(colleague.getListener().getHost()) && listenerModel.getPort() == colleague.getListener().getPort()) {
                LOGGER.info(" Client successful registered :{}", colleague);
                return new ResultImpl(OperationStatus.OK);
            }
        }
        LOGGER.error("No matches: server :{} listeners: {}", colleague, listeners);
        return new ResultImpl(OperationStatus.ERROR, "No matches: server : " + colleague + " listeners:" + listeners);
    }

    private ConnectModel registerServer(final Object data) throws SystemException  {
        final ConnectModel connect = (ConnectModel) data;

        final ChildrenServerService childService = (ChildrenServerService) context.get(ServiceName.CHILDREN_SERVICE);
        childService.registerClient(connect);
        // for answer
        final StoreModelService storeModelService = (StoreModelService) context.get(ServiceName.STORE_MODEL_SERVICE);

        final ConnectModel registered = storeModelService.registerServer(connect);
        registered.setStatus(ConnectStatus.OK);

        LOGGER.info(" server sucsessful refistred :{}", connect);

        return connect;
    }

    @SuppressWarnings("unchecked")
    private int setNewStatus(final int port, final int newStatus) {
        LOGGER.info(" set new status:" + newStatus + " for " + storeModelService.getServerModel().getServerName());
        final List<ServerListener> listeners = (List<ServerListener>) context.get(ServerContext.ServiceName.LISTENERS);
        for (final ServerListener serverListener : listeners) {
            if (serverListener.getListener().getPort() == port) {
                if (newStatus == 0 && serverListener.getListener().getStatus() == 1) {
                    serverListener.stop();
                    serverListener.getListener().setStatus(0);
                    return 0;
                } else if (newStatus == 1 && serverListener.getListener().getStatus() == 0) {
                    serverListener.start();
                    serverListener.getListener().setStatus(1);
                    return 1;
                }
                return serverListener.getListener().getStatus();
            }
        }
        return -1;
    }

    @Override
    public void stop() {

    }
}