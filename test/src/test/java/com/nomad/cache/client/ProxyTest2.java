package com.nomad.cache.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.nomad.cache.test.model.MainTestModel;
import com.nomad.cache.test.model.MainTestModelId;
import com.nomad.client.IdentifiersResult;
import com.nomad.client.ModelsResult;
import com.nomad.client.SessionResult;
import com.nomad.client.SimpleCacheClient;
import com.nomad.client.VoidResult;
import com.nomad.exception.ModelNotExistException;
import com.nomad.message.Body;
import com.nomad.message.FullMessage;
import com.nomad.message.OperationStatus;
import com.nomad.model.BaseCommand;
import com.nomad.model.CommandPluginModelImpl;
import com.nomad.model.ConnectModelImpl;
import com.nomad.model.DataSourceModelImpl;
import com.nomad.model.ListenerModelImpl;
import com.nomad.model.Model;
import com.nomad.model.ServerModelImpl;
import com.nomad.model.ServiceCommand;
import com.nomad.model.StoreModel.ServerType;
import com.nomad.model.StoreModel.StoreType;
import com.nomad.model.StoreModelImpl;
import com.nomad.model.session.SessionClientModelImpl;
import com.nomad.model.session.SessionServerModelImp;
import com.nomad.pm.PmDataInvokerFactory;
import com.nomad.server.PmDataInvoker;
import com.nomad.server.ServerLauncher;

public class ProxyTest2 extends CommonTest {

    private static ServerLauncher launcher;
    private static ServerLauncher launcher2;
    private static int port = 2222;
    private static SimpleCacheClient client;

    private static PmDataInvoker dataInvoker;

    /*
     * test session isolation
     */
    @org.junit.Test
    public void test1() throws Exception {
        FullMessage message = client.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(3));

        try {
            dataInvoker.getModel(new MainTestModelId(3));
            fail();
        } catch (final ModelNotExistException e) {
        }

        final MainTestModel t = new MainTestModel();
        t.setId(3);
        t.setName("test3");
        final String session1 = client.sendCommand(BaseCommand.START_NEW_SESSION).getHeader().getSessionId();
        message = client.sendCommandForModel(BaseCommand.PUT, t, session1);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        final String session2 = client.sendCommand(BaseCommand.START_NEW_SESSION).getHeader().getSessionId();

        message = client.sendCommandForId(BaseCommand.GET, new MainTestModelId(3), session2);
        assertTrue(message.getBody().getResponse().getResultList().isEmpty());
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        message = client.sendCommandForId(BaseCommand.GET, new MainTestModelId(3), session1);

        assertNotNull(message.getBody().getResponse().getResultList());
        client.sendCommand(BaseCommand.ROLLBACK, session1);

    }

    @org.junit.Test
    public void test2() throws Exception {
        FullMessage message = client.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(3), null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        try {
            dataInvoker.getModel(new MainTestModelId(3));
            fail();
        } catch (final ModelNotExistException e) {

        }

        final MainTestModel t = new MainTestModel();
        t.setId(3);
        t.setName("test3");

        final String session1 = client.sendCommand(BaseCommand.START_NEW_SESSION).getHeader().getSessionId();

        message = client.sendCommandForModel(BaseCommand.PUT, t, session1);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        try {
            dataInvoker.getModel(new MainTestModelId(3));
            fail();
        } catch (final ModelNotExistException e) {
        }
        message = client.sendCommand(BaseCommand.COMMIT, session1);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        final MainTestModel testModel = (MainTestModel) dataInvoker.getModel(new MainTestModelId(3));
        assertNotNull(testModel);
        assertNotNull(client.sendCommandForId(BaseCommand.GET, new MainTestModelId(3), null).getBody().getResponse().getResultList());

        message = client.sendCommandForId(BaseCommand.GET, new MainTestModelId(3), session1);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        assertNotNull(message.getBody().getResponse().getResultList());
        client.sendCommand(BaseCommand.CLOSE_SESSION, session1);

    }

    @org.junit.Test
    public void test3() throws Exception {

        FullMessage message = client.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(3), null);

        try {
            dataInvoker.getModel(new MainTestModelId(3));
            fail();
        } catch (final ModelNotExistException e) {

        }

        final MainTestModel t = new MainTestModel();
        t.setId(3);
        t.setName("test3");
        message = client.sendCommand(BaseCommand.START_NEW_SESSION);
        final String session1 = message.getHeader().getSessionId();
        message = client.sendCommandForModel(BaseCommand.PUT, t, session1);
        message = client.sendCommand(BaseCommand.COMMIT, session1);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        dataInvoker.getModel(new MainTestModelId(3));

        final String session2 = client.sendCommand(BaseCommand.START_NEW_SESSION).getHeader().getSessionId();

        message = client.sendCommandForId(BaseCommand.GET, new MainTestModelId(3), session2);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(session2, message.getHeader().getSessionId());

        message = client.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(3), session2);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        message = client.sendCommandForId(BaseCommand.GET, new MainTestModelId(3), null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertNotNull(message.getBody().getResponse().getResultList());
        message = client.sendCommandForId(BaseCommand.COMMIT, new MainTestModelId(3), session2);

    }

    @org.junit.Test
    public void test4() throws Exception {
        client.removeModel(new MainTestModelId(3), null);

        final MainTestModel t = new MainTestModel();
        t.setId(3);
        t.setName("test3");
        final String session1 = client.sendCommand(BaseCommand.START_NEW_SESSION).getHeader().getSessionId();

        client.putModel(t, session1);
        client.commit(session1);

        final String session2 = client.startSession().getSessionId();
        client.getModel(new MainTestModelId(3), session2);

        client.removeModel(new MainTestModelId(3), session2);

        final SessionResult sessionResult = client.closeSession(session2);
        assertEquals(OperationStatus.OK, sessionResult.getOperationStatus());

        final ModelsResult modelResult = client.getModel(new MainTestModelId(3), session2);
        assertEquals(OperationStatus.INVALID_SESSION, modelResult.getOperationStatus());


    }

    @org.junit.Test
    public void testRollback() throws Exception {

        client.removeModel( new MainTestModelId(3), null);

        final MainTestModel t = new MainTestModel();
        t.setId(3);
        t.setName("test3");
        final String session1 = client.sendCommand(BaseCommand.START_NEW_SESSION).getHeader().getSessionId();

        client.putModel(t, session1);
        client.sendCommand(BaseCommand.COMMIT, session1);

        final String session2 = client.startSession().getSessionId();
        client.removeModel(new MainTestModelId(3), session2);

        final String session3 = client.startSession().getSessionId();
        final IdentifiersResult iResult = client.removeModel(new MainTestModelId(3), session3);
        assertEquals(OperationStatus.BLOCKED, iResult.getOperationStatus());

        VoidResult vResult = client.rollback(session2);
        assertEquals(OperationStatus.OK, vResult.getOperationStatus());

        vResult = client.rollback(session2);
        assertEquals(OperationStatus.OK, vResult.getOperationStatus());


    }

    private static Model getNewTest(final int id, final String name) {
        final MainTestModel result = new MainTestModel();
        result.setId(id);
        result.setName(name);
        return result;
    }

    @org.junit.Test
    public void test6() throws Exception {
        SimpleCacheClient client1 = null;
        SimpleCacheClient client2 = null;
        try {
            client1 = new SimpleCacheClient(host, port);
            client2 = new SimpleCacheClient(host, 2422);
            String session1 = client.sendCommand(BaseCommand.START_NEW_SESSION).getHeader().getSessionId();
            final String session2 = client.startSession().getSessionId();

            client.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(5), session2);
            client.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(6), session2);
            client.sendCommand(BaseCommand.COMMIT, session2);

            session1 = client.startSession().getSessionId();

            client.putModel( getNewTest(5, "test5"), session1);
            client.putModel( getNewTest(6, "test6"), session1);
            client.putModel(getNewTest(7, "test7"), session1);
            client.putModel( getNewTest(8, "test8"), session1);

            client.sendCommand(BaseCommand.COMMIT, session1);

            assertNotNull(dataInvoker.getModel(new MainTestModelId(5)));
            assertNotNull(dataInvoker.getModel(new MainTestModelId(6)));

            final int[] cache = new int[2];
            final int[] test = new int[4];

            FullMessage message = client1.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE, new MainTestModelId(5));
            assertEquals(OperationStatus.UNSUPPORTED_MODEL_NAME, message.getResult().getOperationStatus());
            cache[0] += getCountIdentifiers(message.getBody());
            test[0] += getCountIdentifiers(message.getBody());

            message = client2.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE, new MainTestModelId(5));
            assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
            cache[1] += getCountIdentifiers(message.getBody());
            test[0] += getCountIdentifiers(message.getBody());

            message = client1.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE, new MainTestModelId(6));
            assertEquals(OperationStatus.UNSUPPORTED_MODEL_NAME, message.getResult().getOperationStatus());
            cache[0] += getCountIdentifiers(message.getBody());
            test[1] += getCountIdentifiers(message.getBody());

            message = client2.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE, new MainTestModelId(6));
            assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
            cache[1] += getCountIdentifiers(message.getBody());
            test[1] += getCountIdentifiers(message.getBody());

            message = client1.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE, new MainTestModelId(7));
            assertEquals(OperationStatus.UNSUPPORTED_MODEL_NAME, message.getResult().getOperationStatus());
            cache[0] += getCountIdentifiers(message.getBody());
            test[2] += getCountIdentifiers(message.getBody());

            message = client2.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE, new MainTestModelId(7));
            assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
            cache[1] += getCountIdentifiers(message.getBody());
            test[2] += getCountIdentifiers(message.getBody());

            message = client1.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE, new MainTestModelId(8));
            assertEquals(OperationStatus.UNSUPPORTED_MODEL_NAME, message.getResult().getOperationStatus());
            cache[0] += getCountIdentifiers(message.getBody());
            test[3] += getCountIdentifiers(message.getBody());
            message = client2.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE, new MainTestModelId(8));
            assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
            cache[1] += getCountIdentifiers(message.getBody());
            test[3] += getCountIdentifiers(message.getBody());

            assertEquals(4, cache[0] + cache[1]);
            assertEquals(1, test[0]);
            assertEquals(1, test[1]);
            assertEquals(1, test[2]);
            assertEquals(1, test[3]);

        } finally {
            if (client1 != null) {
                client1.close();
            }
            if (client2 != null) {
                client2.close();
            }
        }
    }
    private int getCountIdentifiers(Body body){
        int result =0;
        if(body.getResponse()==null){
            return result;
        }
        if(body.getResponse().getIdentifiers()==null){
            return result;
        }
        return body.getResponse().getIdentifiers().size();

    }
    @BeforeClass
    public static void setUp() throws Exception {
        commonSetup();
        registerSerialized();

        version = 0x1;

        final List<StoreModelImpl> models1 = new ArrayList<>();
        models1.add(getStoreData("MainTestModel", "com.nomad.cache.test.model.MainTestModel", ServerType.CACHE));
        final List<StoreModelImpl> models2 = new ArrayList<>();
        models2.add(getStoreData("Child", "com.nomad.cache.test.model.Child", ServerType.CACHE));
        models2.add(getStoreData("MainTestModel", "com.nomad.cache.test.model.MainTestModel", ServerType.CACHE_MANAGER));

        final SessionServerModelImp sessionServer = getSessionServerModel(host, 5445, 4);

        final SessionClientModelImpl sessionClient = getSessionClientModel(host, 5445, 2);

        final ServerModelImpl serverModel = new ServerModelImpl();
        serverModel.getStoreModels().addAll(models2);
        serverModel.getListeners().add(getListenerModel(host, port, 10));
        serverModel.setManagementServerModel(getManagementServerModel(2242, host, 2, 2000));

        serverModel.setServerName("main");
        serverModel.getProperties().setProperty("SessionStorePath", "/opt/t1/sessionstore/1");
        serverModel.getSaveServerModels().add(getSaveServerModel(host, 5545, 6));
        serverModel.getSaveClientModels().add(getSaveClientModel(host, 5545, 3));
        serverModel.setSessionServerModel(sessionServer);
        serverModel.getSessionClientModels().add(sessionClient);
        serverModel.setCalculateStatistic(false);

        final DataSourceModelImpl dataSource = new DataSourceModelImpl();
        dataSource.setName("a");
        dataSource.setThreads(10);
        dataSource.setTimeOut(10000);
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
        proxyPlugin.setCheckDelay(10);
        proxyPlugin.setPoolSize(10);
        proxyPlugin.setTimeout(60);

        serverModel.getCommandPlugins().add(plugin);


        final ServerModelImpl serverModel2 = new ServerModelImpl();
        serverModel2.getStoreModels().addAll(models1);
        final ListenerModelImpl listener = getListenerModel(host, 2422, 10);
        serverModel2.getListeners().add(listener);
        serverModel2.setManagementServerModel(getManagementServerModel(2442, host, 2, 2000));
        serverModel2.setServerName("second");
        serverModel2.getProperties().setProperty("SessionStorePath", "/opt/t1/sessionstore/2");

        serverModel2.addDataSources(dataSource);
        serverModel2.getSessionClientModels().add(sessionClient);

        serverModel2.getCommandPlugins().add(plugin);
        serverModel2.getSaveClientModels().add(getSaveClientModel(host, 5545, 3));
        serverModel2.setCalculateStatistic(false);

        final ConnectModelImpl connect = getConnectModel(serverModel2, serverModel, 5, listener);

        launcher = new ServerLauncher(serverModel);
        launcher.start();

        serverModel2.getClients().add(connect);

        dataInvoker = PmDataInvokerFactory.getDataInvoker("b", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:hsql://localhost/test", "sa", "", null, 1);

        launcher2 = new ServerLauncher(serverModel2);
        launcher2.start();

        client = new SimpleCacheClient(host, port);

    }

    @AfterClass
    public static void tearDown() throws Exception {
        dataInvoker.close();
        launcher.stop();
        launcher2.stop();
        client.close();

    }

    protected static StoreModelImpl getStoreData(final String name, final String clazz) {
        final StoreModelImpl storeModel = new StoreModelImpl();
        storeModel.setModel(name);
        storeModel.setClazz(clazz);
        storeModel.setReadThrough(true);
        storeModel.setWriteThrough(true);
        storeModel.setDataSource("a");
        storeModel.setServerType(ServerType.ALL);
        storeModel.setStoreType(StoreType.OBJECT);
        storeModel.setCopyCount(1);
        return storeModel;
    }

}
