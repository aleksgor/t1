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
import com.nomad.client.ModelsResult;
import com.nomad.client.SimpleCacheClient;
import com.nomad.exception.ModelNotExistException;
import com.nomad.message.FullMessage;
import com.nomad.message.OperationStatus;
import com.nomad.model.BaseCommand;
import com.nomad.model.DataSourceModelImpl;
import com.nomad.model.ListenerModelImpl;
import com.nomad.model.ServerModelImpl;
import com.nomad.model.StoreModelImpl;
import com.nomad.model.session.SessionServerModel;
import com.nomad.model.session.SessionServerModelImp;
import com.nomad.pm.PmDataInvokerFactory;
import com.nomad.server.PmDataInvoker;
import com.nomad.server.ServerLauncher;

public class ServerTest extends CommonTest {

    private static ServerLauncher launcher;

    private static int port = 2222;
    private static SimpleCacheClient client;
    private static PmDataInvoker dataInvoker;

    /*
     * test session isolation
     */
    @org.junit.Test
    public void testSessionIsolation() throws Exception {
        FullMessage message = client.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(3), null);
        final MainTestModel t = new MainTestModel();
        t.setId(3);
        t.setName("test3");
        message = client.sendCommand(BaseCommand.START_NEW_SESSION);
        final String session1 = message.getHeader().getSessionId();

        message = client.sendCommand(BaseCommand.START_NEW_SESSION);
        final String session2 = message.getHeader().getSessionId();

        ModelsResult mResult = client.putModel(t, session1);
        assertEquals(OperationStatus.OK, mResult.getOperationStatus());

        message = client.sendCommandForId(BaseCommand.GET, new MainTestModelId(3), session2);
        assertTrue("empty result", message.getBody().getResponse().getResultList().isEmpty());

        message = client.sendCommandForId(BaseCommand.GET, new MainTestModelId(3), session1);
        assertNotNull("not empty list", message.getBody().getResponse().getResultList());
        client.sendCommand(BaseCommand.ROLLBACK, session1);

    }

    @org.junit.Test
    public void test2() throws Exception {

        FullMessage message = client.sendCommandForId("Delete", new MainTestModelId(3), null);
        // sendCommand("Commit", null, message.getHeader().getSessionId());

        final MainTestModel t = new MainTestModel();
        t.setId(3);
        t.setName("test3");
        message = client.sendCommand(BaseCommand.START_NEW_SESSION);
        final String session1 = message.getHeader().getSessionId();
        message = client.sendCommandForModel(BaseCommand.PUT, t, session1);
        client.sendCommand(BaseCommand.COMMIT, session1);

        final MainTestModel testModel = (MainTestModel) dataInvoker.getModel(new MainTestModelId(3));
        assertNotNull(testModel);

        message = client.sendCommandForId(BaseCommand.GET, new MainTestModelId(3), session1);
        assertNotNull(message.getBody().getResponse().getResultList());

        message = client.sendCommandForId(BaseCommand.GET, new MainTestModelId(3), session1);
        assertNotNull(message.getBody().getResponse().getResultList());


    }

    @org.junit.Test
    public void testCommit() throws Exception {

        FullMessage message = client.sendCommandForId("Delete", new MainTestModelId(3), null);
        client.sendCommand("Commit", message.getHeader().getSessionId());

        final MainTestModel t = new MainTestModel();
        t.setId(3);
        t.setName("test3");
        final String session1 = client.sendCommand(BaseCommand.START_NEW_SESSION).getHeader().getSessionId();
        message = client.sendCommandForModel(BaseCommand.PUT, t, session1);
        assertEquals(session1, message.getHeader().getSessionId());

        message = client.sendCommand(BaseCommand.COMMIT, session1);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        message = client.sendCommandForId(BaseCommand.GET, new MainTestModelId(3), session1);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(session1, message.getHeader().getSessionId());

        message = client.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(3), session1);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        final String session2 = client.sendCommand(BaseCommand.START_NEW_SESSION).getHeader().getSessionId();

        message = client.sendCommandForId(BaseCommand.GET, new MainTestModelId(3), session2);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertNotNull(message.getBody().getResponse().getResultList());
        message = client.sendCommandForId(BaseCommand.COMMIT, new MainTestModelId(3), session1);

        try {
            dataInvoker.getModel(new MainTestModelId(3));
            fail();
        } catch (final ModelNotExistException e) {

        }
    }


    @org.junit.Test
    public void testCloseSession() throws Exception {

        FullMessage message = client.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(3), null);

        final MainTestModel t = new MainTestModel();
        t.setId(3);
        t.setName("test3");
        ModelsResult mResult = client.putModel(t, null);
        assertEquals(OperationStatus.OK, mResult.getOperationStatus());

        final String session2 = client.sendCommand(BaseCommand.START_NEW_SESSION).getHeader().getSessionId();
        message = client.sendCommandForId(BaseCommand.GET, new MainTestModelId(3), session2);

        message = client.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(3), session2);

        message = client.sendCommand(BaseCommand.CLOSE_SESSION, session2);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        // test close session
        message = client.sendCommandForId(BaseCommand.GET, new MainTestModelId(3), session2);
        assertEquals(OperationStatus.INVALID_SESSION, message.getResult().getOperationStatus());

        client.closeSession(session2);


    }

    @org.junit.Test
    public void testBlock() throws Exception {

        FullMessage message = client.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(3), null);

        final MainTestModel t = new MainTestModel();
        t.setId(3);
        t.setName("test3");

        final String session1 = client.sendCommandForId(BaseCommand.START_NEW_SESSION, new MainTestModelId(3), null).getHeader().getSessionId();

        message = client.sendCommandForModel(BaseCommand.PUT, t, session1);
        client.sendCommand(BaseCommand.COMMIT, session1);

        message = client.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(3), session1);
        final String session2 = client.sendCommandForId(BaseCommand.START_NEW_SESSION, new MainTestModelId(3), null).getHeader().getSessionId();
        message = client.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(3), session2);
        assertEquals(OperationStatus.BLOCKED, message.getResult().getOperationStatus());

        message = client.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(3), null);
        assertEquals(OperationStatus.BLOCKED, message.getResult().getOperationStatus());

        client.sendCommand(BaseCommand.ROLLBACK, session1);

        message = client.sendCommandForId(BaseCommand.ROLLBACK, new MainTestModelId(3), session2);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());


    }

    @BeforeClass
    public static void setUp() throws Exception {
        version = 0x1;
        registerSerialized();
        // launcher.start();
        final SessionServerModel sessionServer = new SessionServerModelImp();
        sessionServer.setSessionTimeLive(10000000);
        final ListenerModelImpl listener = new ListenerModelImpl();
        // ls.setClazz("com.nomad.server.HttpListener");
        listener.setPort(2222);
        listener.setMinThreads(10);
        listener.setMaxThreads(10);
        listener.setBacklog(10);
        final List<StoreModelImpl> models = new ArrayList<>();

        models.add(getStoreData("MainTestModel", "com.nomad.cache.test.model.MainTestModel"));
        models.add(getStoreData("Child", "com.nomad.cache.test.model.Child"));

        final ServerModelImpl serverModel = new ServerModelImpl();
        serverModel.setManagementServerModel(getManagementServerModel(2224, host, 2, 2000));
        serverModel.getStoreModels().addAll(models);
        serverModel.getListeners().add(listener);
        serverModel.setSessionServerModel(sessionServer);

        final DataSourceModelImpl dataSource = new DataSourceModelImpl();
        dataSource.setName("a");
        dataSource.setTimeOut(1000);
        dataSource.setThreads(12);
        dataSource.setClazz("com.nomad.cache.test.userdataaccess.BaseDataInvoker");
        /*
         * dataSource.addProperty("user", "sa"); dataSource.addProperty("password", ""); dataSource.addProperty("url", "jdbc:hsqldb:hsql://localhost/test");
         * dataSource.addProperty("driver", "org.hsqldb.jdbcDriver");
         */dataSource.addProperty("user", "sa");
         dataSource.addProperty("password", "");
         dataSource.addProperty("url", "jdbc:hsqldb:hsql://localhost/test");
         dataSource.addProperty("driver", "org.hsqldb.jdbcDriver");
         serverModel.addDataSources(dataSource);

         /*
          * "a", null, "jdbc:postgresql://localhost:5432/test", "test", "test", "pm.cfg.xml" },
          */
         serverModel.setLocalSessions(true);

         dataInvoker = PmDataInvokerFactory.getDataInvoker("b", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:hsql://localhost/test", "sa", "", null, 1);
         // dataInvoker = PmDataInvokerFactory.getDataInvoker("b", "org.postgresql.Driver", "jdbc:postgresql://localhost:5432/test", "test", "test", null, 1);
         launcher = new ServerLauncher(serverModel);

         launcher.start();
         client = new SimpleCacheClient(host, port);

    }

    @AfterClass
    public static void tearDown() throws Exception {
        launcher.stop();
        dataInvoker.getConnection().close();
        client.close();

    }
}
