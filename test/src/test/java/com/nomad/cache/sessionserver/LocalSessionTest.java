package com.nomad.cache.sessionserver;

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

public class LocalSessionTest extends CommonTest {

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
        ModelsResult mResult = client.putModels(models, null);
        assertEquals(OperationStatus.INVALID_SESSION, mResult.getOperationStatus());

        mResult = client.putModels(models, session);
        assertEquals(OperationStatus.OK, mResult.getOperationStatus());

        final TestCriteria criteria = new TestCriteria();
        CriteriaResult<MainTestModel> cResult = client.getModels(criteria, result.getSessionId());
        assertEquals(OperationStatus.OK, cResult.getOperationStatus());

    }

    @BeforeClass
    public static void setUp() throws Exception {

        commonSetup();
        final String[] files = { "configuration/localsession/cacheManager.xml", "configuration/localsession/cache1.xml", "configuration/localsession/cache2.xml",
        "configuration/localsession/cache3.xml" };
        starter = new StartFormXml();
        starter.startServers(files);
        client = new SingleCacheClient("localhost", 2122);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        starter.stop();

    }

}
