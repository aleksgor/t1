package com.nomad.cache.multidatasource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.nomad.StartFormXml;
import com.nomad.cache.client.CommonTest;
import com.nomad.cache.test.model.MainTestModel;
import com.nomad.cache.test.model.TestCriteria;
import com.nomad.client.CountResult;
import com.nomad.client.CriteriaResult;
import com.nomad.client.ModelsResult;
import com.nomad.client.SimpleCacheClient;
import com.nomad.message.FullMessage;
import com.nomad.message.OperationStatus;
import com.nomad.model.BaseCommand;
import com.nomad.model.Criteria.Condition;
import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.model.ServiceCommand;
import com.nomad.model.criteria.AggregateFunction;
import com.nomad.model.criteria.StatisticElement;
import com.nomad.pm.PmDataInvokerFactory;
import com.nomad.server.PmDataInvoker;

public class TestMultiDataBase extends CommonTest {

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
            final String[] files = { "configuration/matchserver/cacheManager.xml", "configuration/matchserver/cache1.xml",
                    "configuration/matchserver/cache2.xml", "configuration/matchserver/cache3.xml", "configuration/matchserver/cache4.xml" };
            starter = new StartFormXml();

            starter.startServers(files);

            cacheManager = new SimpleCacheClient(host, port0);
            cache1 = new SimpleCacheClient(host, port1);
            cache2 = new SimpleCacheClient(host, port2);
            cache3 = new SimpleCacheClient(host, port3);
            cache4 = new SimpleCacheClient(host, port4);

            // pDataInvoker = PmDataInvokerFactory.getDataInvoker("postgres",
            // "org.postgresql.Driver", "jdbc:postgresql://localhost:5432/test",
            // "test", "test",
            // "pm.cfg.xml", 1);
            // sDataInvoker = PmDataInvokerFactory.getDataInvoker("mysql",
            // "org.postgresql.Driver", "jdbc:postgresql://localhost:5432/test",
            // "test", "test",
            // "pm2.cfg.xml", 1);
            pDataInvoker = PmDataInvokerFactory.getDataInvoker("postgres", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:hsql://localhost/test", "sa", "", "pm.cfg.xml",
                    1);

            sDataInvoker = PmDataInvokerFactory.getDataInvoker("mysql", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:hsql://localhost/test", "sa", "", "pm2.cfg.xml",
                    1);

            sDataInvoker.eraseModel(new TestCriteria());
            pDataInvoker.eraseModel(new TestCriteria());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test1() throws Exception {

        int length = 9;
        MainTestModel[] models = new MainTestModel[length];
        listIds = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            models[i] = getNewTestModel(i, "test" + i);
            listIds.add(models[i].getIdentifier());
        }

        FullMessage message = cacheManager.sendCommandForId(BaseCommand.DELETE, listIds, null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(0, getCount());

        message = cache1.sendCommandForId(ServiceCommand.GET_FROM_CACHE, listIds, null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(0, message.getBody().getResponse().getResultList().size());

        message = cache2.sendCommandForId(ServiceCommand.GET_FROM_CACHE, listIds, null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(0, message.getBody().getResponse().getResultList().size());

        message = cache3.sendCommandForId(ServiceCommand.GET_FROM_CACHE, listIds, null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(0, message.getBody().getResponse().getResultList().size());

        message = cacheManager.sendCommandForModel(BaseCommand.PUT, Arrays.<Model> asList(models), null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        assertEquals(length, getCount());

        TestCriteria criteria = (TestCriteria) new TestCriteria().addCriterion(TestCriteria.ID, Condition.GE, 0).addCriterion(TestCriteria.ID, Condition.LE,
                length);
        assertEquals(4, pDataInvoker.getList(criteria).getResultList().size());
        assertEquals(5, sDataInvoker.getList(criteria).getResultList().size());

    }

    @Test
    public void test2() throws Exception {
        int length = 9;
        MainTestModel[] models = new MainTestModel[length];
        listIds = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            models[i] = getNewTestModel(i, "test" + i);
            models[i].setMoney(i + (i / 10.0));
            listIds.add(models[i].getIdentifier());
        }

        TestCriteria criteria = new TestCriteria();
        criteria.addOrderAsc(TestCriteria.ID);
        CountResult countResult = cacheManager.removeModel(criteria, null);
        assertEquals(OperationStatus.OK, countResult.getOperationStatus());
        assertEquals(0, getCount());

        ModelsResult modelsResult = cacheManager.putModels(Arrays.<Model> asList(models), null);
        assertEquals(OperationStatus.OK, modelsResult.getOperationStatus());

        assertEquals(length, getCount());

        CriteriaResult<MainTestModel> result = cacheManager.getModels(criteria, null);
        assertEquals(OperationStatus.OK, result.getOperationStatus());
        assertEquals(length, result.getResult().getResultList().size());
        assertEquals(length, result.getResult().getCountAllRow());
        Collection<MainTestModel> resModels = result.getResult().getResultList();
        int index = 0;
        for (MainTestModel mainTestModel : resModels) {
            assertEquals(index, mainTestModel.getId());
            index++;
        }

        // paging
        criteria.setPageSize(5);
        result = cacheManager.getModels(criteria, null);
        assertEquals(OperationStatus.OK, result.getOperationStatus());
        assertEquals(5, result.getResult().getResultList().size());
        assertEquals(length, result.getResult().getCountAllRow());
        index = 0;
        for (MainTestModel mainTestModel : resModels) {
            assertEquals(index, mainTestModel.getId());
            index++;
        }
        // DESC
        criteria.cleanSort();
        criteria.addOrderDesc(TestCriteria.ID);
        result = cacheManager.getModels(criteria, null);
        resModels = result.getResult().getResultList();
        assertEquals(OperationStatus.OK, result.getOperationStatus());
        assertEquals(5, result.getResult().getResultList().size());
        assertEquals(length, result.getResult().getCountAllRow());
        index = 0;
        for (MainTestModel mainTestModel : resModels) {
            assertEquals(8 - index, mainTestModel.getId());
            index++;
        }

    }

    @Test
    public void testMultiSorting() throws Exception {
        int length = 9;
        MainTestModel[] models = new MainTestModel[length];
        listIds = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            models[i] = getNewTestModel(i, "test" + (i % 2 == 0 ? ("" + i) : ""));
            listIds.add(models[i].getIdentifier());
        }

        TestCriteria criteria = new TestCriteria();
        criteria.addOrderAsc(TestCriteria.NAME);
        criteria.addOrderAsc(TestCriteria.ID);
        CountResult countResult = cacheManager.removeModel(criteria, null);
        assertEquals(OperationStatus.OK, countResult.getOperationStatus());
        assertEquals(0, getCount());

        ModelsResult modelsResult = cacheManager.putModels(Arrays.<Model> asList(models), null);
        assertEquals(OperationStatus.OK, modelsResult.getOperationStatus());
        assertEquals(length, getCount());

        CriteriaResult<MainTestModel> result = cacheManager.getModels(criteria, null);
        assertEquals(OperationStatus.OK, result.getOperationStatus());
        assertEquals(length, result.getResult().getResultList().size());
        assertEquals(length, result.getResult().getCountAllRow());

        Collection<MainTestModel> resModels = result.getResult().getResultList();
        int[] template = { 1, 3, 5, 7, 0, 2, 4, 6, 8 };
        int index = 0;
        for (MainTestModel model : resModels) {
            assertEquals(template[index], model.getId());
            index++;

        }

        criteria.cleanSort();
        criteria.addOrderDesc(TestCriteria.NAME);
        criteria.addOrderDesc(TestCriteria.ID);
        result = cacheManager.getModels(criteria, null);
        assertEquals(OperationStatus.OK, result.getOperationStatus());
        assertEquals(length, result.getResult().getResultList().size());
        assertEquals(length, result.getResult().getCountAllRow());
        int[] template1 = { 8, 6, 4, 2, 0, 7, 5, 3, 1 };
        resModels = result.getResult().getResultList();
        index = 0;
        for (MainTestModel model : resModels) {
            assertEquals(template1[index], model.getId());
            index++;
        }
    }

    @Test
    public void testStatistic() throws Exception {
        int length = 9;
        MainTestModel[] models = new MainTestModel[length];
        listIds = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            models[i] = getNewTestModel(i, "test" + (i % 2 == 0 ? ("" + i) : ""));
            listIds.add(models[i].getIdentifier());
        }

        TestCriteria criteria = new TestCriteria();
        CountResult countResult = cacheManager.removeModel(criteria, null);
        assertEquals("remove model error !!", OperationStatus.OK, countResult.getOperationStatus());
        assertEquals(0, getCount());

        ModelsResult modelsResult = cacheManager.putModels(Arrays.<Model> asList(models), null);
        assertEquals("put model error !!", OperationStatus.OK, modelsResult.getOperationStatus());
        assertEquals(length, getCount());
        criteria.addStatisticRequirement(TestCriteria.ID, AggregateFunction.SUM);
        criteria.addStatisticRequirement(TestCriteria.ID, AggregateFunction.COUNT);
        criteria.addStatisticRequirement(TestCriteria.ID, AggregateFunction.MAX);
        criteria.addStatisticRequirement(TestCriteria.ID, AggregateFunction.MIN);
        criteria.addStatisticRequirement(TestCriteria.ID, AggregateFunction.AVG);
        CriteriaResult<MainTestModel> result = cacheManager.getModels(criteria, null);
        assertNotNull(result);
        assertNotNull(result.getResult());
        assertNotNull(result.getResult().getStatistics());
        assertEquals(5, result.getResult().getStatistics().size());
        Iterator<StatisticElement> statistic = result.getResult().getStatistics().iterator();
        assertEquals(9, result.getResult().getResultList().size());

        assertEquals(36.0d, statistic.next().getValue());
        assertEquals((double) length, statistic.next().getValue());
        assertEquals(8d, statistic.next().getValue());
        assertEquals(0d, statistic.next().getValue());
        assertEquals(4d, statistic.next().getValue());

    }

    @Test
    public void testGrouping() throws Exception {
        int length = 40;
        MainTestModel[] models = new MainTestModel[length];
        listIds = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            models[i] = getNewTestModel(i, "test" + (i % 2 == 0 ? "1" : ""));
            models[i].setChildId(i % 3);
            models[i].setSecondChildId(i % 5);
            models[i].setMoney(i + (i / 10.0));
            listIds.add(models[i].getIdentifier());
        }

        TestCriteria criteria = new TestCriteria();
        CountResult countResult = cacheManager.removeModel(criteria, null);
        assertEquals(OperationStatus.OK, countResult.getOperationStatus());
        assertEquals(0, getCount());

        ModelsResult modelsResult = cacheManager.putModels(Arrays.<Model> asList(models), null);
        assertEquals(OperationStatus.OK, modelsResult.getOperationStatus());
        assertEquals(length, getCount());

        criteria.addGroupRequirement(TestCriteria.CHILD_ID, AggregateFunction.COUNT);
        criteria.addGroupRequirement(TestCriteria.SECOND_CHILD_ID, AggregateFunction.COUNT);
        criteria.addGroupRequirement(TestCriteria.CHILD_ID, AggregateFunction.SUM);
        criteria.addGroupSelect(TestCriteria.MONEY, AggregateFunction.SUM);

        CriteriaResult<MainTestModel> result = cacheManager.getModels(criteria, null);

        assertEquals(3, result.getResult().getGroups().size());
        Iterator<StatisticElement> iterator = result.getResult().getGroups().iterator();
        assertEquals(length, result.getResult().getResultList().size());

        StatisticElement element0 = iterator.next();
        assertEquals("" + 0, "" + element0.getValue());
        assertEquals("childId", element0.getFieldName());
        assertEquals(5, element0.getChildren().size());
        assertNull(element0.getFunction());
        int i = 0;
        for (StatisticElement chse : element0.getChildren()) {
            assertEquals("" + i, "" + chse.getValue());
            assertNull(chse.getFunction());
            assertEquals("secondChildId", chse.getFieldName());
            assertEquals("first child:", 4, chse.getChildren().size());
            i++;
            for (StatisticElement chchse : chse.getChildren()) {
                assertNotNull(chchse.getFunction());
                assertEquals(0, chchse.getChildren().size());
            }
        }
        assertEquals("element 0,0", 49.5, getStatisticElement(0, 3, element0).getDoubleValue(), 0.001);
        assertEquals("element 0,1", 69.3d, getStatisticElement(1, 3, element0).getDoubleValue(), 0.001);
        assertEquals("element 0,2", 42.9d, getStatisticElement(2, 3, element0).getDoubleValue(), 0.001);
        assertEquals("element 0,3", 59.4d, getStatisticElement(3, 3, element0).getDoubleValue(), 0.001);
        assertEquals("element 0,4", 79.2d, getStatisticElement(4, 3, element0).getDoubleValue(), 0.001);

        /*
         * 0,0,49.50000000000000000 0,1,69.30000000000001000
         * 0,2,42.90000000000000000 0,3,59.39999999999999000
         * 0,4,79.19999999999999000
         */

        StatisticElement element1 = iterator.next();
        assertEquals("" + 1, "" + element1.getValue());
        assertEquals("childId", element1.getFieldName());
        assertEquals(5, element1.getChildren().size());
        assertNull(element1.getFunction());
        i = 0;
        for (StatisticElement chse : element1.getChildren()) {
            assertEquals("" + i, "" + chse.getValue());
            assertNull(chse.getFunction());
            assertEquals("secondChildId", chse.getFieldName());
            assertEquals("first child:", 4, chse.getChildren().size());
            i++;
            for (StatisticElement chchse : chse.getChildren()) {
                assertNotNull(chchse.getFunction());
                assertEquals(0, chchse.getChildren().size());

            }
        }
        /*
         * 1,0,38.50000000000000000 1,1,52.80000000000000400
         * 1,2,72.60000000000001000 1,3,45.10000000000000000
         * 1,4,62.69999999999999600
         */
        assertEquals("element 0,0", 38.5, getStatisticElement(0, 3, element1).getDoubleValue(), 0.001);
        assertEquals("element 0,1", 52.8d, getStatisticElement(1, 3, element1).getDoubleValue(), 0.001);
        assertEquals("element 0,2", 72.6d, getStatisticElement(2, 3, element1).getDoubleValue(), 0.001);
        assertEquals("element 0,3", 45.1d, getStatisticElement(3, 3, element1).getDoubleValue(), 0.001);
        assertEquals("element 0,4", 62.7d, getStatisticElement(4, 3, element1).getDoubleValue(), 0.001);

        StatisticElement element2 = iterator.next();
        assertEquals("" + 2, "" + element2.getValue());
        assertEquals("childId", element2.getFieldName());
        assertEquals(5, element2.getChildren().size());
        assertNull(element2.getFunction());
        i = 0;
        for (StatisticElement chse : element2.getChildren()) {
            assertEquals("" + i, "" + chse.getValue());
            assertNull(chse.getFunction());
            assertEquals("secondChildId", chse.getFieldName());
            assertEquals(4, chse.getChildren().size());
            i++;
            for (StatisticElement chchse : chse.getChildren()) {
                assertNotNull(chchse.getFunction());
                assertEquals(0, chchse.getChildren().size());

            }
        }
        /*
         * 2,0,66.00000000000000000 2,1,40.70000000000000000
         * 2,2,56.10000000000001000 2,3,75.89999999999999000
         * 2,4,47.30000000000000000
         */
        assertEquals("element 0,0", 66.0, getStatisticElement(0, 3, element2).getDoubleValue(), 0.001);
        assertEquals("element 0,1", 40.7d, getStatisticElement(1, 3, element2).getDoubleValue(), 0.001);
        assertEquals("element 0,2", 56.1d, getStatisticElement(2, 3, element2).getDoubleValue(), 0.001);
        assertEquals("element 0,3", 75.9d, getStatisticElement(3, 3, element2).getDoubleValue(), 0.001);
        assertEquals("element 0,4", 47.3d, getStatisticElement(4, 3, element2).getDoubleValue(), 0.001);

    }

    private StatisticElement getStatisticElement(int i, int j, StatisticElement element) {
        StatisticElement element0 = (StatisticElement) element.getChildren().toArray()[i];
        return (StatisticElement) element0.getChildren().toArray()[j];
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
