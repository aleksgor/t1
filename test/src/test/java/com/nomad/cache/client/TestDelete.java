package com.nomad.cache.client;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.nomad.cache.test.model.MainTestModel;
import com.nomad.cache.test.model.MainTestModelId;
import com.nomad.client.IdentifiersResult;
import com.nomad.client.SimpleCacheClient;
import com.nomad.message.FullMessage;
import com.nomad.message.OperationStatus;
import com.nomad.model.BaseCommand;
import com.nomad.model.CommandPluginModelImpl;
import com.nomad.model.ConnectModelImpl;
import com.nomad.model.DataSourceModelImpl;
import com.nomad.model.Identifier;
import com.nomad.model.ListenerModelImpl;
import com.nomad.model.ServerModelImpl;
import com.nomad.model.ServiceCommand;
import com.nomad.model.StoreModelImpl;
import com.nomad.pm.PmDataInvokerFactory;
import com.nomad.server.PmDataInvoker;
import com.nomad.server.ServerLauncher;

public class TestDelete extends CommonTest {
    private static ServerLauncher launcher;
    private static ServerLauncher launcher2;
    private static ServerLauncher launcher3;

    private static String host = "localhost";
    private static int port1 = 2132;
    private static int port2 = 2232;
    private static int port3 = 2332;
    private static SimpleCacheClient clientMain;
    private static SimpleCacheClient client2;
    private static SimpleCacheClient client3;
    private static PmDataInvoker dataInvoker;

    @org.junit.Test
    public void test1() throws Exception {
        FullMessage message;

        message = clientMain.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(1), null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        message = clientMain.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(2), null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(0, testInCache(new MainTestModelId(1)));
        assertEquals(0, testInCache(new MainTestModelId(2)));

        final MainTestModel test1 = getNewTestModel(1, "a,dbvlashdvcasbdvc.as");
        final MainTestModel test2 = getNewTestModel(2, "a,dbvlashdvcasbdvc.asafcavcadc");
        message = clientMain.sendCommandForModel(BaseCommand.PUT, test1, null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        message = clientMain.sendCommandForModel(BaseCommand.PUT, test2, null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        assertEquals(2, testInCache(new MainTestModelId(1)));
        assertEquals(2, testInCache(new MainTestModelId(2)));

        message = clientMain.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(1), null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        message = clientMain.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(2), null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(0, testInCache(new MainTestModelId(1)));
        assertEquals(0, testInCache(new MainTestModelId(2)));

    }

    @org.junit.Test
    public void test2() throws Exception {
        FullMessage message;

        for (int i = 0; i < 10; i++) {
            final MainTestModel testModel = getNewTestModel(i, "a,dbvlashdvcasbdvc.as");

            message = clientMain.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(i), null);
            assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
            assertEquals(0, testInCache(new MainTestModelId(i)));


            final String sessionId = clientMain.sendCommand(BaseCommand.START_NEW_SESSION).getHeader().getSessionId();
            clientMain.sendCommandForModel(ServiceCommand.PUT_INTO_CACHE, testModel, sessionId);
            assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

            client2.sendCommandForModel(ServiceCommand.PUT_INTO_CACHE, testModel, sessionId);
            assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

            client3.sendCommandForModel(ServiceCommand.PUT_INTO_CACHE, testModel, sessionId);
            assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

            assertEquals(3, testInCache(new MainTestModelId(i)));
            clientMain.sendCommand(BaseCommand.COMMIT, sessionId);

            assertEquals(3, testInCache(new MainTestModelId(i)));

            message = clientMain.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(i), null);
            assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

            assertEquals(0, testInCache(new MainTestModelId(i)));
        }

    }

    @org.junit.Test
    public void test3() throws Exception {
        FullMessage message;

        for (int i = 0; i < 10; i++) {
            final MainTestModel testModel = getNewTestModel(i, "a,dbvlashdvcasbdvc.as");

            final IdentifiersResult iResult = clientMain.removeModel(new MainTestModelId(i), null);
            assertEquals(OperationStatus.OK, iResult.getOperationStatus());
            assertEquals(0, testInCache(new MainTestModelId(i)));


            final String sessionId = clientMain.startSession().getSessionId();
            message = clientMain.sendCommandForModel(ServiceCommand.PUT_INTO_CACHE, testModel, sessionId);
            assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

            message = client2.sendCommandForModel(ServiceCommand.PUT_INTO_CACHE, testModel, sessionId);
            assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

            message = client3.sendCommandForModel(ServiceCommand.PUT_INTO_CACHE, testModel, sessionId);
            assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

            assertEquals(3, testInCache(new MainTestModelId(i)));
            message=clientMain.sendCommand(BaseCommand.ROLLBACK, sessionId);
            assertEquals(0, testInCache(new MainTestModelId(i)));

            message = clientMain.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(i), null);
            assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

            assertEquals(0, testInCache(new MainTestModelId(i)));
        }
    }
    @org.junit.Test
    public void test4() throws Exception {
        FullMessage message;

        for (int i = 0; i < 10; i++) {
            final MainTestModel testModel = getNewTestModel(i, "a,dbvlashdvcasbdvc.as");

            message = clientMain.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(i), null);
            assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
            assertEquals(0, testInCache(new MainTestModelId(i)));


            final String sessionId = clientMain.sendCommand(BaseCommand.START_NEW_SESSION).getHeader().getSessionId();
            clientMain.sendCommandForModel(ServiceCommand.PUT_INTO_CACHE, testModel, sessionId);
            assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

            client2.sendCommandForModel(ServiceCommand.PUT_INTO_CACHE, testModel, sessionId);
            assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

            client3.sendCommandForModel(ServiceCommand.PUT_INTO_CACHE, testModel, sessionId);
            assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

            assertEquals(3, testInCache(new MainTestModelId(i)));
            clientMain.sendCommand(BaseCommand.ROLLBACK, sessionId);
            assertEquals(0, testInCache(new MainTestModelId(i)));

            message = clientMain.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(i), null);
            assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

            assertEquals(0, testInCache(new MainTestModelId(i)));
        }
    }

    private int testInCache(final Identifier id) throws Exception {
        int counter = 0;
        FullMessage message;
        message = clientMain.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE, id, null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        counter += message.getBody().getResponse().getIdentifiers().size();

        message = client2.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE, id, null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        counter += message.getBody().getResponse().getIdentifiers().size();

        message = client3.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE, id, null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        counter += message.getBody().getResponse().getIdentifiers().size();

        return counter;
    }

    @BeforeClass
    public static void setUp() throws Exception {
        commonSetup();
        registerSerialized();


        final List<StoreModelImpl> models1 = new ArrayList<>();
        models1.add(getStoreData("MainTestModel", "com.nomad.cache.test.model.MainTestModel"));
        final List<StoreModelImpl> models2 = new ArrayList<>();
        models2.add(getStoreData("Child", "com.nomad.cache.test.model.Child"));
        models2.add(getStoreData("MainTestModel", "com.nomad.cache.test.model.MainTestModel"));

        final DataSourceModelImpl dataSource = new DataSourceModelImpl();
        dataSource.setName("a");
        dataSource.setTimeOut(1000);
        dataSource.setThreads(10);
        dataSource.setClazz("com.nomad.cache.test.userdataaccess.BaseDataInvoker");
        dataSource.addProperty("user", "sa");
        dataSource.addProperty("password", "");
        dataSource.addProperty("url", "jdbc:hsqldb:hsql://localhost/test");
        dataSource.addProperty("driver", "org.hsqldb.jdbcDriver");

        final ServerModelImpl serverModel = new ServerModelImpl();
        serverModel.getStoreModels().addAll(models1);

        serverModel.getListeners().add(getListenerModel(host,port1, 9));
        serverModel.setManagementServerModel(getManagementServerModel(2142,host,2,10000));

        serverModel.setServerName("main");
        serverModel.setSessionServerModel( getSessionServerModel(host,5888, 15));
        serverModel.getSessionClientModels().add(getSessionClientModel(host,5888, 5));
        serverModel.getSaveClientModels().add(getSaveClientModel(host,5224, 5));

        serverModel.addDataSources(dataSource);

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
        final ListenerModelImpl listener2=getListenerModel(host,port2, 9);
        serverModel2.getListeners().add(listener2);
        serverModel2.setManagementServerModel(getManagementServerModel(2242,host,2,10000));;


        serverModel2.setServerName("second");
        serverModel2.addDataSources(dataSource);
        serverModel2.getCommandPlugins().add(plugin);
        serverModel2.getSaveServerModels().add(getSaveServerModel(host,5224, 15));
        serverModel2.getSaveClientModels().add(getSaveClientModel(host,5224, 5));

        final ConnectModelImpl connectModel = getConnectModel(serverModel2, serverModel, 4, listener2);

        serverModel2.getClients().add(connectModel);
        serverModel2.setTrustSessions(true);

        launcher2 = new ServerLauncher(serverModel2);

        launcher2.start();

        final ServerModelImpl serverModel3 = new ServerModelImpl();
        serverModel3.getStoreModels().addAll(models2);
        final ListenerModelImpl listener3=getListenerModel(host,port3, 9);
        serverModel3.getListeners().add(listener3);
        serverModel3.setManagementServerModel(getManagementServerModel(2342, host, 10, 10000));
        serverModel3.setServerName("second");

        serverModel3.addDataSources(dataSource);

        serverModel3.getCommandPlugins().add(plugin);
        serverModel3.getSaveClientModels().add(getSaveClientModel(host,5224, 5));

        final ConnectModelImpl connectModel3 = getConnectModel(serverModel3, serverModel, 4, listener3);

        serverModel3.getClients().add(connectModel3);
        serverModel3.setTrustSessions(true);

        launcher3 = new ServerLauncher(serverModel3);

        launcher3.start();

        clientMain = new SimpleCacheClient(host, port1);
        client2 = new SimpleCacheClient(host, port2);
        client3 = new SimpleCacheClient(host, port3);

    }

    @AfterClass
    public static void tearDown() throws Exception {
        dataInvoker.close();
        launcher.stop();
        launcher2.stop();
        launcher3.stop();

    }
}
