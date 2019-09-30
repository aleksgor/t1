package com.nomad.cache.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.nomad.cache.test.model.MainTestModel;
import com.nomad.cache.test.model.MainTestModelId;
import com.nomad.client.SimpleCacheClient;
import com.nomad.exception.ModelNotExistException;
import com.nomad.message.FullMessage;
import com.nomad.message.OperationStatus;
import com.nomad.model.BaseCommand;
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

public class ProxyTest extends CommonTest {

    private static ServerLauncher launcher;
    private static ServerLauncher launcher2;

    private static PmDataInvoker dataInvoker;
    private static SimpleCacheClient clientMain;
    private static SimpleCacheClient client2;

    private static int port1 = 2132;
    private static int port2 = 2232;

    /*
     * test session isolation
     */

    @org.junit.Test
    public void test4() throws Exception {
        FullMessage message = clientMain.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(3), null);
        message = clientMain.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(3), null);
        message = clientMain.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(3), null);
        message = clientMain.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(3), null);

        final MainTestModel t = new MainTestModel();
        t.setId(3);
        t.setName("test3");
        final String session1 = clientMain.sendCommand(BaseCommand.START_NEW_SESSION).getHeader().getSessionId();

        message = clientMain.sendCommandForModel(BaseCommand.PUT, t, session1);
        clientMain.sendCommand(BaseCommand.COMMIT, session1);

        final String session2 = clientMain.sendCommand(BaseCommand.START_NEW_SESSION).getHeader().getSessionId();
        message = clientMain.sendCommandForId(BaseCommand.GET, new MainTestModelId(3), session2);

        message = clientMain.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(3), session2);
        message = clientMain.sendCommand(BaseCommand.CLOSE_SESSION, session2);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        message = clientMain.sendCommandForId(BaseCommand.GET, new MainTestModelId(3), session2);
        assertEquals(OperationStatus.INVALID_SESSION, message.getResult().getOperationStatus());
        assertEquals(t.getId(), 3);

        message = clientMain.sendCommandForModel(BaseCommand.PUT, t, session1);
        clientMain.sendCommand(BaseCommand.COMMIT, session1);

    }

    @org.junit.Test
    public void test1() throws Exception {
        FullMessage message = clientMain.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(3), null);

        try {
            dataInvoker.getModel(new MainTestModelId(3));
            fail();
        } catch (final ModelNotExistException e) {
        }

        final MainTestModel t = new MainTestModel();
        t.setId(3);
        t.setName("test3");
        final String session1 = clientMain.sendCommand(BaseCommand.START_NEW_SESSION).getHeader().getSessionId();
        message = clientMain.sendCommandForModel(BaseCommand.PUT, t, session1);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        final String session2 = clientMain.sendCommand(BaseCommand.START_NEW_SESSION).getHeader().getSessionId();

        message = clientMain.sendCommandForId(BaseCommand.GET, new MainTestModelId(3), session2);
        assertTrue(message.getBody().getResponse().getResultList().isEmpty());
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        message = clientMain.sendCommandForId(BaseCommand.GET, new MainTestModelId(3), session1);

        assertNotNull(message.getBody().getResponse().getResultList());
        clientMain.sendCommand(BaseCommand.ROLLBACK, session1);

    }

    @org.junit.Test
    public void test2() throws Exception {

        FullMessage message = clientMain.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(3), null);
        clientMain.sendCommand(BaseCommand.COMMIT, message.getHeader().getSessionId());
        try {
            dataInvoker.getModel(new MainTestModelId(3));
            fail();
        } catch (final ModelNotExistException e) {

        }

        final MainTestModel t = new MainTestModel();
        t.setId(3);
        t.setName("test3");

        final String session1 = clientMain.sendCommand(BaseCommand.START_NEW_SESSION).getHeader().getSessionId();
        message = clientMain.sendCommandForModel(BaseCommand.PUT, t, session1);
        try {
            dataInvoker.getModel(new MainTestModelId(3));
            fail();
        } catch (final ModelNotExistException e) {
        }
        clientMain.sendCommand(BaseCommand.COMMIT, session1);

        final MainTestModel testModel = (MainTestModel) dataInvoker.getModel(new MainTestModelId(3));
        assertNotNull(testModel);
        assertNotNull(clientMain.sendCommandForId(BaseCommand.GET, new MainTestModelId(3), null).getBody().getResponse().getResultList());

        message = clientMain.sendCommandForId(BaseCommand.GET, new MainTestModelId(3), session1);
        assertNotNull(message.getBody().getResponse().getResultList());

    }

    /*


     */
    @org.junit.Test
    public void test3() throws Exception {

        FullMessage message = clientMain.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(3), null);

        try {
            dataInvoker.getModel(new MainTestModelId(3));
            fail();
        } catch (final ModelNotExistException e) {
        }

        final MainTestModel t = new MainTestModel();
        t.setId(3);
        t.setName("test3");
        message = clientMain.sendCommand(BaseCommand.START_NEW_SESSION);
        ;
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        final String session1 = message.getHeader().getSessionId();
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        message = clientMain.sendCommandForModel(BaseCommand.PUT, t, session1);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        message = clientMain.sendCommand(BaseCommand.COMMIT, session1);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        dataInvoker.getModel(new MainTestModelId(3));

        final String session2 = clientMain.sendCommand(BaseCommand.START_NEW_SESSION).getHeader().getSessionId();

        message = clientMain.sendCommandForId(BaseCommand.GET, new MainTestModelId(3), session2);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(session2, message.getHeader().getSessionId());

        message = clientMain.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(3), session2);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        message = clientMain.sendCommandForId(BaseCommand.GET, new MainTestModelId(3), null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertNotNull(message.getBody().getResponse().getResultList());
        message = clientMain.sendCommandForId(BaseCommand.COMMIT, new MainTestModelId(3), session2);

    }

    @org.junit.Test
    public void testRollback() throws Exception {

        FullMessage message = clientMain.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(3), null);

        final MainTestModel t = new MainTestModel();
        t.setId(3);
        t.setName("test3");
        final String session1 = clientMain.sendCommand(BaseCommand.START_NEW_SESSION).getHeader().getSessionId();

        message = clientMain.sendCommandForModel(BaseCommand.PUT, t, session1);
        clientMain.sendCommand(BaseCommand.COMMIT, session1);

        final String session2 = clientMain.sendCommand(BaseCommand.START_NEW_SESSION).getHeader().getSessionId();
        message = clientMain.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(3), session2);

        final String session3 = clientMain.sendCommand(BaseCommand.START_NEW_SESSION).getHeader().getSessionId();
        message = clientMain.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(3), session3);

        assertEquals(message.getResult().getOperationStatus(), OperationStatus.BLOCKED);
        message = clientMain.sendCommand(BaseCommand.ROLLBACK, session2);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        message = clientMain.sendCommand(BaseCommand.ROLLBACK, session2);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

    }

    @AfterClass
    public static void tearDown() throws Exception {
        clientMain.close();
        client2.close();
        dataInvoker.close();
        launcher.stop();
        launcher2.stop();
    }

    @BeforeClass
    public static void setUp() throws Exception {

        host = InetAddress.getLocalHost().getHostName();

        final SessionServerModelImp sessionServer = new SessionServerModelImp();
        sessionServer.setPort(5888);
        sessionServer.setSessionTimeLive(60000);
        sessionServer.setMinThreads(6);
        sessionServer.setMaxThreads(6);
        sessionServer.setKeepAliveTime(1000);

        final SessionClientModelImpl sessionClient = new SessionClientModelImpl();
        sessionClient.setHost(host);
        sessionClient.setPort(5888);
        sessionClient.setThreads(2);

        final List<StoreModelImpl> models1 = new ArrayList<>();
        models1.add(getStoreData("MainTestModel", "com.nomad.cache.test.model.MainTestModel"));
        final List<StoreModelImpl> models2 = new ArrayList<>();
        models2.add(getStoreData("Child", "com.nomad.cache.test.model.Child"));
        models2.add(getStoreData("MainTestModel", "com.nomad.cache.test.model.MainTestModel"));

        final DataSourceModelImpl dataSource = new DataSourceModelImpl();
        dataSource.setName("a");
        dataSource.setTimeOut(10000);
        dataSource.setThreads(10);
        dataSource.setClazz("com.nomad.cache.test.userdataaccess.BaseDataInvoker");
        dataSource.addProperty("user", "sa");
        dataSource.addProperty("password", "");
        dataSource.addProperty("url", "jdbc:hsqldb:hsql://localhost/test");
        dataSource.addProperty("driver", "org.hsqldb.jdbcDriver");

        final ServerModelImpl serverModel = new ServerModelImpl();
        serverModel.getStoreModels().addAll(models1);
        serverModel.getListeners().add(getListenerModel(host, 2122, 3));
        serverModel.getListeners().add(getListenerModel(host, port1, 3));
        serverModel.setManagementServerModel(getManagementServerModel(2142, host, 2, 2000));

        serverModel.setServerName("main");
        serverModel.getProperties().setProperty("SessionStorePath", "/opt/t1/sessionstore/1");
        serverModel.setSessionServerModel(sessionServer);
        serverModel.getSessionClientModels().add(sessionClient);
        serverModel.getSaveServerModels().add(getSaveServerModel(host, 2124, 5));
        serverModel.getSaveClientModels().add(getSaveClientModel(host, 2124, 5));

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
        final ListenerModelImpl listener = getListenerModel(host, 2222, 3);
        serverModel2.getListeners().add(listener);
        serverModel2.getListeners().add(getListenerModel(host, port2, 3));
        serverModel2.setManagementServerModel(getManagementServerModel(2242, host, 2, 2000));
        serverModel2.setServerName("second");

        serverModel2.addDataSources(dataSource);

        serverModel2.getCommandPlugins().add(plugin);
        serverModel2.getProperties().setProperty("SessionStorePath", "/opt/t1/sessionstore/2");
        serverModel2.getSessionClientModels().add(sessionClient);
        serverModel2.getSaveClientModels().add(getSaveClientModel(host, 2124, 5));

        final ConnectModelImpl connectModel = getConnectModel(serverModel2, serverModel, 3, listener);

        serverModel2.getClients().add(connectModel);

        launcher2 = new ServerLauncher(serverModel2);

        launcher2.start();

        clientMain = new SimpleCacheClient(host, port1);
        client2 = new SimpleCacheClient(host, port2);

    }

}
