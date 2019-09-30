package com.nomad.cache.update;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.nomad.StartFormXml;
import com.nomad.cache.client.CommonTest;
import com.nomad.cache.test.model.MainTestModel;
import com.nomad.cache.test.model.MainTestModelId;
import com.nomad.cache.test.model.TestCriteria;
import com.nomad.client.CriteriaResult;
import com.nomad.client.ModelsResult;
import com.nomad.client.SimpleCacheClient;
import com.nomad.exception.SystemException;
import com.nomad.message.OperationStatus;
import com.nomad.model.Identifier;
import com.nomad.model.update.UpdateItem;
import com.nomad.model.update.UpdateItem.Operation;
import com.nomad.model.update.UpdateItemImpl;
import com.nomad.model.update.UpdateRequest;
import com.nomad.model.update.UpdateRequestImpl;
import com.nomad.pm.PmDataInvokerFactory;
import com.nomad.server.PmDataInvoker;

public class TestUpdate extends CommonTest {

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
    private List<Identifier> listIds;
    private static PmDataInvoker pDataInvoker;
    private static PmDataInvoker sDataInvoker;

    @BeforeClass
    public static void start() throws Exception {
        try {
            host = "localhost";
            commonSetup();
            final String[] files = { "configuration/matchserver/cacheManager.xml", "configuration/matchserver/cache1.xml", "configuration/matchserver/cache2.xml",
                    "configuration/matchserver/cache3.xml", "configuration/matchserver/cache4.xml" };
            starter = new StartFormXml();

            starter.startServers(files);

            cacheManager = new SimpleCacheClient(host, port0);
            cache1 = new SimpleCacheClient(host, port1);
            cache2 = new SimpleCacheClient(host, port2);
            cache3 = new SimpleCacheClient(host, port3);
            cache4 = new SimpleCacheClient(host, port4);

            pDataInvoker = PmDataInvokerFactory.getDataInvoker("postgress", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:hsql://localhost/test", "sa", "", "pm.cfg.xml", 1);
            sDataInvoker = PmDataInvokerFactory.getDataInvoker("mysqll", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:hsql://localhost/test", "sa", "", "pm2.cfg.xml", 1);

            sDataInvoker.eraseModel(new TestCriteria());
            pDataInvoker.eraseModel(new TestCriteria());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test1() throws Exception {

        int length=9;
        MainTestModel[] models = new MainTestModel[length];
        listIds= new ArrayList<>(length);
        for(int i=0;i<length;i++){
            models[i]=getNewTestModel(i, "test"+i);
            models[i].setMoney(5.5);
            listIds.add(models[i].getIdentifier());
        }
        cacheManager.removeModel(new TestCriteria(), null);
        cacheManager.putModels(Arrays.asList(models), null);
        
        MainTestModel testModel= getModel(1);
        assertEquals("test1",testModel.getName());
        assertEquals(5.5,testModel.getMoney(),0.001);
        
        UpdateRequest updateRequest=new UpdateRequestImpl();
        updateRequest.setModelName(new MainTestModelId().getModelName());
        UpdateItem upItem= new UpdateItemImpl(TestCriteria.MONEY,"3",Operation.INCREMENT);
        updateRequest.getUpdateItems().add(upItem);
        ModelsResult  mResult= cacheManager.updateModels(null, new TestCriteria(), updateRequest, null);
        assertEquals(OperationStatus.OK, mResult.getOperationStatus());
        
        CriteriaResult<MainTestModel> testModels= cacheManager.getModels( new TestCriteria(), null);
        assertEquals(length, testModels.getResult().getResultList().size());
        
        for (MainTestModel mainTestModel : testModels.getResult().getResultList()) {
            assertEquals(8.5,mainTestModel.getMoney(),0.001);

        }

        updateRequest=new UpdateRequestImpl();
        updateRequest.setModelName(new MainTestModelId().getModelName());
        upItem= new UpdateItemImpl(TestCriteria.MONEY,"2",Operation.INCREMENT);
        updateRequest.getUpdateItems().add(upItem);
        mResult= cacheManager.updateModels(Collections.singletonList(new MainTestModelId(1)), null, updateRequest, null);
        assertEquals(OperationStatus.OK, mResult.getOperationStatus());
        
        testModel= getModel(1);
        assertEquals("test1",testModel.getName());
        assertEquals(10.5,testModel.getMoney(),0.001);
        
        
        

        
    }

    private MainTestModel getModel(long id) throws SystemException{
        ModelsResult mResult = cacheManager.getModel(new MainTestModelId(id) , null);
        assertEquals(OperationStatus.OK, mResult.getOperationStatus());
        assertEquals(1, mResult.getModels().size());
        return (MainTestModel) mResult.getModels().get(0);
        
    }
    @AfterClass
    public static void stop() {
        if (starter != null) {
            starter.stop();
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
        if (cache4 != null) {
            cache4.close();
        }

        if (pDataInvoker != null) {
            pDataInvoker.close();
        }
        if (sDataInvoker != null) {
            sDataInvoker.close();
        }

    }

}
