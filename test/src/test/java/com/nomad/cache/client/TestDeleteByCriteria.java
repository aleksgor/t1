package com.nomad.cache.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.nomad.StartFormXml;
import com.nomad.cache.test.model.MainTestModel;
import com.nomad.cache.test.model.MainTestModelId;
import com.nomad.cache.test.model.TestCriteria;
import com.nomad.client.CountResult;
import com.nomad.client.CriteriaResult;
import com.nomad.client.ModelsResult;
import com.nomad.client.SessionResult;
import com.nomad.client.SimpleCacheClient;
import com.nomad.client.SingleCacheClient;
import com.nomad.client.VoidResult;
import com.nomad.message.OperationStatus;
import com.nomad.model.Criteria.Condition;
import com.nomad.model.Model;
import com.nomad.pm.PmDataInvokerFactory;
import com.nomad.server.PmDataInvoker;

public class TestDeleteByCriteria extends CommonTest {

    private static int portTranslator = 2032;
    private static int port1 = 2132;
    private static int port2 = 2232;
    private static int port3 = 2332;
    private static int port4 = 2432;
    private static SingleCacheClient cacheTranslator;
    private static SingleCacheClient cacheManager;
    private static SimpleCacheClient cache1;
    private static SimpleCacheClient cache2;
    private static SimpleCacheClient cache3;
    private static PmDataInvoker dataInvoker;
    private static StartFormXml starter;

    @org.junit.Test
    public void test1() throws Exception {
        final MainTestModel test1 = getNewTestModel(1, "a,dbvlashdvcasbdvc.as");
        final MainTestModel test2 = getNewTestModel(2, "a,dbvlashdvcasbdvc.asafcavcadc");
        ModelsResult answer = cacheTranslator.putModel(test1, null);
        assertEquals(OperationStatus.OK, answer.getOperationStatus());
        answer = cacheTranslator.putModel(test2, null);
        ModelsResult modelsResult = cacheTranslator.getModel(test1.getIdentifier(), null);
        assertEquals(modelsResult.getModels().size(), 1);

        modelsResult = cacheTranslator.getModel(test2.getIdentifier(), null);
        assertEquals(modelsResult.getModels().size(), 1);

        final TestCriteria criteria = new TestCriteria();
        criteria.addCriterion(TestCriteria.ID, Condition.EQ, 1);
        CountResult count = cacheTranslator.removeModel(criteria, null);
        assertEquals(1, count.getCount());

        modelsResult = cacheTranslator.getModel(test1.getIdentifier(), null);
        assertEquals(0, modelsResult.getModels().size());

        modelsResult = cacheTranslator.getModel(test2.getIdentifier(), null);
        assertEquals(1, modelsResult.getModels().size());

    }

    @org.junit.Test
    public void testManyData() throws Exception {
        final int count = 105;
        final List<Model> models = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            models.add(getNewTestModel(i, "a,dbvlashdvcasbdvc.as" + 1));
        }

        final ModelsResult answer = cacheTranslator.putModels(models, null);
        assertEquals(OperationStatus.OK, answer.getOperationStatus());

        final TestCriteria criteria = new TestCriteria();
        criteria.addCriterion(TestCriteria.ID, Condition.GT, 2);
        criteria.addCriterion(TestCriteria.ID, Condition.LT, 103);

        CriteriaResult<MainTestModel> criteriaResult = cacheTranslator.getModels(criteria, null);
        assertEquals(100, criteriaResult.getResult().getResultList().size());

        final CountResult countResult = cacheTranslator.removeModel(criteria, null);
        assertEquals(100, countResult.getCount());

        criteriaResult = cacheTranslator.getModels(criteria, null);
        assertEquals(0, criteriaResult.getResult().getResultList().size());

    }

    @org.junit.Test
    public void testBlock() throws Exception {
        final int count = 105;
        final List<Model> models = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            models.add(getNewTestModel(i, "a,dbvlashdvcasbdvc.as" + 1));
        }
        final ModelsResult answer = cacheTranslator.putModels(models, null);
        assertEquals(OperationStatus.OK, answer.getOperationStatus());

        final SessionResult session = cacheTranslator.startSession();
        final String sessionId = session.getSessionId();
        assertNotNull(sessionId);

        final ModelsResult modelResult = cacheTranslator.getModel(new MainTestModelId(10), null);
        assertEquals(OperationStatus.OK, modelResult.getOperationStatus());
        assertEquals(1, modelResult.getModels().size());
        final MainTestModel testModel = (MainTestModel) modelResult.getModels().iterator().next();

        testModel.setName("blocked");
        ModelsResult result = cacheTranslator.putModel(testModel, null);
        assertEquals(1, result.getModels().size());
        result = cacheTranslator.putModel(testModel, sessionId);

        final TestCriteria criteria = new TestCriteria();
        criteria.addCriterion(TestCriteria.ID, Condition.GT, 2);
        criteria.addCriterion(TestCriteria.ID, Condition.LT, 103);

        CriteriaResult<MainTestModel> criteriaResult = cacheTranslator.getModels(criteria, null);
        assertEquals(100, criteriaResult.getResult().getResultList().size());
        CountResult countResult = cacheTranslator.removeModel(criteria, null);
        assertEquals(OperationStatus.BLOCKED, countResult.getOperationStatus());
        assertEquals(0, countResult.getCount());
        final VoidResult voidResult = cacheTranslator.commit(sessionId);
        assertEquals(OperationStatus.OK, voidResult.getOperationStatus());

        countResult = cacheTranslator.removeModel(criteria, null);
        assertEquals(OperationStatus.OK, countResult.getOperationStatus());
        assertEquals(100, countResult.getCount());

        criteriaResult = cacheTranslator.getModels(criteria, null);
        assertEquals(0, criteriaResult.getResult().getResultList().size());

    }

    @BeforeClass
    public static void setUp() throws Exception {

        commonSetup();
        final String[] files = { "configuration/translator.xml", "configuration/cacheManager.xml", "configuration/cache1.xml", "configuration/cache2.xml",
        "configuration/cache3.xml" };

        starter = new StartFormXml();
        starter.startServers(files);

        cacheTranslator = new SingleCacheClient(host, portTranslator);
        cacheManager = new SingleCacheClient(host, port1);
        cache1 = new SimpleCacheClient(host, port2);
        cache2 = new SimpleCacheClient(host, port3);
        cache3 = new SimpleCacheClient(host, port4);
        dataInvoker = PmDataInvokerFactory.getDataInvoker("b", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:hsql://localhost/test", "sa", "", null, 1);

    }

    @AfterClass
    public static void tearDown() throws Exception {
        dataInvoker.close();
        close(cacheTranslator);
        close(cacheManager);
        close(cache1);
        close(cache2);
        close(cache3);

        if (starter != null) {
            starter.stop();
        }
    }

}
