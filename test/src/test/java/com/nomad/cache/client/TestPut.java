package com.nomad.cache.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.nomad.cache.test.model.MainTestModel;
import com.nomad.cache.test.model.MainTestModelId;
import com.nomad.client.IdentifiersResult;
import com.nomad.client.ModelsResult;
import com.nomad.client.SimpleCacheClient;
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
import com.nomad.model.session.SessionServerModelImp;
import com.nomad.pm.PmDataInvokerFactory;
import com.nomad.server.PmDataInvoker;
import com.nomad.server.ServerLauncher;

public class TestPut extends CommonTest {

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

    /**
     * Put into 2 cache (from 3)
     */
    @org.junit.Test
    public void testPut1() throws Exception {
        
        final List<Identifier> ids = new ArrayList<>();
        ids.add(new MainTestModelId(1));
        ids.add(new MainTestModelId(2));
        ids.add(new MainTestModelId(3));
        ids.add(new MainTestModelId(4));
        ids.add(new MainTestModelId(5));
        final IdentifiersResult idMessage = clientMain.removeModels(ids, null);
        assertEquals(OperationStatus.OK, idMessage.getOperationStatus());
        final Collection<Model> models = dataInvoker.getModel(ids);
        assertEquals(0, models.size());
        FullMessage message;
        for (int i = 0; i < 6; i++) {
            message = clientMain.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE.toString(), new MainTestModelId(i + 1), null);
            assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
            assertTrue(message.getBody().getResponse().getIdentifiers().isEmpty());

            message = client2.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE.toString(), new MainTestModelId(i + 1), null);
            assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
            assertTrue(message.getBody().getResponse().getIdentifiers().isEmpty());
            message = client3.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE.toString(), new MainTestModelId(i + 1), null);

            assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
            assertTrue(message.getBody().getResponse().getIdentifiers().isEmpty());

        }

        final MainTestModel t1 = getNewTestModel(1, "tes1");
        models.add(t1);
        final ModelsResult mMessage = clientMain.putModels(models, null);
        assertEquals(OperationStatus.OK, mMessage.getOperationStatus());

        int calculator = 0;
        message = clientMain.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE.toString(), new MainTestModelId(1), null);
        assertTrue(message.getBody().getResponse().getIdentifiers().size() < 2);
        calculator += message.getBody().getResponse().getIdentifiers().size();
        message = client2.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE.toString(), new MainTestModelId(1), null);
        assertTrue(message.getBody().getResponse().getIdentifiers().size() < 2);
        calculator += message.getBody().getResponse().getIdentifiers().size();
        message = client3.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE.toString(), new MainTestModelId(1), null);
        assertTrue(message.getBody().getResponse().getIdentifiers().size() < 2);
        calculator += message.getBody().getResponse().getIdentifiers().size();
        assertEquals(2, calculator);


    }

    /**
     * Put into 2 cache (from 3) (1 exist)
     */

    @org.junit.Test
    public void testPutRightInCache() throws Exception {
        final List<Identifier> ids = new ArrayList<>();
        ids.add(new MainTestModelId(1));
        ids.add(new MainTestModelId(2));
        ids.add(new MainTestModelId(3));
        ids.add(new MainTestModelId(4));
        ids.add(new MainTestModelId(5));

        final Collection<Model> models = dataInvoker.getModel(ids);
        FullMessage message;
        cleanCache();
        final MainTestModel t1 = getNewTestModel(1, "tes1");
        models.add(t1);
        message = clientMain.sendCommandForModel(ServiceCommand.PUT_INTO_CACHE.name(), t1, null);
        message = clientMain.sendCommandForId(ServiceCommand.GET_FROM_CACHE.name(), t1.getIdentifier(), null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(1,  message.getBody().getResponse().getResultList().size());
        assertEquals(t1, message.getBody().getResponse().getResultList().iterator().next());

        message = client2.sendCommandForId(ServiceCommand.GET_FROM_CACHE.name(), t1.getIdentifier(), null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(0, message.getBody().getResponse().getResultList().size());

        message = client3.sendCommandForId(ServiceCommand.GET_FROM_CACHE.name(), t1.getIdentifier(), null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(0, message.getBody().getResponse().getResultList().size());


    }

    /**
     * Put into 2 cache (from 3) (1 exist)
     */

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @org.junit.Test
    public void testPut2() throws Exception {
        final List<Identifier> ids = new ArrayList<>();
        ids.add(new MainTestModelId(1));
        ids.add(new MainTestModelId(2));
        ids.add(new MainTestModelId(3));
        ids.add(new MainTestModelId(4));
        ids.add(new MainTestModelId(5));

        Collection<Model> models = new ArrayList<>();
        models = dataInvoker.getModel(ids);
        FullMessage message;
        cleanCache();
        final MainTestModel t1 = getNewTestModel(1, "tes1");
        models.add(t1);

        message = clientMain.sendCommandForModel(BaseCommand.PUT, (List) models, null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        int calculator = 0;
        message = clientMain.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE.toString(), new MainTestModelId(1), null);
        assertTrue(message.getBody().getResponse().getIdentifiers().size() < 2);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        calculator += message.getBody().getResponse().getIdentifiers().size();

        message = client2.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE.toString(), new MainTestModelId(1), null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertTrue(message.getBody().getResponse().getIdentifiers().size() < 2);
        calculator += message.getBody().getResponse().getIdentifiers().size();
        message = client3.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE.toString(), new MainTestModelId(1), null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertTrue(message.getBody().getResponse().getIdentifiers().size() < 2);
        calculator += message.getBody().getResponse().getIdentifiers().size();
        assertEquals(2, calculator);

    }

    /**
     * Put into 2 cache (from 3) (1 exist) plus session
     */

    @org.junit.Test
    public void testPut3() throws Exception {
        cleanCache();

        final List<Model> models = new ArrayList<>();

        FullMessage message;

        message = clientMain.sendCommand(BaseCommand.START_NEW_SESSION);
        final String sessionId = message.getHeader().getSessionId();
        final MainTestModel t1 = getNewTestModel(1, "tes1");
        models.add(t1);
        message = clientMain.sendCommandForModel(ServiceCommand.PUT_INTO_CACHE.toString(), t1, null);
        message = clientMain.sendCommandForId(ServiceCommand.GET_FROM_CACHE.toString(), t1.getIdentifier(), null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(t1, message.getBody().getResponse().getResultList().iterator().next());

        message = client2.sendCommandForId(ServiceCommand.GET_FROM_CACHE.toString(), t1.getIdentifier(), null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(0, message.getBody().getResponse().getResultList().size());

        message = client3.sendCommandForId(ServiceCommand.GET_FROM_CACHE.toString(), t1.getIdentifier(), null);

        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(0, message.getBody().getResponse().getResultList().size());
        message = clientMain.sendCommandForModel(BaseCommand.PUT, models, sessionId);

        assertEquals(2, getCountInCache(new MainTestModelId(1), sessionId));

        message = clientMain.sendCommand(BaseCommand.COMMIT, sessionId);

    }

    /**
     * Put into 2 cache (from 3) (1 exist) plus session with different sessions
     */

    @org.junit.Test
    public void testPutCommitRollback() throws Exception {
        FullMessage message;
        cleanCache();

        final List<Model> models = new ArrayList<>();

        message = clientMain.sendCommand(BaseCommand.START_NEW_SESSION);
        final String sessionId1 = message.getHeader().getSessionId();
        message = clientMain.sendCommand(BaseCommand.START_NEW_SESSION);
        final String sessionId2 = message.getHeader().getSessionId();
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        final MainTestModel t1 = getNewTestModel(1, "test1");
        final MainTestModel t2 = getNewTestModel(2, "test2");
        final MainTestModel t3 = getNewTestModel(3, "test3");
        models.add(t1);
        models.add(t2);
        models.add(t3);
        final List<Identifier> ids = new ArrayList<>();
        ids.add(new MainTestModelId(1));
        ids.add(new MainTestModelId(2));
        ids.add(new MainTestModelId(3));

        ModelsResult mr;
        message = clientMain.sendCommandForModel(BaseCommand.PUT.toString(), models, sessionId1);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        assertEquals(2, getCountInCache(new MainTestModelId(1), null));
        assertEquals(2, getCountInCache(new MainTestModelId(2), null));
        assertEquals(2, getCountInCache(new MainTestModelId(3), null));
        message = clientMain.sendCommandForId(BaseCommand.GET.toString(), ids, sessionId2);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(0, message.getBody().getResponse().getResultList().size());

        message = clientMain.sendCommandForId(BaseCommand.GET.toString(), ids, null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(0, message.getBody().getResponse().getResultList().size());

        mr = clientMain.getModels(ids, sessionId1);
        assertEquals(OperationStatus.OK, mr.getOperationStatus());
        assertEquals(3, mr.getModels().size());

        message = clientMain.sendCommand(BaseCommand.ROLLBACK.toString(), sessionId1);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(0, getCountInCache(new MainTestModelId(1), null));
        assertEquals(0, getCountInCache(new MainTestModelId(2), null));
        assertEquals(0, getCountInCache(new MainTestModelId(3), null));

        message = clientMain.sendCommandForModel(BaseCommand.PUT.toString(), models, sessionId1);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        message = clientMain.sendCommandForId(BaseCommand.GET.toString(), ids, sessionId2);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(0, message.getBody().getResponse().getResultList().size());
        assertEquals(2, getCountInCache(new MainTestModelId(1), null));
        assertEquals(2, getCountInCache(new MainTestModelId(2), null));
        assertEquals(2, getCountInCache(new MainTestModelId(3), null));

        message = clientMain.sendCommandForId(BaseCommand.DELETE.toString(), new MainTestModelId(1), sessionId2);
        assertEquals(OperationStatus.BLOCKED, message.getResult().getOperationStatus());
        assertEquals(2, getCountInCache(new MainTestModelId(1), null));
        assertEquals(2, getCountInCache(new MainTestModelId(2), null));
        assertEquals(2, getCountInCache(new MainTestModelId(3), null));

        message = clientMain.sendCommandForModel(BaseCommand.PUT.toString(), t1, sessionId2);
        assertEquals(OperationStatus.BLOCKED, message.getResult().getOperationStatus());
        assertEquals(2, getCountInCache(new MainTestModelId(1), null));
        assertEquals(2, getCountInCache(new MainTestModelId(2), null));
        assertEquals(2, getCountInCache(new MainTestModelId(3), null));

        message = clientMain.sendCommand(BaseCommand.COMMIT.toString(), sessionId1);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        assertEquals(2, getCountInCache(new MainTestModelId(1), null));
        assertEquals(2, getCountInCache(new MainTestModelId(2), null));
        assertEquals(2, getCountInCache(new MainTestModelId(3), null));

        // delete
        message = clientMain.sendCommandForId(BaseCommand.DELETE.toString(), new MainTestModelId(1), sessionId2);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(2, getCountInCache(new MainTestModelId(1), null));
        assertEquals(2, getCountInCache(new MainTestModelId(2), null));
        assertEquals(2, getCountInCache(new MainTestModelId(3), null));
        message = clientMain.sendCommand(BaseCommand.COMMIT.toString(), sessionId1);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        message = clientMain.sendCommand(BaseCommand.COMMIT.toString(), sessionId2);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

    }

    /**
     * Put into 2 cache (from 3) (1 exist) plus session
     */

    @org.junit.Test
    public void testCommitRollback() throws Exception {

        cleanCache();

        final List<Model> models = new ArrayList<>();

        FullMessage message;

        message = clientMain.sendCommand(BaseCommand.START_NEW_SESSION);
        final String session1 = message.getHeader().getSessionId();
        message = clientMain.sendCommand(BaseCommand.START_NEW_SESSION);
        final String session2 = message.getHeader().getSessionId();

        final MainTestModel t1 = getNewTestModel(1, "test1");
        models.add(t1);
        message = clientMain.sendCommandForModel(BaseCommand.PUT, t1, session1);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        message = clientMain.sendCommandForModel(BaseCommand.PUT, t1, null);
        assertEquals(OperationStatus.BLOCKED, message.getResult().getOperationStatus());

        message = clientMain.sendCommandForModel(BaseCommand.PUT, t1, session2);
        assertEquals(OperationStatus.BLOCKED, message.getResult().getOperationStatus());

        message = clientMain.sendCommandForId(BaseCommand.GET, new MainTestModelId(1), session2);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(0, message.getBody().getResponse().getResultList().size());

        message = clientMain.sendCommandForId(BaseCommand.GET, new MainTestModelId(1), null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(0, message.getBody().getResponse().getResultList().size());

        message = clientMain.sendCommandForId(BaseCommand.GET.toString(), new MainTestModelId(1), session1);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(1, message.getBody().getResponse().getResultList().size());

        message = clientMain.sendCommand(BaseCommand.ROLLBACK.toString(), session1);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        message = clientMain.sendCommandForId(BaseCommand.GET.toString(), new MainTestModelId(1), session1);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(0, message.getBody().getResponse().getResultList().size());

        message = clientMain.sendCommandForModel(BaseCommand.PUT.toString(), t1, session1);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        message = clientMain.sendCommand(BaseCommand.COMMIT.toString(), session1);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        message = clientMain.sendCommandForId(BaseCommand.GET.toString(), new MainTestModelId(1), session2);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(1, message.getBody().getResponse().getResultList().size());

        t1.setName("ppppp1");
        message = clientMain.sendCommandForModel(BaseCommand.PUT, t1, session2);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        message = clientMain.sendCommandForId(BaseCommand.GET, new MainTestModelId(1), session1);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(1, message.getBody().getResponse().getResultList().size());
        assertEquals("test1", ((MainTestModel) message.getBody().getResponse().getResultList().iterator().next()).getName());

        final ModelsResult modelsResult = clientMain.getModel(new MainTestModelId(1), session2);
        assertEquals(OperationStatus.OK, modelsResult.getOperationStatus());
        assertEquals(1, modelsResult.getModels().size());
        assertEquals("ppppp1", ((MainTestModel) modelsResult.getModels().get(0)).getName());

        message = clientMain.sendCommand(BaseCommand.COMMIT.toString(), session2);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        message = clientMain.sendCommandForId(BaseCommand.GET.toString(), new MainTestModelId(1), session1);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(1, message.getBody().getResponse().getResultList().size());
        assertEquals("ppppp1", ((MainTestModel) message.getBody().getResponse().getResultList().iterator().next()).getName());

    }

    @org.junit.Test
    public void testManyModelCommitRollback() throws Exception {
        cleanCache();

        final List<Model> models = new ArrayList<>();
        final List<Identifier> ids = new ArrayList<>();
        ids.add(new MainTestModelId(1));
        ids.add(new MainTestModelId(2));
        ids.add(new MainTestModelId(3));

        FullMessage message;

        message = clientMain.sendCommand(BaseCommand.START_NEW_SESSION);
        final String session1 = message.getHeader().getSessionId();

        message = clientMain.sendCommand(BaseCommand.START_NEW_SESSION);
        final String session2 = message.getHeader().getSessionId();

        final MainTestModel t1 = getNewTestModel(1, "test1");
        models.add(t1);
        final MainTestModel t2 = getNewTestModel(2, "test2");
        models.add(t2);
        final MainTestModel t3 = getNewTestModel(3, "test3");
        models.add(t3);

        message = clientMain.sendCommandForModel(BaseCommand.PUT.toString(), models, session1);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        message = clientMain.sendCommandForModel(BaseCommand.PUT.toString(), t1, null);
        assertEquals(OperationStatus.BLOCKED, message.getResult().getOperationStatus());

        message = clientMain.sendCommandForModel(BaseCommand.PUT.toString(), t2, null);
        assertEquals(OperationStatus.BLOCKED, message.getResult().getOperationStatus());

        message = clientMain.sendCommandForModel(BaseCommand.PUT.toString(), t3, null);
        assertEquals(OperationStatus.BLOCKED, message.getResult().getOperationStatus());

        message = clientMain.sendCommandForModel(BaseCommand.PUT.toString(), t1, session2);
        assertEquals(OperationStatus.BLOCKED, message.getResult().getOperationStatus());

        message = clientMain.sendCommandForModel(BaseCommand.PUT.toString(), t2, session2);
        assertEquals(OperationStatus.BLOCKED, message.getResult().getOperationStatus());

        message = clientMain.sendCommandForModel(BaseCommand.PUT.toString(), t3, session2);
        assertEquals(OperationStatus.BLOCKED, message.getResult().getOperationStatus());

        message = clientMain.sendCommandForId(BaseCommand.GET.toString(), new MainTestModelId(1), session2);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(0, message.getBody().getResponse().getResultList().size());

        message = clientMain.sendCommandForId(BaseCommand.GET.toString(), new MainTestModelId(2), session2);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(0, message.getBody().getResponse().getResultList().size());

        message = clientMain.sendCommandForId(BaseCommand.GET.toString(), new MainTestModelId(3), session2);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(0, message.getBody().getResponse().getResultList().size());

        message = clientMain.sendCommandForId(BaseCommand.GET.toString(), ids, session2);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(0, message.getBody().getResponse().getResultList().size());

        message = clientMain.sendCommandForId(BaseCommand.GET.toString(), new MainTestModelId(1), null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(0, message.getBody().getResponse().getResultList().size());

        message = clientMain.sendCommandForId(BaseCommand.GET.toString(), new MainTestModelId(2), null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(0, message.getBody().getResponse().getResultList().size());

        message = clientMain.sendCommandForId(BaseCommand.GET.toString(), new MainTestModelId(3), null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(0, message.getBody().getResponse().getResultList().size());

        message = clientMain.sendCommandForId(BaseCommand.GET.toString(), ids, null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(0, message.getBody().getResponse().getResultList().size());

        message = clientMain.sendCommandForId(BaseCommand.GET.toString(), new MainTestModelId(1), session1);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(1, message.getBody().getResponse().getResultList().size());

        message = clientMain.sendCommandForId(BaseCommand.GET.toString(), new MainTestModelId(2), session1);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(1, message.getBody().getResponse().getResultList().size());

        message = clientMain.sendCommandForId(BaseCommand.GET.toString(), new MainTestModelId(3), session1);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(1, message.getBody().getResponse().getResultList().size());

        message = clientMain.sendCommandForId(BaseCommand.GET.toString(), ids, session1);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(3, message.getBody().getResponse().getResultList().size());

        message = clientMain.sendCommand(BaseCommand.ROLLBACK.toString(), session1);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        message = clientMain.sendCommandForId(BaseCommand.GET.toString(), new MainTestModelId(1), session1);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(0, message.getBody().getResponse().getResultList().size());

        message = clientMain.sendCommandForId(BaseCommand.GET.toString(), new MainTestModelId(2), session1);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(0, message.getBody().getResponse().getResultList().size());

        message = clientMain.sendCommandForId(BaseCommand.GET.toString(), new MainTestModelId(3), session1);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(0, message.getBody().getResponse().getResultList().size());

        message = clientMain.sendCommandForId(BaseCommand.GET.toString(), ids, session1);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(0, message.getBody().getResponse().getResultList().size());

        message = clientMain.sendCommandForModel(BaseCommand.PUT.toString(), models, session1);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        message = clientMain.sendCommand(BaseCommand.COMMIT.toString(), session1);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        message = clientMain.sendCommandForId(BaseCommand.GET.toString(), new MainTestModelId(1), session2);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(1, message.getBody().getResponse().getResultList().size());

        message = clientMain.sendCommandForId(BaseCommand.GET.toString(), new MainTestModelId(2), session2);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(1, message.getBody().getResponse().getResultList().size());

        message = clientMain.sendCommandForId(BaseCommand.GET.toString(), new MainTestModelId(3), session2);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(1, message.getBody().getResponse().getResultList().size());

        message = clientMain.sendCommandForId(BaseCommand.GET.toString(), ids, session2);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(3, message.getBody().getResponse().getResultList().size());

        t1.setName("ppppp1");
        t2.setName("ppppp2");
        t3.setName("ppppp3");
        message = clientMain.sendCommandForModel(BaseCommand.PUT.toString(), models, session2);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        message = clientMain.sendCommandForId(BaseCommand.GET.toString(), new MainTestModelId(1), session1);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(1, message.getBody().getResponse().getResultList().size());
        assertEquals("test1", ((MainTestModel) message.getBody().getResponse().getResultList().iterator().next()).getName());

        message = clientMain.sendCommandForId(BaseCommand.GET.toString(), new MainTestModelId(2), session1);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(1, message.getBody().getResponse().getResultList().size());
        assertEquals("test2", ((MainTestModel) message.getBody().getResponse().getResultList().iterator().next()).getName());

        message = clientMain.sendCommandForId(BaseCommand.GET.toString(), new MainTestModelId(3), session1);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(1, message.getBody().getResponse().getResultList().size());
        assertEquals("test3", ((MainTestModel) message.getBody().getResponse().getResultList().iterator().next()).getName());

        message = clientMain.sendCommandForId(BaseCommand.GET.toString(), ids, session1);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(3, message.getBody().getResponse().getResultList().size());
        final Set<Model> listModels = new HashSet<>();
        for (final Model model : message.getBody().getResponse().getResultList()) {
            if (((MainTestModel) model).getName().equals("test1") || ((MainTestModel) model).getName().equals("test2") || ((MainTestModel) model).getName().equals("test3")) {
                listModels.add(model);
            }
        }
        assertEquals(3, listModels.size());

        message = clientMain.sendCommandForId(BaseCommand.GET.toString(), ids, session2);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(3, message.getBody().getResponse().getResultList().size());
        listModels.clear();
        for (final Model model : message.getBody().getResponse().getResultList()) {
            if (((MainTestModel) model).getName().equals("test1") || ((MainTestModel) model).getName().equals("test2") || ((MainTestModel) model).getName().equals("test3")) {
                listModels.add(model);
            }
        }
        assertEquals(0, listModels.size());
        listModels.clear();
        for (final Model model : message.getBody().getResponse().getResultList()) {
            if (((MainTestModel) model).getName().equals("ppppp1") || ((MainTestModel) model).getName().equals("ppppp2") || ((MainTestModel) model).getName().equals("ppppp3")) {
                listModels.add(model);
            }
        }
        assertEquals(3, listModels.size());

        message = clientMain.sendCommand(BaseCommand.COMMIT.toString(), session2);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        message = clientMain.sendCommandForId(BaseCommand.GET.toString(), ids, session1);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(3, message.getBody().getResponse().getResultList().size());
        listModels.clear();
        for (final Model model : message.getBody().getResponse().getResultList()) {
            if (((MainTestModel) model).getName().equals("test1") || ((MainTestModel) model).getName().equals("test2") || ((MainTestModel) model).getName().equals("test3")) {
                listModels.add(model);
            }
        }
        assertEquals(0, listModels.size());
        listModels.clear();
        for (final Model model : message.getBody().getResponse().getResultList()) {
            if (((MainTestModel) model).getName().equals("ppppp1") || ((MainTestModel) model).getName().equals("ppppp2") || ((MainTestModel) model).getName().equals("ppppp3")) {
                listModels.add(model);
            }
        }
        assertEquals(3, listModels.size());

        message = clientMain.sendCommandForId(BaseCommand.GET.toString(), ids, null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(3, message.getBody().getResponse().getResultList().size());
        listModels.clear();
        for (final Model model : message.getBody().getResponse().getResultList()) {
            if (((MainTestModel) model).getName().equals("test1") || ((MainTestModel) model).getName().equals("test2") || ((MainTestModel) model).getName().equals("test3")) {
                listModels.add(model);
            }
        }
        assertEquals(0, listModels.size());
        listModels.clear();
        for (final Model model : message.getBody().getResponse().getResultList()) {
            if (((MainTestModel) model).getName().equals("ppppp1") || ((MainTestModel) model).getName().equals("ppppp2") || ((MainTestModel) model).getName().equals("ppppp3")) {
                listModels.add(model);
            }
        }
        assertEquals(3, listModels.size());

    }

    private void cleanCache() throws Exception {
        final List<Identifier> ids = new ArrayList<>();
        ids.add(new MainTestModelId(1));
        ids.add(new MainTestModelId(2));
        ids.add(new MainTestModelId(3));
        ids.add(new MainTestModelId(4));
        ids.add(new MainTestModelId(5));

        final IdentifiersResult iResult = clientMain.removeModels(ids, null);
        assertEquals(OperationStatus.OK, iResult.getOperationStatus());

        FullMessage message;
        final Collection<Model> models = dataInvoker.getModel(ids);
        assertEquals(0, models.size());
        for (int i = 0; i < 6; i++) {
            message = clientMain.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE.toString(), new MainTestModelId(i + 1), null);
            assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
            assertTrue(message.getBody().getResponse().getIdentifiers().isEmpty());

            message = client2.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE.toString(), new MainTestModelId(i + 1), null);
            assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
            assertTrue(message.getBody().getResponse().getIdentifiers().isEmpty());

            message = client3.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE.toString(), new MainTestModelId(i + 1), null);
            assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
            assertTrue(message.getBody().getResponse().getIdentifiers().isEmpty());

        }

    }

    private int getCountInCache(final Identifier id, final String session) throws Exception {

        int calculator = 0;
        FullMessage message = clientMain.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE.toString(), id, session);
        assertTrue(message.getBody().getResponse().getIdentifiers().size() < 2);
        System.out.println("cache1:"+message.getBody().getResponse().getIdentifiers().size());
        calculator += message.getBody().getResponse().getIdentifiers().size();
        message = client2.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE.toString(), id, session);
        System.out.println("cache2:"+message.getBody().getResponse().getIdentifiers().size());
        assertTrue(message.getBody().getResponse().getIdentifiers().size() < 2);
        calculator += message.getBody().getResponse().getIdentifiers().size();
        message = client3.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE.toString(), id, session);
        System.out.println("cache3:"+message.getBody().getResponse().getIdentifiers().size());
        assertTrue(message.getBody().getResponse().getIdentifiers().size() < 2);
        calculator += message.getBody().getResponse().getIdentifiers().size();
        return calculator;
    }

    @BeforeClass
    public static void setUp() throws Exception {

        final List<StoreModelImpl> models1 = new ArrayList<>();
        models1.add(getStoreData("MainTestModel", "com.nomad.cache.test.model.MainTestModel"));
        final List<StoreModelImpl> models2 = new ArrayList<>();
        models2.add(getStoreData("Child", "com.nomad.cache.test.model.Child"));
        models2.add(getStoreData("MainTestModel", "com.nomad.cache.test.model.MainTestModel"));

        final DataSourceModelImpl dataSource = new DataSourceModelImpl();
        dataSource.setName("a");
        dataSource.setThreads(10);
        dataSource.setTimeOut(10000);
        dataSource.setClazz("com.nomad.cache.test.userdataaccess.BaseDataInvoker");
        dataSource.addProperty("user", "sa");
        dataSource.addProperty("password", "");
        dataSource.addProperty("url", "jdbc:hsqldb:hsql://localhost/test");
        dataSource.addProperty("driver", "org.hsqldb.jdbcDriver");

        final ServerModelImpl serverModel = new ServerModelImpl();

        serverModel.getStoreModels().addAll(models1);
        serverModel.getListeners().add(getListenerModel(host, port1, 20));
        serverModel.setManagementServerModel(getManagementServerModel(2142, host, 2, 2000));
        serverModel.setServerName("main");
        serverModel.getProperties().setProperty("SessionStorePath", "/opt/t1/sessionstore/1");
        final SessionServerModelImp sessionServer = getSessionServerModel(host, 5888, 15);
        sessionServer.setSessionTimeLive(1000000);
        serverModel.setSessionServerModel(sessionServer);
        serverModel.getSessionClientModels().add(getSessionClientModel(host, 5888, 5));
        serverModel.getSaveClientModels().add(getSaveClientModel(host, 5224, 5));
        serverModel.setCalculateStatistic(false);

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
        final ListenerModelImpl listener2 = getListenerModel(host, port2, 6);
        serverModel2.getListeners().add(listener2);
        serverModel2.setManagementServerModel(getManagementServerModel(2242, host, 2, 2000));
        serverModel2.setServerName("first");

        serverModel2.addDataSources(dataSource);

        serverModel2.getCommandPlugins().add(plugin);
        serverModel2.setTrustSessions(true);
        serverModel2.getSaveServerModels().add(getSaveServerModel(host, 5224, 15));
        serverModel2.getSaveClientModels().add(getSaveClientModel(host, 5224, 5));
        serverModel2.setCalculateStatistic(false);

        final ConnectModelImpl connectModel = getConnectModel(serverModel2, serverModel, 5, listener2);

        serverModel2.getClients().add(connectModel);

        launcher2 = new ServerLauncher(serverModel2);

        launcher2.start();

        final ServerModelImpl serverModel3 = new ServerModelImpl();

        serverModel3.getStoreModels().addAll(models2);
        final ListenerModelImpl listener = getListenerModel(host, port3, 6);
        serverModel3.getListeners().add(listener);
        serverModel3.setManagementServerModel(getManagementServerModel(2342, host, 2, 2000));
        serverModel3.setServerName("second");

        serverModel3.addDataSources(dataSource);

        serverModel3.getCommandPlugins().add(plugin);
        serverModel3.setTrustSessions(true);
        serverModel3.getSaveClientModels().add(getSaveClientModel(host, 5224, 5));
        serverModel3.setCalculateStatistic(false);

        final ConnectModelImpl connectModel3 = getConnectModel(serverModel3, serverModel, 5, listener);

        serverModel3.getClients().add(connectModel3);// client

        launcher3 = new ServerLauncher(serverModel3);

        launcher3.start();

        clientMain = new SimpleCacheClient(host, port1);
        client2 = new SimpleCacheClient(host, port2);
        client3 = new SimpleCacheClient(host, port3);

    }

    @AfterClass
    public static void tearDown() throws Exception {
        if(dataInvoker!=null){
            dataInvoker.close();
        }
        close(launcher);
        close(launcher2);
        close(launcher3);
        close(clientMain);
        close(client2);
        close(client3);

    }

}
