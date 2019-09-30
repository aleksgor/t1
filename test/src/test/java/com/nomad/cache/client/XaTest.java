package com.nomad.cache.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.nomad.cache.test.model.MainTestModel;
import com.nomad.cache.test.model.MainTestModelId;
import com.nomad.client.ModelsResult;
import com.nomad.client.SimpleCacheClient;
import com.nomad.client.VoidResult;
import com.nomad.exception.ModelNotExistException;
import com.nomad.exception.SystemException;
import com.nomad.message.FullMessage;
import com.nomad.message.OperationStatus;
import com.nomad.model.BaseCommand;
import com.nomad.model.CommandPluginModelImpl;
import com.nomad.model.DataSourceModelImpl;
import com.nomad.model.Identifier;
import com.nomad.model.ListenerModelImpl;
import com.nomad.model.Model;
import com.nomad.model.ServerModelImpl;
import com.nomad.model.ServiceCommand;
import com.nomad.model.StoreModelImpl;
import com.nomad.model.session.SessionServerModel;
import com.nomad.model.session.SessionServerModelImp;
import com.nomad.pm.PmDataInvokerFactory;
import com.nomad.server.PmDataInvoker;
import com.nomad.server.ServerLauncher;

public class XaTest extends CommonTest {

    private static ServerLauncher launcher;

    private static int port = 2222;

    private static SimpleCacheClient client;
    private static PmDataInvoker dataInvoker;

    /*
     * test session isolation
     */
    @org.junit.Test
    public void test1() throws Exception {

        client.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(3));

        final String session = client.startSession().getSessionId();
        final MainTestModel test = new MainTestModel();
        test.setId(3);
        test.setName("aljfhajhdfvsjdbvjshdvjqhvjkeqwvhljqevcljqw dlcqld");
        final ModelsResult modelsResult = client.putModel(test, session);
        assertEquals(OperationStatus.OK, modelsResult.getOperationStatus());
        assertNull(getModel(new MainTestModelId(3)));
        final VoidResult voidResult = client.commit(session);
        assertEquals(OperationStatus.OK, voidResult.getOperationStatus());
        assertNotNull(getModel(new MainTestModelId(3)));

        client.removeModel(new MainTestModelId(3), null);
    }

    private Model getModel(final Identifier id) throws SystemException {
        try {
            return dataInvoker.getModel(id);
        } catch (final ModelNotExistException e) {
        }
        return null;
    }

    @org.junit.Test
    public void testDoublePhase() throws Exception {

        FullMessage message = client.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(3), null);

        final MainTestModel test = new MainTestModel();
        test.setId(3);
        test.setName("aljfhajhdfvsjdbvjshdvjqhvjkeqwvhljqevcljqw dlcqld");
        message = client.sendCommand(BaseCommand.START_NEW_SESSION);
        final String session = message.getHeader().getSessionId();

        message = client.sendCommandForModel(BaseCommand.PUT, test, session);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertNull(getModel(new MainTestModelId(3)));
        message = client.sendCommand(ServiceCommand.COMMIT_PHASE1.toString(), session);

        assertNotNull(getModel(new MainTestModelId(3)));

        message = client.sendCommand(ServiceCommand.COMMIT_PHASE2.toString(), session);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertNotNull(getModel(new MainTestModelId(3)));

        message = client.sendCommandForId(BaseCommand.GET, new MainTestModelId(3));
        assertNotNull(message.getBody().getResponse().getResultList());
        message = client.sendCommandForId(BaseCommand.COMMIT, new MainTestModelId(3), session);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        message = client.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(3), null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

    }

    @org.junit.Test
    public void testRollbackAdd() throws Exception {

        FullMessage message = client.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(3));

        final MainTestModel test = new MainTestModel();
        test.setId(3);
        test.setName("aljfhajhdfvsjdbvjshdvjqhvjkeqwvhljqevcljqw dlcqld");
        message = client.sendCommand(BaseCommand.START_NEW_SESSION);
        final String session = message.getHeader().getSessionId();

        message = client.sendCommandForModel(BaseCommand.PUT, test, session);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertNull(getModel(new MainTestModelId(3)));
        message = client.sendCommand(ServiceCommand.COMMIT_PHASE1.toString(), session);

        assertNotNull(getModel(new MainTestModelId(3)));

        message = client.sendCommand(BaseCommand.ROLLBACK, session);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertNull(getModel(new MainTestModelId(3)));

        message = client.sendCommandForId(BaseCommand.GET, new MainTestModelId(3));
        assertTrue(message.getBody().getResponse().getResultList().isEmpty());


    }

    @org.junit.Test
    public void testRollbackUpdate() throws Exception {

        FullMessage message = client.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(3), null);

        MainTestModel test = new MainTestModel();
        test.setId(3);
        test.setName("first");

        message = client.sendCommandForModel(BaseCommand.PUT, test, null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        test.setName("second");

        message = client.sendCommand(BaseCommand.START_NEW_SESSION);
        final String session = message.getHeader().getSessionId();

        message = client.sendCommandForModel(BaseCommand.PUT, test, session);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        message = client.sendCommand(ServiceCommand.COMMIT_PHASE1.toString(), session);

        assertNotNull(getModel(new MainTestModelId(3)));

        message = client.sendCommand(BaseCommand.ROLLBACK, session);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        test = (MainTestModel) getModel(new MainTestModelId(3));
        assertEquals("first", test.getName());

        message = client.sendCommandForId(BaseCommand.GET, new MainTestModelId(3), null);
        test = (MainTestModel) message.getBody().getResponse().getResultList().iterator().next();
        assertEquals("first", test.getName());


    }

    @org.junit.Test
    public void testRollbackDelete() throws Exception {

        FullMessage message = client.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(3), null);

        final MainTestModel test = new MainTestModel();
        test.setId(3);
        test.setName("first");

        message = client.sendCommandForModel(BaseCommand.PUT, test);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        message = client.sendCommand(BaseCommand.START_NEW_SESSION);
        final String session = message.getHeader().getSessionId();

        message = client.sendCommandForId(BaseCommand.DELETE, test.getIdentifier(), session);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        message = client.sendCommand(ServiceCommand.COMMIT_PHASE1.toString(), session);

        assertNull(getModel(new MainTestModelId(3)));

        message = client.sendCommand(BaseCommand.ROLLBACK, session);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertNotNull(getModel(new MainTestModelId(3)));

        message = client.sendCommandForId(BaseCommand.GET, new MainTestModelId(3));
        assertNotNull(message.getBody().getResponse().getResultList());
    }

    @BeforeClass
    public static void setUp() throws Exception {

        // launcher.start();
        commonSetup();
        final ListenerModelImpl listener = new ListenerModelImpl();
        // ls.setClazz("com.nomad.server.HttpListener");
        listener.setPort(2222);
        listener.setMinThreads(10);
        listener.setMaxThreads(10);
        listener.setBacklog(10);
        final List<StoreModelImpl> models = new ArrayList<>();

        models.add(getStoreData("MainTestModel", "com.nomad.cache.test.model.MainTestModel"));
        models.add(getStoreData("Child", "com.nomad.cache.test.model.Child"));

        final SessionServerModel sessionServer= new SessionServerModelImp();
        sessionServer.setSessionTimeLive(30000);
        sessionServer.setMaxThreads(30);
        sessionServer.setMinThreads(30);
        // final SessionClientModel sessionclient = new SessionClientModelImpl();


        final ServerModelImpl serverModel = new ServerModelImpl();
        serverModel.setManagementServerModel(getManagementServerModel(2224, host, 2, 2000));
        serverModel.getStoreModels().addAll(models);
        serverModel.getListeners().add(listener);
        serverModel.setServerName("main");
        serverModel.setCalculateStatistic(false);

        serverModel.setSessionServerModel(sessionServer);
        // serverModel.getSessionClientModels().add(sessionclient);
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

        final CommandPluginModelImpl plugin = new CommandPluginModelImpl();
        plugin.setCheckDelay(10);
        plugin.setClazz("com.nomad.plugin.IdGenerator");
        plugin.setPoolSize(10);
        plugin.getProperties().put("DataSourceName", "a");
        plugin.setTimeout(10);

        serverModel.getCommandPlugins().add(plugin);

        plugin.setTimeout(10);

        serverModel.getCommandPlugins().add(plugin);
        serverModel.getProperties().setProperty("SessionStorePath", "/opt/t1/sessionstore");

        dataInvoker = PmDataInvokerFactory.getDataInvoker("b", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:hsql://localhost/test", "sa", "", null, 1);
        launcher = new ServerLauncher(serverModel);

        launcher.start();

        client = new SimpleCacheClient(host, port);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        client.close();
        launcher.stop();
        dataInvoker.close();
    }

}
