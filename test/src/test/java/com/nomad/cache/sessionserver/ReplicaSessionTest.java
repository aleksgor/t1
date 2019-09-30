package com.nomad.cache.sessionserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.nomad.cache.client.CommonTest;
import com.nomad.cache.test.model.MainTestModel;
import com.nomad.cache.test.model.MainTestModelId;
import com.nomad.client.ModelsResult;
import com.nomad.client.SimpleCacheClient;
import com.nomad.message.FullMessage;
import com.nomad.message.OperationStatus;
import com.nomad.model.BaseCommand;
import com.nomad.model.DataSourceModelImpl;
import com.nomad.model.ListenerModelImpl;
import com.nomad.model.ServerModelImpl;
import com.nomad.model.StoreModelImpl;
import com.nomad.model.session.SessionClientModel;
import com.nomad.model.session.SessionServerModel;
import com.nomad.pm.PmDataInvokerFactory;
import com.nomad.server.PmDataInvoker;
import com.nomad.server.ThreadServerLauncher;

public class ReplicaSessionTest extends CommonTest {

    private static ThreadServerLauncher launcher;
    private static ThreadServerLauncher launcher2;
    private static ThreadServerLauncher launcher3;

    private static int port = 2222;
    private static SimpleCacheClient client;
    private static PmDataInvoker dataInvoker;

    /*
     * test session isolation
     */
    @org.junit.Test
    public void testOne() throws Exception {
        try {
            launcher2.start();
            launcher3.start();
            launcher.start();
            client = new SimpleCacheClient(host, port);

            smallTransactTest();
        } finally {
            stopAll();
        }

    }

    @org.junit.Test
    public void testTwo() throws Exception {

        try {
            launcher.start();
            launcher2.start();
            launcher3.start();
            client = new SimpleCacheClient(host, port);

            smallTransactTest();
        } finally {
            stopAll();
        }

    }

    @org.junit.Test
    public void testWithoutOneSessionServer() throws Exception {

        try {
            launcher.start();
            launcher2.start();
            client = new SimpleCacheClient(host, port);

            smallTransactTest();
        } finally {
            stopAll();
        }

    }

    @org.junit.Test
    public void testSyncSessions() throws Exception {
        try {
            launcher2.start();
            launcher.start();
            client = new SimpleCacheClient(host, port);
            client.removeModel(new MainTestModelId(3), null);

            final MainTestModel t = new MainTestModel();
            t.setId(3);
            t.setName("test3");
            final String session1 = client.startSession().getSessionId();
            final String session2 = client.startSession().getSessionId();

            launcher3.start();

            Thread.sleep(3000);
            ModelsResult modelsResult = client.getModel(new MainTestModelId(3), session1);
            assertEquals(OperationStatus.OK, modelsResult.getOperationStatus());

            modelsResult = client.getModel(new MainTestModelId(3), session2);
            assertEquals(OperationStatus.OK, modelsResult.getOperationStatus());

            launcher2.stop();

            Thread.sleep(500);
            modelsResult = client.putModel(t, session1);

            assertEquals(OperationStatus.OK, modelsResult.getOperationStatus());

            modelsResult = client.getModel(new MainTestModelId(3), session2);
            assertEquals(OperationStatus.OK, modelsResult.getOperationStatus());

            modelsResult = client.getModel(new MainTestModelId(3), session1);
            assertEquals(OperationStatus.OK, modelsResult.getOperationStatus());

        } finally {
            stopAll();
        }

    }

    private void stopAll() {
        if (launcher != null) {
            launcher.stop();
        }
        if (launcher2 != null) {
            launcher2.stop();
        }
        if (launcher3 != null) {
            launcher3.stop();
        }
        client.close();
    }

    private void smallTransactTest() throws Exception {
        FullMessage message = client.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(3), null);

        final MainTestModel t = new MainTestModel();
        t.setId(3);
        t.setName("test3");
        message = client.sendCommand(BaseCommand.START_NEW_SESSION);
        final String session1 = message.getHeader().getSessionId();

        message = client.sendCommand(BaseCommand.START_NEW_SESSION);
        final String session2 = message.getHeader().getSessionId();

        message = client.sendCommandForModel(BaseCommand.PUT, t, session1);

        message = client.sendCommandForId(BaseCommand.GET, new MainTestModelId(3), session2);
        assertTrue(message.getBody().getResponse().getResultList().isEmpty());

        message = client.sendCommandForId(BaseCommand.GET, new MainTestModelId(3), session1);
        assertNotNull(message.getBody().getResponse().getResultList());
        client.sendCommand(BaseCommand.ROLLBACK, session1);
    }

    @BeforeClass
    public static void setUp() throws Exception {
        commonSetup();
        registerSerialized();
        // launcher.start();
        final SessionClientModel sessionClient1 = getSessionClientModel(host, 5588, 2);
        final SessionClientModel sessionClient2 = getSessionClientModel(host, 5590, 2);
        ListenerModelImpl listeners = new ListenerModelImpl();
        // ls.setClazz("com.nomad.server.HttpListener");
        listeners = getListenerModel(host, port, 10);
        final List<StoreModelImpl> models = new ArrayList<>();

        models.add(getStoreData("MainTestModel", "com.nomad.cache.test.model.MainTestModel"));
        models.add(getStoreData("Child", "com.nomad.cache.test.model.Child"));

        final SessionServerModel sessionServerModel1 = getSessionServerModel(host, 5590, 15);
        final SessionServerModel sessionServerModel2 = getSessionServerModel(host, 5588, 15);

        final SessionClientModel sessionClientModel1 = getSessionClientModel(host, 5590, 5);
        final SessionClientModel sessionClientModel2 = getSessionClientModel(host, 5588, 5);


        final ServerModelImpl serverModel = new ServerModelImpl();
        serverModel.setManagementServerModel(getManagementServerModel(2224, host, 2, 2000));
        serverModel.getStoreModels().addAll(models);
        serverModel.getListeners().add(listeners);

        final DataSourceModelImpl dataSourceModel = new DataSourceModelImpl();
        dataSourceModel.setName("a");
        dataSourceModel.setTimeOut(1000);
        dataSourceModel.setThreads(12);
        dataSourceModel.setClazz("com.nomad.cache.test.userdataaccess.BaseDataInvoker");
        dataSourceModel.addProperty("user", "sa");
        dataSourceModel.addProperty("password", "");
        dataSourceModel.addProperty("url", "jdbc:hsqldb:hsql://localhost/test");
        dataSourceModel.addProperty("driver", "org.hsqldb.jdbcDriver");

        serverModel.addDataSources(dataSourceModel);
        serverModel.setLocalSessions(false);
        serverModel.setServerName("main");
        serverModel.getSessionClientModels().add(sessionClient2);
        serverModel.getSessionClientModels().add(sessionClient1);

        dataInvoker = PmDataInvokerFactory.getDataInvoker("b", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:hsql://localhost/test", "sa", "", null, 1);

        sessionServerModel2.getMirrors().add(sessionClientModel1);
        sessionServerModel1.getMirrors().add(sessionClientModel2);

        final ServerModelImpl serverModel2 = new ServerModelImpl();
        serverModel2.setServerName("second");
        serverModel2.setManagementServerModel(getManagementServerModel(2234, host, 2, 2000));

        serverModel2.setSessionServerModel(sessionServerModel1);
        serverModel2.getSessionClientModels().add(sessionClient1);

        // -------

        final ServerModelImpl serverModel3 = new ServerModelImpl();
        serverModel3.setServerName("third");
        serverModel3.setManagementServerModel(getManagementServerModel(2244, host, 2, 2000));

        serverModel3.setSessionServerModel(sessionServerModel2);
        serverModel3.getSessionClientModels().add(sessionClient2);

        launcher = new ThreadServerLauncher(serverModel);

        launcher2 = new ThreadServerLauncher(serverModel2);

        launcher3 = new ThreadServerLauncher(serverModel3);
        // client = new SimpleCacheClient(host, port);

    }

    @AfterClass
    public static void tearDown() throws Exception {
        launcher.stop();
        dataInvoker.getConnection().close();
        client.close();

    }
}
