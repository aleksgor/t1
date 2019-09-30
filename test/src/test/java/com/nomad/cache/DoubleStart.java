package com.nomad.cache;

import java.util.ArrayList;
import java.util.List;

import com.nomad.cache.client.CommonTest;
import com.nomad.model.CommandPluginModelImpl;
import com.nomad.model.DataSourceModelImpl;
import com.nomad.model.ListenerModelImpl;
import com.nomad.model.ServerModel;
import com.nomad.model.ServerModelImpl;
import com.nomad.model.StoreModelImpl;
import com.nomad.server.ServerLauncher;

public class DoubleStart extends CommonTest {


    private static ServerLauncher launcher;
    private static ServerLauncher launcher2;

    /**
     * @param args
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception {
        final DoubleStart test = new DoubleStart();
        test.start();
    }


    private void start() throws Exception {
        registerSerialized();

        version = 0x2;

        final List<StoreModelImpl> models1 = new ArrayList<>();
        models1.add(getStoreData("Test", "com.nomad.cache.test.model.Test"));
        final List<StoreModelImpl> models2 = new ArrayList<>();
        models2.add(getStoreData("Child", "com.nomad.cache.test.model.Child"));
        models2.add(getStoreData("Test", "com.nomad.cache.test.model.Test"));

        final ServerModel serverModel = new ServerModelImpl();

        serverModel.getStoreModels().addAll(models1);
        serverModel.getListeners().add(getListenerModel(host,2222, 5));
        serverModel.getListeners().add(getListenerModel(host,2232, 5));
        serverModel.setManagementServerModel(getManagementServerModel(2242, host, 2, 2000));
        serverModel.setServerName("main");
        serverModel.getProperties().setProperty("SessionStorePath", "/opt/t1/sessionstore/1");


        final DataSourceModelImpl dataSource = new DataSourceModelImpl();
        dataSource.setName("a");
        dataSource.setThreads(10);
        dataSource.setClazz("com.nomad.cache.test.userdataaccess.BaseDataInvoker");
        dataSource.addProperty("user", "sa");
        dataSource.addProperty("password", "");
        dataSource.addProperty("url", "jdbc:hsqldb:hsql://localhost/test");
        dataSource.addProperty("driver", "org.hsqldb.jdbcDriver");
        serverModel.addDataSources(dataSource);

        final CommandPluginModelImpl plugin = new CommandPluginModelImpl();
        plugin.setCheckDelay(10);
        plugin.setClazz("com.nomad.plugin.IdGenerator");
        plugin.setPoolSize(10);
        plugin.getProperties().put("DataSourceName", "a");
        plugin.setTimeout(10);

        final CommandPluginModelImpl proxyPlugin = new CommandPluginModelImpl();
        proxyPlugin.setClazz("com.nomad.plugin.IdGeneratorProxy");
        proxyPlugin.setCheckDelay(1000);
        proxyPlugin.setPoolSize(10);
        proxyPlugin.setTimeout(600000);

        serverModel.getCommandPlugins().add(plugin);


        launcher = new ServerLauncher(serverModel);

        launcher.start();

        final ServerModel serverModel2 = new ServerModelImpl();
        serverModel2.getStoreModels().addAll(models2);
        final ListenerModelImpl listener=getListenerModel(host,2422, 5);
        serverModel2.getListeners().add(listener);
        serverModel2.getListeners().add(getListenerModel(host,2432, 5));
        serverModel2.setManagementServerModel(getManagementServerModel(2442, host, 2, 2000));
        serverModel2.setServerName("second");

        serverModel2.addDataSources(dataSource);

        serverModel2.getCommandPlugins().add(plugin);
        serverModel2.getProperties().setProperty("SessionStorePath", "/opt/t1/sessionstore/2");

        serverModel2.getServers().add(getConnectModel(serverModel2, serverModel, 8,listener));


        launcher2 = new ServerLauncher(serverModel2);

        launcher2.start();


    }



}
