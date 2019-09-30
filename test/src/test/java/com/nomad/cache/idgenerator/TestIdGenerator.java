package com.nomad.cache.idgenerator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.nomad.StartFormXml;
import com.nomad.cache.client.CommonTest;
import com.nomad.cache.test.model.NoIdTestCriteria;
import com.nomad.cache.test.model.NoIdTestModel;
import com.nomad.client.CountResult;
import com.nomad.client.ModelsResult;
import com.nomad.client.SimpleCacheClient;
import com.nomad.message.OperationStatus;
import com.nomad.model.Model;
import com.nomad.pm.PmDataInvokerFactory;
import com.nomad.server.PmDataInvoker;

public class TestIdGenerator extends CommonTest {

    private static int port0 = 2022;
    private static int port1 = 2122;
    private static int port2 = 2222;
    private static int port3 = 2322;
    private static int port4 = 2422;
    private static SimpleCacheClient cacheManager;
    private static SimpleCacheClient cache1;
    private static SimpleCacheClient cache2;
    private static SimpleCacheClient cache3;
    private static SimpleCacheClient cache4;
    private static StartFormXml starter;
    private static PmDataInvoker pDataInvoker;
    private static PmDataInvoker sDataInvoker;

    @BeforeClass
    public static void start() throws Exception {
        try {
            host = "localhost";
            commonSetup();
            final String[] files = { "configuration/idgenerator/cacheManager.xml", "configuration/idgenerator/cache1.xml", "configuration/idgenerator/cache2.xml",
                    "configuration/idgenerator/cache3.xml", "configuration/idgenerator/cache4.xml" };
            starter = new StartFormXml();

            starter.startServers(files);

            cacheManager = new SimpleCacheClient(host, port0);
            cache1 = new SimpleCacheClient(host, port1);
            cache2 = new SimpleCacheClient(host, port2);
            cache3 = new SimpleCacheClient(host, port3);
            cache4 = new SimpleCacheClient(host, port4);

            pDataInvoker = PmDataInvokerFactory.getDataInvoker("postgres1", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:hsql://localhost/test", "sa", "", "pm.cfg.xml", 1);

            sDataInvoker = PmDataInvokerFactory.getDataInvoker("msql1", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:hsql://localhost/test", "sa", "", "pm2.cfg.xml", 1);

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test1() throws Exception {

        NoIdTestCriteria criteria = new NoIdTestCriteria();
        pDataInvoker.eraseModel(criteria);
        sDataInvoker.eraseModel(criteria);

        int length = 50;

        NoIdTestModel[] models = new NoIdTestModel[length];
        for (int i = 0; i < length; i++) {
            models[i] = getNewNoIdTestModel("test" + i);
        }

        CountResult cResult = cacheManager.removeModel(criteria, null);

        assertEquals(" delete error", OperationStatus.OK, cResult.getOperationStatus());

        ModelsResult mResult = cacheManager.putModels(Arrays.<Model> asList(models), null);
        assertEquals(" put error", OperationStatus.OK, mResult.getOperationStatus());
        mResult.getModels();
        for (Model model : mResult.getModels()) {
            assertNotNull(model);
            assertNotNull(model.getIdentifier());
        }

        criteria = new NoIdTestCriteria();
        assertEquals(25, pDataInvoker.getList(criteria).getResultList().size());
        assertEquals(25, sDataInvoker.getList(criteria).getResultList().size());

    }


    @AfterClass
    public static void stop() {
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
        if (cache4 != null) {
            cache4.close();
        }

        if (starter != null) {
            starter.stop();
        }
        if (pDataInvoker != null) {
            pDataInvoker.close();
        }
        if (sDataInvoker != null) {
            sDataInvoker.close();
        }

    }

}
