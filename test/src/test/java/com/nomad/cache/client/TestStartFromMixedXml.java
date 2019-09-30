package com.nomad.cache.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.nomad.StartFormXml;
import com.nomad.cache.test.model.MainTestModel;
import com.nomad.cache.test.model.MainTestModelId;
import com.nomad.client.IdentifiersResult;
import com.nomad.client.SimpleCacheClient;
import com.nomad.exception.ModelNotExistException;
import com.nomad.message.FullMessage;
import com.nomad.message.OperationStatus;
import com.nomad.model.BaseCommand;
import com.nomad.model.Identifier;
import com.nomad.model.ServiceCommand;
import com.nomad.pm.PmDataInvokerFactory;
import com.nomad.server.PmDataInvoker;

public class TestStartFromMixedXml extends CommonTest {

    private static String host = "localhost";
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

    @Test
    public void test1() throws Exception {
        StartFormXml starter = null;
        try {
            commonSetup();
            final String[] files = { "configuration/hybrid/cache1.xml", "configuration/hybrid/cache2.xml", "configuration/hybrid/cache3.xml",
                    "configuration/hybrid/translator.xml", "configuration/hybrid/cacheManager.xml" };
            starter = new StartFormXml();
            starter.startServers(files);

            cacheTranslator = new SimpleCacheClient(host, portTranslator);
            cacheManager = new SimpleCacheClient(host, port1);
            cache1 = new SimpleCacheClient(host, port2);
            cache2 = new SimpleCacheClient(host, port3);
            cache3 = new SimpleCacheClient(host, port4);
            dataInvoker = PmDataInvokerFactory.getDataInvoker("b", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:hsql://localhost/test", "sa", "", null, 1);

            final MainTestModel testModel = getNewTestModel(1, "test2");
            FullMessage message = null;

            IdentifiersResult result = cacheTranslator.removeModel(testModel.getIdentifier(), null);
            assertEquals(OperationStatus.OK, result.getOperationStatus());
            test(0, testModel.getIdentifier());

            try {
                dataInvoker.getModel(testModel.getIdentifier());
                fail("Test model in database!");
            } catch (final ModelNotExistException e) {
            }
            message = cacheTranslator.sendCommand(BaseCommand.START_NEW_SESSION);
            assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
            final String session = message.getHeader().getSessionId();

            message = cacheTranslator.sendCommandForModel(BaseCommand.PUT, testModel, session);
            assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

            message = cacheTranslator.sendCommandForId(BaseCommand.GET, testModel.getIdentifier(), session);
            assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
            assertEquals(1, message.getBody().getResponse().getResultList().size());

            try {
                dataInvoker.getModel(testModel.getIdentifier());
                fail("Test model in database!");
            } catch (final ModelNotExistException e) {
            }
            message = cacheTranslator.sendCommandForId(BaseCommand.IN_CACHE, testModel.getIdentifier(), null);
            assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
            assertEquals(1, message.getBody().getResponse().getIdentifiers().size());

            message = cacheTranslator.sendCommandForId(BaseCommand.IN_CACHE, new MainTestModelId(86761881), null);
            assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
            assertEquals(0, message.getBody().getResponse().getIdentifiers().size());

            test(2, testModel.getIdentifier());
            message = cacheTranslator.sendCommand(BaseCommand.COMMIT, session);
            test(2, testModel.getIdentifier());
            assertNotNull(dataInvoker.getModel(testModel.getIdentifier()));

            testModel.setName("changedTest");
            final String session2 = cacheTranslator.sendCommand(BaseCommand.START_NEW_SESSION).getHeader().getSessionId();
            message = cacheTranslator.sendCommandForModel(BaseCommand.PUT, testModel, session2);
            assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
            message = cacheTranslator.sendCommand(BaseCommand.COMMIT, session2);
            assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

            test(2, testModel.getIdentifier());
            // !!!!
            message = cacheTranslator.sendCommandForId(BaseCommand.GET, testModel.getIdentifier(), session);
            assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
            assertEquals("changedTest", ((com.nomad.cache.test.model.MainTestModel) message.getBody().getResponse().getResultList().iterator().next()).getName());

            test(2, testModel.getIdentifier());

            testModel.setName("changedTest2");
            final String session3 = cacheTranslator.sendCommand(BaseCommand.START_NEW_SESSION).getHeader().getSessionId();
            message = cacheTranslator.sendCommandForModel(BaseCommand.PUT, testModel, session3);
            assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
            message = cacheTranslator.sendCommand(BaseCommand.ROLLBACK, session3);
            assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

            message = cacheTranslator.sendCommandForId(BaseCommand.GET, testModel.getIdentifier(), session);
            assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
            assertEquals("changedTest", ((com.nomad.cache.test.model.MainTestModel) message.getBody().getResponse().getResultList().iterator().next()).getName());

            test(2, testModel.getIdentifier());
        } finally {
            if (cacheTranslator != null) {
                cacheTranslator.close();
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

    private void test(final int i, final Identifier id) throws Exception {
        int counter = 0;
        FullMessage message = cache1.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE, id, null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        counter += message.getBody().getResponse().getIdentifiers().size();

        message = cache2.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE, id, null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        counter += message.getBody().getResponse().getIdentifiers().size();

        message = cache3.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE, id, null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        counter += message.getBody().getResponse().getIdentifiers().size();

        assertEquals(i, counter);
    }
}
