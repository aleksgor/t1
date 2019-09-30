package com.nomad.cache.hsqldb;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.nomad.StartFormXml;
import com.nomad.cache.client.CommonTest;
import com.nomad.cache.test.model.MainTestModel;
import com.nomad.cache.test.model.TestCriteria;
import com.nomad.client.SimpleCacheClient;
import com.nomad.message.FullMessage;
import com.nomad.message.OperationStatus;
import com.nomad.model.BaseCommand;
import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.model.ServiceCommand;
import com.nomad.model.criteria.StatisticResult;
import com.nomad.pm.PmDataInvokerFactory;
import com.nomad.server.PmDataInvoker;

public class TestGet extends CommonTest {

    private static String host = "localhost";
    private static int port1 = 2122;
    private static int port2 = 2222;
    private static int port3 = 2322;
    private static int port4 = 2422;
    private static SimpleCacheClient cacheManager;
    private static SimpleCacheClient cache1;
    private static SimpleCacheClient cache2;
    private static SimpleCacheClient cache3;
    private static StartFormXml starter;
    private List<Identifier> listIds;
    private static PmDataInvoker dataInvoker;

    @BeforeClass
    public static void start() throws Exception {
        commonSetup();
        final String[] files = { "configuration/hsqldb/cacheManager.xml", "configuration/hsqldb/cache1.xml", "configuration/hsqldb/cache2.xml", "configuration/hsqldb/cache3.xml" };
        starter = new StartFormXml();
        starter.startServers(files);

        cacheManager = new SimpleCacheClient(host, port1);
        cache1 = new SimpleCacheClient(host, port2);
        cache2 = new SimpleCacheClient(host, port3);
        cache3 = new SimpleCacheClient(host, port4);
        dataInvoker = PmDataInvokerFactory.getDataInvoker("b", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:hsql://localhost/test", "SA", "", null, 1);

    }

    @Test
    public void test1() throws Exception {

        final com.nomad.cache.test.model.MainTestModel t1 = getNewTestModel(1, "test1");
        final com.nomad.cache.test.model.MainTestModel t2 = getNewTestModel(2, "test2");
        final com.nomad.cache.test.model.MainTestModel t3 = getNewTestModel(3, "test3");
        final com.nomad.cache.test.model.MainTestModel t4 = getNewTestModel(4, "test4");
        final com.nomad.cache.test.model.MainTestModel t5 = getNewTestModel(5, "test5");

        listIds = Arrays.asList(new Identifier[] { t1.getIdentifier(), t2.getIdentifier(), t3.getIdentifier(), t4.getIdentifier(), t5.getIdentifier() });
        FullMessage message = null;
        message = cacheManager.sendCommandForId(BaseCommand.DELETE.name(), listIds, null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        message = cache1.sendCommandForId(ServiceCommand.GET_FROM_CACHE.toString(), listIds, null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(0, message.getBody().getResponse().getResultList().size());

        message = cache2.sendCommandForId(ServiceCommand.GET_FROM_CACHE.toString(), listIds, null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(0, message.getBody().getResponse().getResultList().size());

        message = cache3.sendCommandForId(ServiceCommand.GET_FROM_CACHE.toString(), listIds, null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(0, message.getBody().getResponse().getResultList().size());
        message = cacheManager.sendCommandForModel(BaseCommand.PUT.name(), Arrays.asList(new Model[] { t1, t2, t3, t4, t5 }), null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        assertEquals(10, getCount());

        message = cacheManager
                .sendCommandForId(BaseCommand.DELETE.toString(), Arrays.asList(new Identifier[] { t3.getIdentifier(), t4.getIdentifier(), t5.getIdentifier() }),
                        null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        dataInvoker.addModel(Arrays.asList(t3,t4,t5));


        assertEquals(4, getCount());

        message = cacheManager.sendCommandForId(BaseCommand.GET, t3.getIdentifier(), null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(1, message.getBody().getResponse().getResultList().size());
        assertEquals(6, getCount());

        message = cacheManager.sendCommandForId(BaseCommand.GET, Arrays.asList(new Identifier[] { t3.getIdentifier(), t4.getIdentifier() }), null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(2, message.getBody().getResponse().getResultList().size());

        assertEquals(8, getCount());

    }

    private int getCount() throws Exception {
        int i = 0;

        FullMessage message = cache1.sendCommandForId(ServiceCommand.GET_FROM_CACHE.toString(), listIds, null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        i += message.getBody().getResponse().getResultList().size();

        message = cache2.sendCommandForId(ServiceCommand.GET_FROM_CACHE.toString(), listIds, null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        i += message.getBody().getResponse().getResultList().size();

        message = cache3.sendCommandForId(ServiceCommand.GET_FROM_CACHE.toString(), listIds, null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        i += message.getBody().getResponse().getResultList().size();
        return i;
    }

    @Test
    public void testInvoker() throws Exception {
        TestCriteria criteria = new TestCriteria();

        final MainTestModel t1 = getNewTestModel(1, "test1");
        final MainTestModel t2 = getNewTestModel(2, "test2");
        final MainTestModel t3 = getNewTestModel(3, "test3");
        final MainTestModel t4 = getNewTestModel(4, "test4");
        final MainTestModel t5 = getNewTestModel(5, "test5");

        dataInvoker.eraseModel(criteria);
        dataInvoker.addModel(Arrays.asList(t1,t2,t3,t4,t5));

        StatisticResult<MainTestModel> result = dataInvoker.getList(criteria);
        assertEquals("count all row", 5, result.getCountAllRow());

        assertEquals("list size", 5, result.getResultList().size());


    }

    @AfterClass
    public static void stop() {
        if (dataInvoker != null) {
            dataInvoker.close();
        }

        if (cacheManager != null) {
            cacheManager.close();
        }
        if (cache1 != null) {
            cache1.close();
        }
        if (cache2 != null) {
            cache2.close();
        }
        if (cache3 != null) {
            cache3.close();
        }
        if (starter != null) {
            starter.stop();
        }

    }

}
