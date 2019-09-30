package com.nomad.cache.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.nomad.cache.test.model.MainTestModel;
import com.nomad.cache.test.model.MainTestModelId;
import com.nomad.client.IdentifiersResult;
import com.nomad.client.ModelsResult;
import com.nomad.client.SessionResult;
import com.nomad.client.SimpleCacheClient;
import com.nomad.client.VoidResult;
import com.nomad.exception.ModelNotExistException;
import com.nomad.exception.SystemException;
import com.nomad.message.FullMessage;
import com.nomad.message.OperationStatus;
import com.nomad.model.BaseCommand;
import com.nomad.model.CommandPluginModelImpl;
import com.nomad.model.ConnectModelImpl;
import com.nomad.model.DataSourceModelImpl;
import com.nomad.model.Identifier;
import com.nomad.model.ListenerModelImpl;
import com.nomad.model.Model;
import com.nomad.model.ServerModelImpl;
import com.nomad.model.ServiceCommand;
import com.nomad.model.StoreModelImpl;
import com.nomad.model.StoreModel.ServerType;
import com.nomad.model.saveserver.SaveClientModel;
import com.nomad.model.saveserver.SaveServerModel;
import com.nomad.model.session.SessionClientModelImpl;
import com.nomad.model.session.SessionServerModelImp;
import com.nomad.pm.PmDataInvokerFactory;
import com.nomad.server.PmDataInvoker;
import com.nomad.server.ServerLauncher;

public class MultiServerXaTest extends CommonTest {

    private static ServerLauncher launcher;
    private static ServerLauncher launcher2;

    private static SimpleCacheClient client1;
    private static SimpleCacheClient client2;

    private static int port = 2222;

    private static SimpleCacheClient client;
    private static PmDataInvoker dataInvoker;

    /*
     * test session isolation
     */
    @org.junit.Test
    public void testAddCommit() throws Exception {

        client.removeModel(new MainTestModelId(3), null);
        client.removeModel(new MainTestModelId(4), null);
        client.removeModel(new MainTestModelId(5), null);

        final MainTestModel test3 = getNewTestModel(3, "Test3");
        final MainTestModel test4 = getNewTestModel(4, "Test4");
        final MainTestModel test5 = getNewTestModel(5, "Test5");

        final SessionResult sessionResult = client.startSession();
        final String session = sessionResult.getSessionId();

        ModelsResult modelResult = client.putModel(test3, session);
        assertEquals(OperationStatus.OK, modelResult.getOperationStatus());
        modelResult = client.putModel(test4, session);
        assertEquals(OperationStatus.OK, modelResult.getOperationStatus());
        modelResult = client.putModel(test5, session);
        assertEquals(OperationStatus.OK, modelResult.getOperationStatus());

        assertNull(getModel(new MainTestModelId(3)));
        assertNull(getModel(new MainTestModelId(4)));
        assertNull(getModel(new MainTestModelId(5)));

        final VoidResult voidResult = client.commit(session);
        assertEquals(OperationStatus.OK, voidResult.getOperationStatus());

        assertNotNull(getModel(new MainTestModelId(3)));
        assertNotNull(getModel(new MainTestModelId(4)));
        assertNotNull(getModel(new MainTestModelId(5)));
        assertNotNull(client.sendCommandForId(BaseCommand.GET, new MainTestModelId(3), null).getBody().getResponse().getResultList());
        assertNotNull(client.sendCommandForId(BaseCommand.GET, new MainTestModelId(4), null).getBody().getResponse().getResultList());
        assertNotNull(client.sendCommandForId(BaseCommand.GET, new MainTestModelId(5), null).getBody().getResponse().getResultList());

        client.removeModel(new MainTestModelId(3), null);
        client.removeModel(new MainTestModelId(4), null);
        client.removeModel(new MainTestModelId(5), null);

    }

    @org.junit.Test
    public void testDoublePhase() throws Exception {
        client.removeModel(new MainTestModelId(3), null);
        client.removeModel(new MainTestModelId(4), null);
        client.removeModel(new MainTestModelId(5), null);

        final MainTestModel test3 = getNewTestModel(3, "Test3");
        final MainTestModel test4 = getNewTestModel(4, "Test4");
        final MainTestModel test5 = getNewTestModel(5, "Test5");

        ModelsResult modelResult = client.putModel(test4, null);

        final SessionResult sessionResult = client.startSession();
        final String session = sessionResult.getSessionId();

        modelResult = client.putModel(test3, session);
        assertEquals(OperationStatus.OK, modelResult.getOperationStatus());
        assertNull(getModel(new MainTestModelId(3)));
        IdentifiersResult idResult = client.removeModel(new MainTestModelId(4), session);
        assertEquals(OperationStatus.OK, idResult.getOperationStatus());
        assertNotNull(getModel(new MainTestModelId(4)));
        modelResult = client.putModel(test5, session);
        assertEquals(OperationStatus.OK, modelResult.getOperationStatus());
        assertNull(getModel(new MainTestModelId(5)));

        client1.sendCommand(ServiceCommand.COMMIT_PHASE1.toString(), session);
        client2.sendCommand(ServiceCommand.COMMIT_PHASE1.toString(), session);

        assertNotNull(getModel(new MainTestModelId(3)));
        assertNull(getModel(new MainTestModelId(4)));
        assertNotNull(getModel(new MainTestModelId(5)));

        final FullMessage message = client.sendCommand(ServiceCommand.COMMIT_PHASE2.toString(), session);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        final VoidResult voidResult = client.commit(session);

        assertEquals(OperationStatus.OK, voidResult.getOperationStatus());
        assertNotNull(getModel(new MainTestModelId(3)));
        assertNull(getModel(new MainTestModelId(4)));
        assertNotNull(getModel(new MainTestModelId(5)));

        assertNotNull(client.getModel(new MainTestModelId(3), null).getModels());
        assertTrue(client.getModel(new MainTestModelId(4), null).getModels().isEmpty());
        assertNotNull(client.getModel(new MainTestModelId(5), null).getModels());

        idResult = client.removeModel(new MainTestModelId(3), null);
        assertEquals(OperationStatus.OK, idResult.getOperationStatus());

        idResult = client.removeModel(new MainTestModelId(5), null);
        assertEquals(OperationStatus.OK, idResult.getOperationStatus());

    }

    @org.junit.Test
    public void testRollbackAdd() throws Exception {
        FullMessage message;
        client.removeModel( new MainTestModelId(3), null);
        client.removeModel( new MainTestModelId(4), null);
        client.removeModel(new MainTestModelId(5), null);

        final MainTestModel test3 = getNewTestModel(3, "Test3");
        final MainTestModel test4 = getNewTestModel(4, "Test4");
        final MainTestModel test5 = getNewTestModel(5, "Test5");

        message = client.sendCommandForModel(BaseCommand.PUT, test4, null);

        message = client.sendCommand(BaseCommand.START_NEW_SESSION);
        final String session = message.getHeader().getSessionId();

        message = client.sendCommandForModel(BaseCommand.PUT, test3, session);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertNull(getModel(new MainTestModelId(3)));

        message = client.sendCommandForModel(BaseCommand.DELETE, test4, session);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertNotNull(getModel(new MainTestModelId(4)));

        message = client.sendCommandForModel(BaseCommand.PUT, test5, session);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertNull(getModel(new MainTestModelId(5)));

        client1.sendCommand(ServiceCommand.COMMIT_PHASE1.toString(), session);
        client2.sendCommand(ServiceCommand.COMMIT_PHASE1.toString(), session);

        assertNotNull(getModel(new MainTestModelId(3)));
        assertNull(getModel(new MainTestModelId(4)));
        assertNotNull(getModel(new MainTestModelId(5)));

        message = client.sendCommand(ServiceCommand.CLEAN_SAVE_SERVICE.toString(), session);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        message = client.sendCommand(BaseCommand.ROLLBACK, session);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        assertNull(getModel(new MainTestModelId(3)));
        assertNotNull(getModel(new MainTestModelId(4)));
        assertNull(getModel(new MainTestModelId(5)));

        assertTrue(client.sendCommandForId(BaseCommand.GET, new MainTestModelId(3), null).getBody().getResponse().getResultList().isEmpty());
        assertNotNull(client.sendCommandForId(BaseCommand.GET, new MainTestModelId(4), null).getBody().getResponse().getResultList());
        assertTrue(client.sendCommandForId(BaseCommand.GET, new MainTestModelId(5), null).getBody().getResponse().getResultList().isEmpty());

    }

    @org.junit.Test
    public void testRollbackUpdate() throws Exception {

        FullMessage message = client.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(3), null);
        client.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(4), null);
        client.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(5), null);

        final MainTestModel test3 = getNewTestModel(3, "Test3");
        MainTestModel test4 = getNewTestModel(4, "Test4");
        final MainTestModel test5 = getNewTestModel(5, "Test5");

        message = client.sendCommandForModel(BaseCommand.PUT, test4, null);

        message = client.sendCommand(BaseCommand.START_NEW_SESSION);
        final String session = message.getHeader().getSessionId();

        client.putModel(test3, session);
        test3.setName("Test3v1");
        ModelsResult modelsResult = client.putModel(test3, session);
        assertEquals(OperationStatus.OK, modelsResult.getOperationStatus());

        modelsResult = client.putModel(test5, session);
        assertEquals(OperationStatus.OK, modelsResult.getOperationStatus());

        test5.setName("Test5v1");
        modelsResult = client.putModel(test5, session);
        assertEquals(OperationStatus.OK, modelsResult.getOperationStatus());

        test4.setName("Test4v1");
        modelsResult = client.putModel(test4, session);
        assertEquals(OperationStatus.OK, modelsResult.getOperationStatus());

        client.sendCommand(ServiceCommand.COMMIT_PHASE1.toString(), session);

        assertNotNull(getModel(new MainTestModelId(3)));

        message = client.sendCommand(ServiceCommand.CLEAN_SAVE_SERVICE.toString(), session);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        message = client.sendCommand(BaseCommand.ROLLBACK, session);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        assertNull(getModel(new MainTestModelId(3)));

        test4 = (MainTestModel) getModel(new MainTestModelId(4));
        assertEquals("Test4", test4.getName());

        assertNull(getModel(new MainTestModelId(5)));

        client.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(3), null);
        client.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(4), null);
        client.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(5), null);

    }

    @org.junit.Test
    public void testRollbackDelete() throws Exception {

        FullMessage message = client.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(3), null);
        client.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(4), null);
        client.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(5), null);
        final MainTestModel test3 = getNewTestModel(3, "Test3");
        final MainTestModel test4 = getNewTestModel(4, "Test4");
        final MainTestModel test5 = getNewTestModel(5, "Test5");

        message = client.sendCommandForModel(BaseCommand.PUT, test3, null);
        message = client.sendCommandForModel(BaseCommand.PUT, test5, null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        message = client.sendCommand(BaseCommand.START_NEW_SESSION);
        final String session = message.getHeader().getSessionId();

        message = client.sendCommand(BaseCommand.START_NEW_SESSION);
        final String session2 = message.getHeader().getSessionId();

        message = client.sendCommandForId(BaseCommand.DELETE, test3.getIdentifier(), session);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertNotNull(getModel(new MainTestModelId(3)));
        assertNotNull(client.sendCommandForId(BaseCommand.GET, test3.getIdentifier(), null).getBody().getResponse().getResultList());
        assertNotNull(client.sendCommandForId(BaseCommand.GET, test3.getIdentifier(), session2).getBody().getResponse().getResultList());
        assertTrue(client.sendCommandForId(BaseCommand.GET, test3.getIdentifier(), session).getBody().getResponse().getResultList().isEmpty());

        message = client.sendCommandForModel(BaseCommand.PUT, test4, session);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertNull(getModel(new MainTestModelId(4)));
        assertTrue(client.sendCommandForId(BaseCommand.GET, test4.getIdentifier(), null).getBody().getResponse().getResultList().isEmpty());
        assertTrue(client.sendCommandForId(BaseCommand.GET, test4.getIdentifier(), session2).getBody().getResponse().getResultList().isEmpty());
        assertNotNull(client.sendCommandForId(BaseCommand.GET, test4.getIdentifier(), session).getBody().getResponse().getResultList());

        message = client.sendCommandForId(BaseCommand.DELETE, test5.getIdentifier(), session);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertNotNull(getModel(new MainTestModelId(5)));
        assertNotNull(client.sendCommandForId(BaseCommand.GET, test5.getIdentifier(), null).getBody().getResponse().getResultList());
        assertNotNull(client.sendCommandForId(BaseCommand.GET, test5.getIdentifier(), session2).getBody().getResponse().getResultList());
        assertTrue(client.sendCommandForId(BaseCommand.GET, test5.getIdentifier(), session).getBody().getResponse().getResultList().isEmpty());

//        client.sendCommand(ServiceCommand.COMMIT_PHASE1.toString(), session);
        client1.sendCommand(ServiceCommand.COMMIT_PHASE1.toString(), session);
        client2.sendCommand(ServiceCommand.COMMIT_PHASE1.toString(), session);

        assertNull(getModel(new MainTestModelId(3)));
        assertTrue(client.sendCommandForId(BaseCommand.GET, test3.getIdentifier(), null).getBody().getResponse().getResultList().isEmpty());
        assertTrue(client.sendCommandForId(BaseCommand.GET, test3.getIdentifier(), session2).getBody().getResponse().getResultList().isEmpty());
        assertTrue(client.sendCommandForId(BaseCommand.GET, test3.getIdentifier(), session).getBody().getResponse().getResultList().isEmpty());

        assertNotNull(getModel(new MainTestModelId(4)));
        assertNotNull(client.sendCommandForId(BaseCommand.GET, test4.getIdentifier(), null).getBody().getResponse().getResultList());
        assertNotNull(client.sendCommandForId(BaseCommand.GET, test4.getIdentifier(), session2).getBody().getResponse().getResultList());
        assertNotNull(client.sendCommandForId(BaseCommand.GET, test4.getIdentifier(), session).getBody().getResponse().getResultList());

        assertNull(getModel(new MainTestModelId(5)));
        assertTrue(client.sendCommandForId(BaseCommand.GET, test5.getIdentifier(), null).getBody().getResponse().getResultList().isEmpty());
        assertTrue(client.sendCommandForId(BaseCommand.GET, test5.getIdentifier(), session2).getBody().getResponse().getResultList().isEmpty());
        assertTrue(client.sendCommandForId(BaseCommand.GET, test5.getIdentifier(), session).getBody().getResponse().getResultList().isEmpty());

        assertNull(getModel(new MainTestModelId(3)));

        message = client.sendCommand(ServiceCommand.CLEAN_SAVE_SERVICE.toString(), session);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        message = client.sendCommand(BaseCommand.ROLLBACK, session);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        assertNotNull(getModel(new MainTestModelId(3)));
        assertNotNull(client.sendCommandForId(BaseCommand.GET, test3.getIdentifier(), null).getBody().getResponse().getResultList());
        assertNotNull(client.sendCommandForId(BaseCommand.GET, test3.getIdentifier(), session2).getBody().getResponse().getResultList());
        assertNotNull(client.sendCommandForId(BaseCommand.GET, test3.getIdentifier(), session).getBody().getResponse().getResultList());

        assertNull(getModel(new MainTestModelId(4)));
        assertTrue(client.sendCommandForId(BaseCommand.GET, test4.getIdentifier(), null).getBody().getResponse().getResultList().isEmpty());
        assertTrue(client.sendCommandForId(BaseCommand.GET, test4.getIdentifier(), session2).getBody().getResponse().getResultList().isEmpty());
        assertTrue(client.sendCommandForId(BaseCommand.GET, test4.getIdentifier(), session).getBody().getResponse().getResultList().isEmpty());

        assertNotNull(getModel(new MainTestModelId(5)));
        assertNotNull(client.sendCommandForId(BaseCommand.GET, test5.getIdentifier(), null).getBody().getResponse().getResultList());
        assertNotNull(client.sendCommandForId(BaseCommand.GET, test5.getIdentifier(), session2).getBody().getResponse().getResultList());
        assertNotNull(client.sendCommandForId(BaseCommand.GET, test5.getIdentifier(), session).getBody().getResponse().getResultList());

    }

    @BeforeClass
    public static void setUp() throws Exception {
        commonSetup();
        registerSerialized();

        version = 0x2;

        final List<StoreModelImpl> models1 = new ArrayList<>();
        models1.add(getStoreData("MainTestModel", "com.nomad.cache.test.model.MainTestModel", ServerType.CACHE_MANAGER));
        final List<StoreModelImpl> models2 = new ArrayList<>();
        models2.add(getStoreData("Child", "com.nomad.cache.test.model.Child"));
        models2.add(getStoreData("MainTestModel", "com.nomad.cache.test.model.MainTestModel"));

        final SessionServerModelImp sessionServerModel = getSessionServerModel(host, 5445, 8);
        sessionServerModel.setSessionTimeLive(60000);
        final SessionClientModelImpl sessionClientModel = getSessionClientModel(host, 5445, 2);

        final SaveServerModel saveServer = getSaveServerModel(host, 5888, 4);
        final SaveClientModel saveClient = getSaveClientModel(host, 5888, 2);

        final ServerModelImpl serverModel = new ServerModelImpl();
        serverModel.getStoreModels().addAll(models1);
        serverModel.getListeners().add(getListenerModel(host, 2222, 5));
        serverModel.getListeners().add(getListenerModel(host, 2232, 5));
        serverModel.setManagementServerModel(getManagementServerModel(2242, host, 2, 2000));
        serverModel.setServerName("main");
        serverModel.getSessionClientModels().add(sessionClientModel);
        serverModel.setSessionServerModel(sessionServerModel);
        serverModel.getSaveClientModels().add(saveClient);
        serverModel.getSaveServerModels().add(saveServer);

        final DataSourceModelImpl dataSourceModel = new DataSourceModelImpl();
        dataSourceModel.setName("a");
        dataSourceModel.setTimeOut(1000);
        dataSourceModel.setThreads(10);
        dataSourceModel.setClazz("com.nomad.cache.test.userdataaccess.BaseDataInvoker");
        dataSourceModel.addProperty("user", "sa");
        dataSourceModel.addProperty("password", "");
        dataSourceModel.addProperty("url", "jdbc:hsqldb:hsql://localhost/test");
        dataSourceModel.addProperty("driver", "org.hsqldb.jdbcDriver");
        serverModel.addDataSources(dataSourceModel);

        final CommandPluginModelImpl plugin = new CommandPluginModelImpl();
        plugin.setCheckDelay(1000);
        plugin.setClazz("com.nomad.plugin.IdGenerator");
        plugin.setPoolSize(10);
        plugin.getProperties().put("DataSourceName", "a");
        plugin.setTimeout(10);

        final CommandPluginModelImpl proxyPlugin = new CommandPluginModelImpl();
        proxyPlugin.setClazz("com.nomad.plugin.IdGeneratorProxy");
        proxyPlugin.setCheckDelay(1000);
        proxyPlugin.setPoolSize(10);
        proxyPlugin.setTimeout(600);

        serverModel.getCommandPlugins().add(plugin);

        dataInvoker = PmDataInvokerFactory.getDataInvoker("b", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:hsql://localhost/test", "sa", "", null, 1);

        launcher = new ServerLauncher(serverModel);

        launcher.start();

        final ServerModelImpl serverModel2 = new ServerModelImpl();
        serverModel2.getStoreModels().addAll(models2);
        final ListenerModelImpl listener = getListenerModel(host, 2422, 5);
        serverModel2.getListeners().add(listener);
        serverModel2.getListeners().add(getListenerModel(host, 2432, 5));
        serverModel2.setManagementServerModel(getManagementServerModel(2442, host, 2, 2000));
        serverModel2.setServerName("second");
        serverModel2.getSessionClientModels().add(sessionClientModel);

        serverModel2.addDataSources(dataSourceModel);

        serverModel2.getCommandPlugins().add(plugin);
        serverModel2.getSaveClientModels().add(saveClient);
        serverModel2.setTrustSessions(true);

        final ConnectModelImpl connectModel = getConnectModel(serverModel2, serverModel, 8, listener);

        serverModel2.getClients().add(connectModel);

        launcher2 = new ServerLauncher(serverModel2);

        launcher2.start();

        client1 = new SimpleCacheClient(host, 2232);
        client2 = new SimpleCacheClient(host, 2432);
        client = new SimpleCacheClient(host, port);

    }

    @Test
    public void orderTest() throws SystemException {

        client.removeModel(new MainTestModelId(3), null);
        client.removeModel(new MainTestModelId(4), null);
        client.removeModel(new MainTestModelId(5), null);

        final MainTestModel test3 = getNewTestModel(3, "Test3");
        //        final MainTestModel test4 = getNewTes(4, "Test4");
        //      final MainTestModel test5 = getNewTes(5, "Test5");


        final String sessionId = client.startSession().getSessionId();
        final String sessionId2= client.startSession().getSessionId();
        client.putModel(test3, sessionId);
        testModelName(test3.getIdentifier(),sessionId,1,"Test3");
        testModelName(test3.getIdentifier(),sessionId2,0,"");

        test3.setName("Step2");
        ModelsResult modelsResult = client.putModel(test3, sessionId);
        assertEquals(OperationStatus.OK, modelsResult.getOperationStatus());
        testModelName(test3.getIdentifier(),sessionId,1,"Step2");
        testModelName(test3.getIdentifier(),sessionId2,0,"");

        test3.setName("Step3");
        modelsResult = client.putModel(test3, sessionId);
        assertEquals(OperationStatus.OK, modelsResult.getOperationStatus());

        testModelName(test3.getIdentifier(),sessionId,1,"Step3");
        testModelName(test3.getIdentifier(),sessionId2,0,"");

        client.commit(sessionId);

        testModelName(test3.getIdentifier(),sessionId,1,"Step3");
        testModelName(test3.getIdentifier(),sessionId2,1,"Step3");

        client.commit(sessionId);
        testModelName(test3.getIdentifier(),sessionId,1,"Step3");
        testModelName(test3.getIdentifier(),sessionId2,1,"Step3");

        client.rollback(sessionId);
        testModelName(test3.getIdentifier(),sessionId,1,"Step3");
        testModelName(test3.getIdentifier(),sessionId2,1,"Step3");

    }

    @Test
    public void partialRollbackTest() throws SystemException {

        client.removeModel(new MainTestModelId(3), null);
        client.removeModel(new MainTestModelId(4), null);
        client.removeModel(new MainTestModelId(5), null);

        final MainTestModel test3 = getNewTestModel(3, "Test3");

        final String sessionId0 = client.startSession().getSessionId();
        final String sessionId2= client.startSession().getSessionId();
        final String sessionId01 = client.startChildSession(sessionId0).getSessionId();

        client.putModel(test3, sessionId0);
        testModelName(test3.getIdentifier(), sessionId0, 1, "Test3");
        testModelName(test3.getIdentifier(), sessionId2, 0, "Test3");

        test3.setName("Step2");
        ModelsResult modelsResult = client.putModel(test3, sessionId0);
        assertEquals(OperationStatus.OK, modelsResult.getOperationStatus());
        testModelName(test3.getIdentifier(), sessionId0, 1, "Step2");
        testModelName(test3.getIdentifier(), sessionId01, 1, "Step2");
        testModelName(test3.getIdentifier(), sessionId2, 0, "Test3");

        test3.setName("Step3");
        modelsResult = client.putModel(test3, sessionId01);
        assertEquals(OperationStatus.OK, modelsResult.getOperationStatus());

        testModelName(test3.getIdentifier(), sessionId0, 1, "Step3");
        testModelName(test3.getIdentifier(),sessionId2,0,"");
        client.rollback(sessionId01);

        testModelName(test3.getIdentifier(), sessionId0, 1, "Step2");
        testModelName(test3.getIdentifier(),sessionId2,0,"");
        client.commit(sessionId0);

        testModelName(test3.getIdentifier(), sessionId0, 1, "Step2");
        testModelName(test3.getIdentifier(),sessionId2,1,"Step2");

    }
    private void testModelName(final Identifier id, final String sessionId,final int count,final String name) throws SystemException{
        final ModelsResult modelsResult = client.getModel(id, sessionId);
        assertEquals(OperationStatus.OK, modelsResult.getOperationStatus());
        assertEquals(count, modelsResult.getModels().size());
        if(count>0){
            MainTestModel testModel = (MainTestModel) modelsResult.getModels().iterator().next();
            assertEquals(name, testModel.getName());
        }

    }
    @AfterClass
    public static void tearDown() throws Exception {
        dataInvoker.close();
        launcher.stop();
        launcher2.stop();
        client.close();
        client2.close();
    }

    private Model getModel(final Identifier id) throws SystemException {
        try {
            return dataInvoker.getModel(id);
        } catch (final ModelNotExistException e) {
        }
        return null;
    }
}
