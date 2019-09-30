package com.nomad.cache.client;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.nomad.StartFormXml;
import com.nomad.cache.test.model.TestCriteria;
import com.nomad.client.ModelsResult;
import com.nomad.client.SimpleCacheClient;
import com.nomad.exception.SystemException;
import com.nomad.message.FullMessage;
import com.nomad.message.OperationStatus;
import com.nomad.model.Criteria.Condition;
import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.model.ServiceCommand;
import com.nomad.model.criteria.AbstractCriteria;
import com.nomad.pm.PmDataInvokerFactory;
import com.nomad.server.PmDataInvoker;

public class SwitchOnSwitchOffTest extends CommonTest {

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
    private static StartFormXml starter2;

    @Test
    public void test1() throws Exception {
        dataInvoker.eraseModel(new TestCriteria());
        final List<Model> models = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            models.add(getNewTestModel(i, "test" + i));
        }
        ModelsResult modelsResult = cacheTranslator.putModels(models, null);
        assertEquals(OperationStatus.OK, modelsResult.getOperationStatus());
        for (int i = 300; i < 500; i++) {
            modelsResult = cacheTranslator.putModel(getNewTestModel(i, "test" + i), null);
            assertEquals(OperationStatus.OK, modelsResult.getOperationStatus());
        }

        final String[] files = { "configuration/cache3.xml" };
        starter2 = new StartFormXml();
        cache3 = new SimpleCacheClient(host, port4);

        starter2.startServers(files);
        Thread.sleep(20000);
        models.clear();
        for (int i = 500; i <= 800; i++) {
            models.add(getNewTestModel(i, "test" + i));
        }
        modelsResult = cacheTranslator.putModels(models, null);
        assertEquals(OperationStatus.OK, modelsResult.getOperationStatus());

        final TestCriteria criteria = new TestCriteria();
        criteria.addCriterion(TestCriteria.ID, Condition.GE, 500);
        criteria.addCriterion(TestCriteria.ID, Condition.LE, 800);

        int k = 0;
        k += getCountInCache(cache1, criteria);
        k += getCountInCache(cache2, criteria);
        k += getCountInCache(cache3, criteria);
        assertEquals(602, k);

    }

    private int getCountInCache(final SimpleCacheClient cache, final AbstractCriteria<? extends Model> criteria) throws Exception {
        FullMessage message = cache.sendCommand(ServiceCommand.GET_IDS_BY_CRITERIA, criteria);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(301, message.getBody().getResponse().getIdentifiers().size());
        final Collection< Identifier> ids = message.getBody().getResponse().getIdentifiers();
        message = cache.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE, ids, null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        return message.getBody().getResponse().getIdentifiers().size();

    }

    @BeforeClass
    public static void init() throws Exception {
        final String[] files = { "configuration/translator.xml", "configuration/cacheManager.xml", "configuration/cache1.xml", "configuration/cache2.xml" };
        starter = new StartFormXml();
        starter.startServers(files);

        cacheTranslator = new SimpleCacheClient(host, portTranslator);
        cacheManager = new SimpleCacheClient(host, port1);
        cache1 = new SimpleCacheClient(host, port2);
        cache2 = new SimpleCacheClient(host, port3);
        dataInvoker = PmDataInvokerFactory.getDataInvoker("b", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:hsql://localhost/test", "sa", "", null, 1);

    }

    @AfterClass
    public static void stop() throws SystemException {
        dataInvoker.eraseModel(new TestCriteria());
        dataInvoker.close();

        if (cache1 != null) {
            cache1.close();
        }
        if (cache2 != null) {
            cache2.close();
        }
        if (cache3 != null) {
            cache3.close();
        }
        if (cacheTranslator != null) {
            cacheTranslator.close();
        }
        if (cacheManager != null) {
            cacheManager.close();
        }
        if (starter != null) {
            starter.stop();
        }
        if (starter2 != null) {
            starter2.stop();
        }
    }
}
