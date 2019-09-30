package com.nomad.pm.test.pm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.nomad.exception.ModelNotExistException;
import com.nomad.exception.SystemException;
import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.model.criteria.StatisticResult;
import com.nomad.pm.PmDataInvokerFactory;
import com.nomad.server.PmDataInvoker;
import com.nomad.pm.test.criteria.ChildCriteria;
import com.nomad.pm.test.criteria.TestCriteria;
import com.nomad.pm.test.models.Child;
import com.nomad.pm.test.models.MainClass;
import com.nomad.pm.test.models.TestId;

public class SQLTest {
    private static String driver = "org.hsqldb.jdbcDriver";
    private static String url = "jdbc:hsqldb:hsql://localhost/test";
    private static String user = "sa";
    private static String password = "";
    private static String configurationFile = "pm.cfg.xml";
    private static String dataSource = "q";

    private static PmDataInvoker dataInvoker;

    private static MainClass t1;
    private static MainClass t2;
    private static Child ch1;
    private static Child ch2;

    @Test
    public void test1() throws Exception {
        setUp();
        MainClass model1;
        model1 = (MainClass) dataInvoker.getModel(new TestId(1));
        assertNotNull(model1);
        assertEquals("11", model1.getMainName());
        assertEquals(1, model1.getMainId());
        assertEquals(1, model1.getChildId());

        Collection<Model> collection = dataInvoker.getModel(Arrays.asList(new TestId(1), new TestId(2)));
        assertNotNull(collection);
        assertEquals(2, collection.size());

        StatisticResult<MainClass>  result = dataInvoker.getList(new TestCriteria());
        assertNotNull(result.getResultList());
        assertEquals(2, result.getResultList().size());
        Iterator<MainClass> iterator=result.getResultList().iterator();
        assertNotNull(iterator.next());
        assertNotNull(iterator.next());
        

        dataInvoker.eraseModel(Arrays.asList(model1.getIdentifier()));
        try {
            model1 = (MainClass) dataInvoker.getModel(new TestId(1));
            fail();
        } catch (final ModelNotExistException e) {

        }

    }

    @Test
    public void testCriteriaIds() throws Exception {
        setUp();
        MainClass model1;
        model1 = (MainClass) dataInvoker.getModel(new TestId(1));
        assertNotNull(model1);
        assertEquals("11", model1.getMainName());
        assertEquals(1, model1.getMainId());
        assertEquals(1, model1.getChildId());

        StatisticResult<MainClass>  result = dataInvoker.getIds(new TestCriteria());
        assertNotNull(result.getIdentifiers());
        assertEquals(2, result.getIdentifiers().size());
        Iterator<Identifier> iterator=result.getIdentifiers().iterator();
        assertNotNull(iterator.next());
        assertNotNull(iterator.next());
        

        dataInvoker.eraseModel(Arrays.asList(model1.getIdentifier()));
        try {
            model1 = (MainClass) dataInvoker.getModel(new TestId(1));
            fail();
        } catch (final ModelNotExistException e) {

        }

    }

    @Test
    public void testAddSomeModels() throws Exception {
        dataInvoker.eraseModel(new TestCriteria());
        try {
            dataInvoker.getModel(new TestId(1));
            fail();
        } catch (ModelNotExistException e) {
            ;
        } catch (SystemException e) {
            fail();
        }
        try {
            dataInvoker.getModel(new TestId(2));
            fail();
        } catch (ModelNotExistException e) {
            ;
        } catch (SystemException e) {
            fail();
        }

        Collection<Model> models = new ArrayList<>();
        models.add(t1);
        models.add(t2);
        Collection<Model> result = dataInvoker.addModel(models);
        assertEquals(2, result.size());
        
        StatisticResult<MainClass>  res= dataInvoker.getList(new TestCriteria());
        assertEquals(2, res.getResultList().size());

    }

    @Test
    public void testUpdateSomeModels() throws Exception {
        dataInvoker.eraseModel(new TestCriteria());

        Collection<Model> result = dataInvoker.addModel(Arrays.asList(t1,t2));
        assertEquals(2, result.size());
        
        TestCriteria criteria= new TestCriteria();
        criteria.addOrderAsc(TestCriteria.ID);
        StatisticResult<MainClass>  res= dataInvoker.getList(criteria);
        assertEquals(2, res.getResultList().size());
        t1.setMainName("Update1");
        t2.setMainName("Update2");
        dataInvoker.updateModel(Arrays.asList(t1,t2));
        res= dataInvoker.getList(criteria);
        assertEquals(2, res.getResultList().size());
        Iterator<MainClass> iterator=res.getResultList().iterator();
        MainClass t11=iterator.next();
        MainClass t12=iterator.next();
        assertEquals("Update1", t11.getMainName());
        assertEquals("Update2", t12.getMainName());
    }

    
    @Test
    public void testEraseSomeModels() throws Exception {
        dataInvoker.eraseModel(new TestCriteria());
        Collection<Model> result = dataInvoker.addModel(Arrays.asList(t1,t2));
        assertEquals(2, result.size());
        
        TestCriteria criteria= new TestCriteria();
        criteria.addOrderAsc(TestCriteria.ID);
        assertEquals(2, dataInvoker.eraseModel(Arrays.asList(t1.getIdentifier(),t2.getIdentifier())));
        assertEquals(0, dataInvoker.eraseModel(Arrays.asList(t1.getIdentifier(),t2.getIdentifier())));

        StatisticResult<MainClass> res= dataInvoker.getList(criteria);
        assertEquals(0, res.getResultList().size());
        
    }
    
    public void setUp() throws Exception {

        dataInvoker.eraseModel(new TestCriteria());
        dataInvoker.eraseModel(new ChildCriteria());

        dataInvoker.addModel(Collections.singletonList(t1));
        dataInvoker.addModel(Collections.singletonList(t2));
        dataInvoker.addModel(Collections.singletonList(ch1));
        dataInvoker.addModel(Collections.singletonList(ch2));

    }

    private static void fillModels() {
        t1 = new MainClass();
        t1.setMainId(1);
        t1.setMainName("11");
        t1.setChildId(1);
        t1.setSecondChildId(2);
        t1.setMainDate(new Date());
        t1.setIdentifier(new TestId(t1.getMainId()));
        
        t2 = new MainClass();
        t2.setMainId(2);
        t2.setMainName("22211");
        t2.setSecondChildId(1);
        t2.setChildId(2);
        t2.setMainDate(new Date());
        t2.setIdentifier(new TestId(t2.getMainId()));

        ch1 = new Child();
        ch1.setId(1);
        ch1.setName("child1");

        ch2 = new Child();
        ch2.setId(2);
        ch2.setName("child2");

    }

    @BeforeClass
    public static void satrt() throws Exception {
        dataInvoker = PmDataInvokerFactory.getDataInvoker(dataSource, driver, url, user, password, configurationFile, 1);
        fillModels();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        dataInvoker.close();
    }

}
