package com.nomad.cache.authorization;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.nomad.StartFormXml;
import com.nomad.cache.client.CommonTest;
import com.nomad.cache.test.model.MainTestModel;
import com.nomad.cache.test.model.TestCriteria;
import com.nomad.client.CriteriaResult;
import com.nomad.client.ModelsResult;
import com.nomad.client.SessionResult;
import com.nomad.client.SingleCacheClient;
import com.nomad.message.OperationStatus;
import com.nomad.model.Model;

public class AuthorizationTest extends CommonTest {

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
        assertEquals(OperationStatus.OK, result.getOperationStatus());
        final List<Model> models = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            models.add(getNewTestModel(i, "test" + i));
        }
        ModelsResult modelsResult = client.putModels(models, null);
        assertEquals(OperationStatus.INVALID_SESSION, modelsResult.getOperationStatus());

        modelsResult = client.putModels(models, session);
        assertEquals(OperationStatus.OK, modelsResult.getOperationStatus());

        final TestCriteria criteria = new TestCriteria();
        CriteriaResult<MainTestModel> criteriaResult = client.getModels(criteria, result.getSessionId());
        assertEquals(OperationStatus.OK, criteriaResult.getOperationStatus());

    }

    @BeforeClass
    public static void setUp() throws Exception {

        commonSetup();
        final String[] files = { "configuration/authorization/cacheManager.xml", "configuration/authorization/cache1.xml", "configuration/authorization/cache2.xml",
                "configuration/authorization/cache3.xml" };
        starter = new StartFormXml();
        starter.startServers(files);
        client = new SingleCacheClient("localhost", 2122);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        starter.stop();

    }

}
