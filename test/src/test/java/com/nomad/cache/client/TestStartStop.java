package com.nomad.cache.client;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import com.nomad.client.SimpleCacheClient;
import com.nomad.model.CommandPluginModelImpl;
import com.nomad.model.ConnectModelImpl;
import com.nomad.model.DataSourceModelImpl;
import com.nomad.model.ListenerModelImpl;
import com.nomad.model.ServerModelImpl;
import com.nomad.model.StoreModelImpl;
import com.nomad.model.session.SessionClientModelImpl;
import com.nomad.model.session.SessionServerModelImp;
import com.nomad.pm.PmDataInvokerFactory;
import com.nomad.server.PmDataInvoker;
import com.nomad.server.ServerLauncher;

public class TestStartStop extends CommonTest{

    private static ServerLauncher launcher;
    private static ServerLauncher launcher2;
    private static ServerLauncher launcher3;

    private static int port1 = 2132;
    private static int port2 = 2232;
    private static int port3 = 2332;
    private static SimpleCacheClient clientMain;
    private static SimpleCacheClient client2;
    private static SimpleCacheClient client3;
    private static PmDataInvoker dataInvoker;

    @org.junit.Test
    public void test1() throws Exception {

        host = InetAddress.getLocalHost().getHostName();

        final SessionServerModelImp sessionServer = new SessionServerModelImp();
        sessionServer.setPort(5888);
        sessionServer.setSessionTimeLive(6000000);
        sessionServer.setMinThreads(15);
        sessionServer.setMaxThreads(15);
        sessionServer.setKeepAliveTime(1000);

        final SessionClientModelImpl sessionClient = new SessionClientModelImpl();
        sessionClient.setHost(host);
        sessionClient.setPort(5888);
        sessionClient.setThreads(5);

        final List<StoreModelImpl> models1 = new ArrayList<>();
        models1.add(getStoreData("MainTestModel", "com.nomad.cache.test.model.MainTestModel"));
        final List<StoreModelImpl> models2 = new ArrayList<>();
        models2.add(getStoreData("Child", "com.nomad.cache.test.model.Child"));
        models2.add(getStoreData("MainTestModel", "com.nomad.cache.test.model.MainTestModel"));

        final DataSourceModelImpl dataSourceModel = new DataSourceModelImpl();
        dataSourceModel.setName("a");
        dataSourceModel.setThreads(10);
        dataSourceModel.setTimeOut(10000);
        dataSourceModel.setClazz("com.nomad.cache.test.userdataaccess.BaseDataInvoker");
        dataSourceModel.addProperty("user", "sa");
        dataSourceModel.addProperty("password", "");
        dataSourceModel.addProperty("url", "jdbc:hsqldb:hsql://localhost/test");
        dataSourceModel.addProperty("driver", "org.hsqldb.jdbcDriver");

        final ServerModelImpl serverModel = new ServerModelImpl();
        serverModel.getStoreModels().addAll(models1);
        serverModel.getListeners().add(getListenerModel(host,port1, 5));
        serverModel.setManagementServerModel(getManagementServerModel(2142, host, 2, 2000));
        serverModel.setServerName("main");
        serverModel.getProperties().setProperty("SessionStorePath", "/opt/t1/sessionstore/1");
        serverModel.setSessionServerModel(sessionServer);
        serverModel.getSessionClientModels().add(sessionClient);
        serverModel.getSaveClientModels().add(getSaveClientModel(host,5224, 5));

        serverModel.addDataSources(dataSourceModel);

        final CommandPluginModelImpl plugin = new CommandPluginModelImpl();
        plugin.setCheckDelay(10);
        plugin.setClazz("com.nomad.plugin.IdGenerator");
        plugin.setPoolSize(10);
        plugin.getProperties().put("DataSourceName", "a");
        plugin.setTimeout(10);

        final CommandPluginModelImpl proxyPlugin = new CommandPluginModelImpl();
        proxyPlugin.setClazz("com.nomad.plugin.IdGeneratorProxy");
        proxyPlugin.setCheckDelay(10);
        proxyPlugin.setPoolSize(10);
        proxyPlugin.setTimeout(60);

        serverModel.getCommandPlugins().add(plugin);

        launcher = new ServerLauncher(serverModel);

        launcher.start();
        Thread.sleep(1400);
        launcher.stop();

    }

    @org.junit.Test
    public void testFull() throws Exception {

        host = InetAddress.getLocalHost().getHostName();

        final SessionServerModelImp sessionServer = new SessionServerModelImp();
        sessionServer.setPort(5888);
        sessionServer.setSessionTimeLive(6000000);
        sessionServer.setMinThreads(15);
        sessionServer.setMaxThreads(15);
        sessionServer.setKeepAliveTime(1000);

        final SessionClientModelImpl sessionClient = new SessionClientModelImpl();
        sessionClient.setHost(host);
        sessionClient.setPort(5888);
        sessionClient.setThreads(5);

        final List<StoreModelImpl> models1 = new ArrayList<>();
        models1.add(getStoreData("MainTestModel", "com.nomad.cache.test.model.MainTestModel"));
        final List<StoreModelImpl> models2 = new ArrayList<>();
        models2.add(getStoreData("Child", "com.nomad.cache.test.model.Child"));
        models2.add(getStoreData("MainTestModel", "com.nomad.cache.test.model.MainTestModel"));

        final DataSourceModelImpl dataSourceModel = new DataSourceModelImpl();
        dataSourceModel.setName("a");
        dataSourceModel.setThreads(10);
        dataSourceModel.setTimeOut(10000);
        dataSourceModel.setClazz("com.nomad.cache.test.userdataaccess.BaseDataInvoker");
        dataSourceModel.addProperty("user", "sa");
        dataSourceModel.addProperty("password", "");
        dataSourceModel.addProperty("url", "jdbc:hsqldb:hsql://localhost/test");
        dataSourceModel.addProperty("driver", "org.hsqldb.jdbcDriver");

        final ServerModelImpl serverModel = new ServerModelImpl();
        serverModel.getStoreModels().addAll(models1);
        serverModel.getListeners().add(getListenerModel(host,port1, 5));
        serverModel.setManagementServerModel(getManagementServerModel(2142, host, 2, 2000));
        serverModel.setServerName("main");
        serverModel.getProperties().setProperty("SessionStorePath", "/opt/t1/sessionstore/1");
        serverModel.setSessionServerModel(sessionServer);
        serverModel.getSessionClientModels().add(sessionClient);
        serverModel.getSaveClientModels().add(getSaveClientModel(host,5224, 5));

        serverModel.addDataSources(dataSourceModel);

        final CommandPluginModelImpl plugin = new CommandPluginModelImpl();
        plugin.setCheckDelay(10);
        plugin.setClazz("com.nomad.plugin.IdGenerator");
        plugin.setPoolSize(10);
        plugin.getProperties().put("DataSourceName", "a");
        plugin.setTimeout(10);

        final CommandPluginModelImpl proxyPlugin = new CommandPluginModelImpl();
        proxyPlugin.setClazz("com.nomad.plugin.IdGeneratorProxy");
        proxyPlugin.setCheckDelay(10);
        proxyPlugin.setPoolSize(10);
        proxyPlugin.setTimeout(60);

        serverModel.getCommandPlugins().add(plugin);

        dataInvoker = PmDataInvokerFactory.getDataInvoker("b", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:hsql://localhost/test", "sa", "", null, 1);

        launcher = new ServerLauncher(serverModel);

        launcher.start();

        final ServerModelImpl serverModel2 = new ServerModelImpl();
        serverModel2.getStoreModels().addAll(models2);
        final ListenerModelImpl listener2=getListenerModel(host,port2, 6);
        serverModel2.getListeners().add(listener2);
        serverModel2.setManagementServerModel(getManagementServerModel(2242, host, 2, 2000));
        serverModel2.setServerName("second");

        serverModel2.addDataSources(dataSourceModel);

        serverModel2.getCommandPlugins().add(plugin);
        serverModel2.getProperties().setProperty("SessionStorePath", "/opt/t1/sessionstore/2");
        serverModel2.getSessionClientModels().add(sessionClient);
        serverModel2.getSaveServerModels().add(getSaveServerModel(host,5224, 15));
        serverModel2.getSaveClientModels().add(getSaveClientModel(host,5224, 5));

        final ConnectModelImpl connectModel = getConnectModel(serverModel2, serverModel, 5, listener2);

        serverModel2.getClients().add(connectModel);


        launcher2 = new ServerLauncher(serverModel2);

        launcher2.start();

        final ServerModelImpl serverModel3 = new ServerModelImpl();
        serverModel3.getStoreModels().addAll(models2);
        final ListenerModelImpl listener3=getListenerModel(host,port3, 6);
        serverModel3.getListeners().add(listener3);
        serverModel3.setManagementServerModel(getManagementServerModel(2342, host, 2, 2000));
        serverModel3.setServerName("second");

        serverModel3.addDataSources(dataSourceModel);

        serverModel3.getCommandPlugins().add(plugin);
        serverModel3.getProperties().setProperty("SessionStorePath", "/opt/t1/sessionstore/3");
        serverModel3.getSessionClientModels().add(sessionClient);
        serverModel3.getSaveClientModels().add(getSaveClientModel(host,5224, 5));

        final ConnectModelImpl connectModel3 = getConnectModel(serverModel3, serverModel, 5, listener3);

        serverModel3.getClients().add(connectModel3);// client

        launcher3 = new ServerLauncher(serverModel3);

        launcher3.start();

        clientMain = new SimpleCacheClient(host, port1);
        client2 = new SimpleCacheClient(host, port2);
        client3 = new SimpleCacheClient(host, port3);

        Thread.sleep(1000);
        clientMain.close();
        client2.close();
        client3.close();

        dataInvoker.close();
        launcher.stop();
        launcher2.stop();
        launcher3.stop();

    }





}
