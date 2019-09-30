package com.nomad.cache.ssl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.nomad.StartFormXml;
import com.nomad.cache.client.CommonTest;
import com.nomad.cache.test.model.MainTestModel;
import com.nomad.cache.test.model.TestCriteria;
import com.nomad.client.CountResult;
import com.nomad.client.CriteriaResult;
import com.nomad.client.ModelsResult;
import com.nomad.client.SessionResult;
import com.nomad.client.SingleCacheClient;
import com.nomad.message.OperationStatus;
import com.nomad.model.Model;
import com.nomad.model.server.ProtocolType;

public class SSLTest extends CommonTest {

    private static StartFormXml starter;
    private static SingleCacheClient client;


    @org.junit.Test
    public void testAuthorization() throws Exception {
        SessionResult result = client.startSession("userlogin", "secretPassword");
        assertEquals(OperationStatus.OK, result.getOperationStatus());

        result = client.startSession("userlogin", "222");
        assertEquals(OperationStatus.ACCESS_DENIED, result.getOperationStatus());

    }

    @org.junit.Test
    public void testSession() throws Exception {
        SessionResult result = client.startSession("userlogin", "secretPassword");
        String session = result.getSessionId();

        final TestCriteria criteria = new TestCriteria();
        CountResult countResult = client.removeModel(criteria, session);
        assertEquals(OperationStatus.OK, countResult.getOperationStatus());
        client.commit(session);

        CriteriaResult<MainTestModel> criteriaResult = client.getModels(criteria, result.getSessionId());
        assertEquals(OperationStatus.OK, criteriaResult.getOperationStatus());
        assertEquals(0, criteriaResult.getResult().getCountAllRow());

        assertEquals(OperationStatus.OK, result.getOperationStatus());
        final List<Model> models = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            models.add(getNewTestModel(i, "test" + i));
        }
        ModelsResult modelsResult = client.putModels(models, null);
        assertEquals(OperationStatus.INVALID_SESSION, modelsResult.getOperationStatus());

        modelsResult = client.putModels(models, session);
        assertEquals(OperationStatus.OK, modelsResult.getOperationStatus());

        criteriaResult = client.getModels(criteria, result.getSessionId());
        assertEquals(OperationStatus.OK, criteriaResult.getOperationStatus());
        assertEquals(0, criteriaResult.getResult().getCountAllRow());
        client.commit(session);

        criteriaResult = client.getModels(criteria, result.getSessionId());
        assertEquals(OperationStatus.OK, criteriaResult.getOperationStatus());
        assertEquals(200, criteriaResult.getResult().getCountAllRow());

    }

    @BeforeClass
    public static void setUp() throws Exception {

        commonSetup();
        final String[] files = { "configuration/sslconfig/cacheManager.xml", "configuration/sslconfig/cache1.xml", "configuration/sslconfig/cache2.xml",
                "configuration/sslconfig/cache3.xml" };
        starter = new StartFormXml();
        starter.startServers(files);
        Map<String, String> properties = new HashMap<>();
        properties.put("javax.net.ssl.keyStore", "src/test/resources/keystore.jks");
        properties.put("javax.net.ssl.keyStorePassword", "storePassword");
        client = new SingleCacheClient("localhost", 2122, ProtocolType.SSL, properties);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        starter.stop();

    }

}
