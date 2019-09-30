package com.nomad.cache.test;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.model.DataSourceModelImpl;
import com.nomad.model.ListenerModelImpl;
import com.nomad.model.ServerModelImpl;
import com.nomad.model.StoreModelImpl;
import com.nomad.model.management.ManagementServerModel;
import com.nomad.model.management.ManagementServerModelImpl;
import com.nomad.server.ServerLauncher;

public class FullStart  {
    protected static Logger LOGGER = LoggerFactory.getLogger(FullStart.class);

    private static ServerLauncher launcher;
    private static ServerLauncher launcher2;
    private static String host;


    /**
     * @param args
     */
    public static void main(final String[] args) {
        final FullStart test = new FullStart();
        test.start();
    }

    private void start() {
        try {

            host = InetAddress.getLocalHost().getHostName();
            final ListenerModelImpl listener = new ListenerModelImpl();
            listener.setPort(2222);
            listener.setMinThreads(15);
            listener.setMaxThreads(15);
            listener.setBacklog(10);


            final List<StoreModelImpl> models1 = new ArrayList<>();
            models1.add(getStoreData("Test", "com.nomad.cache.test.model"));
            final List<StoreModelImpl> models2 = new ArrayList<>();
            models2.add(getStoreData("Child", "com.nomad.cache.test.model"));
            models2.add(getStoreData("Test", "com.nomad.cache.test.model"));

            final ServerModelImpl serverModel = new ServerModelImpl();
            serverModel.getStoreModels().addAll(models1);
            serverModel.getListeners().add(listener);
            serverModel.setManagementServerModel(getManagementServerModel(2225, host, 2, 1000));

            final ListenerModelImpl listener1 = new ListenerModelImpl();
            listener1.setPort(2422);
            listener1.setMinThreads(15);
            listener1.setMaxThreads(15);
            listener1.setBacklog(10000);

            final DataSourceModelImpl dataSourceModel = new DataSourceModelImpl();
            dataSourceModel.setName("a");
            dataSourceModel.setClazz("com.nomad.cache.test.userdataaccess.BaseDataInvoker");
            dataSourceModel.setThreads(10);
            dataSourceModel.addProperty("user", "test");
            dataSourceModel.addProperty("password", "test");
            dataSourceModel.addProperty("url", "jdbc:postgresql://localhost:5432/test");
            dataSourceModel.addProperty("driver", "org.postgresql.Driver");
            dataSourceModel.addProperty("threads", "20");

            serverModel.addDataSources(dataSourceModel);

            launcher = new ServerLauncher(serverModel);

            launcher.start();


            final ServerModelImpl serverModel2 = new ServerModelImpl();
            serverModel2.getStoreModels().addAll(models2);
            serverModel2.getListeners().add(listener1);
            serverModel2.setManagementServerModel(getManagementServerModel(2425, host, 2, 2000));

            serverModel2.addDataSources(dataSourceModel);
            launcher2 = new ServerLauncher(serverModel2);
            launcher2.start();


        } catch (final Throwable e) {
            LOGGER.error(e.getMessage(),e);
        }

    }

    private static StoreModelImpl getStoreData(final String name, final String clazz) {
        final StoreModelImpl storeModel = new StoreModelImpl();
        storeModel.setModel(name);
        storeModel.setClazz(clazz);
        storeModel.setReadThrough(true);
        storeModel.setWriteThrough(true);
        storeModel.setDataSource("a");
        return storeModel;
    }

    protected static ManagementServerModel getManagementServerModel(final int port,final String host,final int threads,final int keepAliveTime){
        final ManagementServerModel managementServer = new ManagementServerModelImpl();
        managementServer.setPort(port);
        managementServer.setHost(host);
        managementServer.setMinThreads(threads);
        managementServer.setMaxThreads(threads);
        managementServer.setKeepAliveTime(keepAliveTime);
        return managementServer;
    }

}
