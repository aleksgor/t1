package com.nomad.cache.relations;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.nomad.StartFormXml;
import com.nomad.cache.client.CommonTest;
import com.nomad.cache.test.model.Child;
import com.nomad.cache.test.model.ChildCriteria;
import com.nomad.cache.test.model.MainTestModel;
import com.nomad.cache.test.model.Price;
import com.nomad.cache.test.model.TestCriteria;
import com.nomad.client.CountResult;
import com.nomad.client.CriteriaResult;
import com.nomad.client.ModelsResult;
import com.nomad.client.SimpleCacheClient;
import com.nomad.message.FullMessage;
import com.nomad.message.OperationStatus;
import com.nomad.model.Identifier;
import com.nomad.model.ServiceCommand;
import com.nomad.pm.PmDataInvokerFactory;
import com.nomad.server.PmDataInvoker;

public class TestRelations extends CommonTest {

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
    private int length = 9;

    @BeforeClass
    public static void start() throws Exception {
        try {
            host = "localhost";
            commonSetup();
            final String[] files = { "configuration/matchserver/cacheManager.xml", "configuration/matchserver/cache1.xml", "configuration/matchserver/cache2.xml", "configuration/matchserver/cache3.xml",
                    "configuration/matchserver/cache4.xml" };
            starter = new StartFormXml();

            starter.startServers(files);

            cacheManager = new SimpleCacheClient(host, port0);
            cache1 = new SimpleCacheClient(host, port1);
            cache2 = new SimpleCacheClient(host, port2);
            cache3 = new SimpleCacheClient(host, port3);
            cache4 = new SimpleCacheClient(host, port4);

            pDataInvoker = PmDataInvokerFactory.getDataInvoker("localpostgres", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:hsql://localhost/test", "sa", "", "pm.cfg.xml", 1);

            sDataInvoker = PmDataInvokerFactory.getDataInvoker("localmysql", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:hsql://localhost/test", "sa", "", "pm2.cfg.xml", 1);

            sDataInvoker.eraseModel(new TestCriteria());
            pDataInvoker.eraseModel(new TestCriteria());
            sDataInvoker.eraseModel(new ChildCriteria());
            pDataInvoker.eraseModel(new ChildCriteria());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private Collection<Child> getChildrens() {
        Collection<Child> childs = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            childs.add(getChildModel(i, "child" + i));
        }
        return childs;
    }

    @Test
    public void test1() throws Exception {
        cacheManager.removeModel(new TestCriteria(),null);
        cacheManager.removeModel(new ChildCriteria(),null);

        Collection<MainTestModel> models = new ArrayList<>(length);
        listIds = new ArrayList<>(length);
        for (int i = 0; i < length * 8; i++) {
            MainTestModel tmodel= getNewTestModel(i, "test" + i);
            long mid=i/8;
            long mid2=mid+1;
            if(mid2>=length){
                mid2=0;
            }
            tmodel.setChildId(mid);
            tmodel.setSecondChildId(mid2);
            models.add(tmodel);

        }
        Collection<Child> childs = getChildrens();
        Price[] prices = new Price[length * 8];
        for (int i = 0; i < length * 8; i++) {
            prices[i] = getPriceModel(i, "test" + i);
        }

        CountResult cr = cacheManager.removeModel(new TestCriteria(), null);
        assertEquals(OperationStatus.OK, cr.getOperationStatus());
        assertEquals(0, getCount());

        ModelsResult mr = cacheManager.putModels(childs, null);
        assertEquals(OperationStatus.OK, mr.getOperationStatus());

        mr = cacheManager.putModels(models, null);
        assertEquals(OperationStatus.OK, mr.getOperationStatus());

        TestCriteria criteria = new TestCriteria();
        CriteriaResult<MainTestModel> criteriaResult = cacheManager.getModels(criteria, null);
        assertEquals(OperationStatus.OK, criteriaResult.getOperationStatus());
        assertEquals(72, criteriaResult.getResult().getResultList().size());
        for (MainTestModel model : criteriaResult.getResult().getResultList()) {
            assertNull(model.getChild());
            assertNull(model.getSecondChild());
        }

        criteria.addRelationLoad(TestCriteria.CHILD_1_RELATION);
        criteriaResult = cacheManager.getModels(criteria, null);
        assertEquals(OperationStatus.OK, criteriaResult.getOperationStatus());
        assertEquals(72, criteriaResult.getResult().getResultList().size());
        for (MainTestModel model : criteriaResult.getResult().getResultList()) {
            assertNotNull(model.getChild());
            assertNull(model.getSecondChild());
        }
        criteria.addRelationLoad(TestCriteria.CHILD_2_RELATION);
        criteriaResult = cacheManager.getModels(criteria, null);
        assertEquals(OperationStatus.OK, criteriaResult.getOperationStatus());
        assertEquals(72, criteriaResult.getResult().getResultList().size());
        for (MainTestModel model : criteriaResult.getResult().getResultList()) {
            assertNotNull(model.getChild());
            assertNotNull(model.getSecondChild());
            assertEquals(model.getChildId(), model.getChild().getId());
            assertEquals(model.getSecondChildId(), model.getSecondChild().getId());
        }
    }

    @Test
    public void test2() throws Exception {
        cacheManager.removeModel(new TestCriteria(),null);
        cacheManager.removeModel(new ChildCriteria(),null);

        Collection<MainTestModel> models = new ArrayList<>(length);
        listIds = new ArrayList<>(length);
        for (int i = 0; i < length * 8; i++) {
            MainTestModel tmodel= getNewTestModel(i, "test" + i);
            long mid=i/8;
            long mid2=mid+1;
            if(mid2>=length){
                mid2=0;
            }
            tmodel.setChildId(mid);
            tmodel.setSecondChildId(mid2);
            models.add(tmodel);

        }
        Collection<Child> childs = getChildrens();
        Price[] prices = new Price[length * 8];
        for (int i = 0; i < length * 8; i++) {
            prices[i] = getPriceModel(i, "test" + i);
        }

        CountResult cr = cacheManager.removeModel(new TestCriteria(), null);
        assertEquals(OperationStatus.OK, cr.getOperationStatus());
        assertEquals(0, getCount());

        ModelsResult mr = cacheManager.putModels(childs, null);
        assertEquals(OperationStatus.OK, mr.getOperationStatus());

        mr = cacheManager.putModels(models, null);
        assertEquals(OperationStatus.OK, mr.getOperationStatus());

        TestCriteria criteria = new TestCriteria();
        CriteriaResult<MainTestModel> criteriaResult = cacheManager.getModels(criteria, null);
        assertEquals(OperationStatus.OK, criteriaResult.getOperationStatus());
        assertEquals(72, criteriaResult.getResult().getResultList().size());
        for (MainTestModel model : criteriaResult.getResult().getResultList()) {
            assertNull(model.getChild());
            assertNull(model.getSecondChild());
        }

        criteria.addRelationLoad(TestCriteria.CHILD_1_RELATION);
        criteriaResult = cacheManager.getModels(criteria, null);
        assertEquals(OperationStatus.OK, criteriaResult.getOperationStatus());
        assertEquals(72, criteriaResult.getResult().getResultList().size());
        for (MainTestModel model : criteriaResult.getResult().getResultList()) {
            assertNotNull(model.getChild());
            assertNull(model.getSecondChild());
        }
        criteria.addRelationLoad(TestCriteria.CHILD_2_RELATION);
        criteriaResult = cacheManager.getModels(criteria, null);
        assertEquals(OperationStatus.OK, criteriaResult.getOperationStatus());
        assertEquals(72, criteriaResult.getResult().getResultList().size());
        for (MainTestModel model : criteriaResult.getResult().getResultList()) {
            assertNotNull(model.getChild());
            assertNotNull(model.getSecondChild());
            assertEquals(model.getChildId(), model.getChild().getId());
            assertEquals(model.getSecondChildId(), model.getSecondChild().getId());
        }
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

        message = cache4.sendCommandForId(ServiceCommand.GET_FROM_CACHE.toString(), listIds, null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        i += message.getBody().getResponse().getResultList().size();
        return i;
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
