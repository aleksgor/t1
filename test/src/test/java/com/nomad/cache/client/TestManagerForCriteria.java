package com.nomad.cache.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Collection;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.nomad.StartFormXml;
import com.nomad.cache.test.model.MainTestModel;
import com.nomad.cache.test.model.MainTestModelId;
import com.nomad.cache.test.model.TestCriteria;
import com.nomad.client.CriteriaResult;
import com.nomad.client.SimpleCacheClient;
import com.nomad.message.FullMessage;
import com.nomad.message.OperationStatus;
import com.nomad.model.BaseCommand;
import com.nomad.model.Identifier;
import com.nomad.pm.PmDataInvokerFactory;
import com.nomad.server.PmDataInvoker;
import com.nomad.utility.ModelUtil;

public class TestManagerForCriteria extends CommonTest {

    private static int portTranslator = 2032;
    private static int port1 = 2132;
    private static int port2 = 2232;
    private static int port3 = 2332;
    private static int port4 = 2432;
    private static SimpleCacheClient cacheTranslator;
    private static SimpleCacheClient cacheManager;
    private static SimpleCacheClient cache1;
    private static SimpleCacheClient cache2;
    private static SimpleCacheClient cache3;
    private static PmDataInvoker dataInvoker;
    private static StartFormXml starter;

    @org.junit.Test
    public void test1() throws Exception {
        clearData();
        final FullMessage result = cacheTranslator.sendCommandForId(BaseCommand.GET, new MainTestModelId(1));
        assertEquals(OperationStatus.OK, result.getResult().getOperationStatus());

    }

    /*
     * test session isolation
     */
    @org.junit.Test
    public void testRelations() {
        try {
            clearData();

            TestCriteria criteria = new TestCriteria();

            CriteriaResult<MainTestModel> criteriaResult = cacheTranslator.getModels(criteria, null);

            assertEquals(OperationStatus.OK, criteriaResult.getOperationStatus());
            Collection<MainTestModel> result = criteriaResult.getResult().getResultList();
            assertEquals(5, result.size());
            for (final MainTestModel test : result) {
                assertNull(test.getChild());
            }

            criteria = new TestCriteria();
            criteria.addRelationLoad("childRelation");
            criteria.setPageSize(10);
            criteriaResult = cacheTranslator.getModels(criteria, null);
            assertEquals(OperationStatus.OK, criteriaResult.getOperationStatus());

            result = criteriaResult.getResult().getResultList();
            assertEquals(5, result.size());
            for (final MainTestModel test : result) {
                assertNotNull(test.getChild());

            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @BeforeClass
    public static void start() throws Exception {
        commonSetup();
        starter = null;

        final String[] files = { "configuration/translator.xml", "configuration/cacheManager.xml", "configuration/cache1.xml", "configuration/cache2.xml",
        "configuration/cache3.xml" };
        starter = new StartFormXml();
        starter.startServers(files);

        cacheTranslator = new SimpleCacheClient(host, portTranslator);
        cacheManager = new SimpleCacheClient(host, port1);
        cache1 = new SimpleCacheClient(host, port2);
        cache2 = new SimpleCacheClient(host, port3);
        cache3 = new SimpleCacheClient(host, port4);
        dataInvoker = PmDataInvokerFactory.getDataInvoker("b", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:hsql://localhost/test", "sa", "", null, 1);

    }

    private void clearData() throws Exception {

        final TestCriteria criteria = new TestCriteria();

        final FullMessage result = cacheTranslator.sendCommand(BaseCommand.GET_LIST_ID_BY_CRITERIA, criteria);
        final List<Identifier> ids = (List<Identifier>) ModelUtil.getIdentifiers(result.getBody().getResponse().getResultList());

        cacheTranslator.sendCommandForId(BaseCommand.DELETE, ids, null);

        cacheTranslator.sendCommandForModel(BaseCommand.PUT, getNewTestModel(1, "test1", 1));
        cacheTranslator.sendCommandForModel(BaseCommand.PUT, getNewTestModel(2, "test2", 1));
        cacheTranslator.sendCommandForModel(BaseCommand.PUT, getNewTestModel(3, "test3", 2));
        cacheTranslator.sendCommandForModel(BaseCommand.PUT, getNewTestModel(4, "test4", 2));
        cacheTranslator.sendCommandForModel(BaseCommand.PUT, getNewTestModel(5, "test5", 2));

        cacheTranslator.sendCommandForModel(BaseCommand.PUT, getChildModel(1, "child1"));
        cacheTranslator.sendCommandForModel(BaseCommand.PUT, getChildModel(2, "child2"));

    }

    @AfterClass
    public static void stop() {
        cacheTranslator.close();
        cacheManager.close();
        cache1.close();
        cache2.close();
        cache3.close();
        starter.stop();
        dataInvoker.close();

    }
}
