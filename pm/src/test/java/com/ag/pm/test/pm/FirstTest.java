package com.nomad.pm.test.pm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.nomad.exception.SystemException;
import com.nomad.model.Criteria;
import com.nomad.model.TransactInvoker;
import com.nomad.pm.PmDataInvokerFactory;
import com.nomad.pm.transactstore.TransactThreadInvoker;
import com.nomad.server.PmDataInvoker;
import com.nomad.pm.test.criteria.ChildCriteria;
import com.nomad.pm.test.criteria.TestCriteria;
import com.nomad.pm.test.models.Child;
import com.nomad.pm.test.models.MainClass;
import com.nomad.pm.test.models.TestId;

@RunWith(Parameterized.class)
public class FirstTest {
    private String driver;
    private String url;
    private String user;
    private String password;
    private String configurationFile;
    private String dataSource;

    private PmDataInvoker commonInvoker;
    private PmDataInvoker dataInvoker;
    private PmDataInvoker childInvoker;

    @Parameterized.Parameters
    public  static Collection<Object[]> parametersForTests() {
        return Arrays.asList(new Object[][] {
                {"a",null, "jdbc:postgresql://localhost:5432/test", "test", "test", "pm.cfg.xml"},
                {"b",null, "jdbc:mysql://localhost:3306/test?characterEncoding=utf8", "test", "test", "pm.cfg.xml"}
        });
    }

    public  FirstTest(String dataSource, String driver, String url, String user,String password, String configurationFile) {
        this.driver=driver;
        this.url=url;
        this.user=user;
        this.password=password;
        this.configurationFile = configurationFile;
        this.dataSource=dataSource;
    }

    @Parameters
    @org.junit.Test
    public void test1() throws Exception {
        MainClass model1;
        model1 = (MainClass) dataInvoker.getModel(Collections.singletonList(new TestId(1))).iterator().next();
        assertTrue(model1 != null);
        assertTrue("11".equals(model1.getMainName()));
        assertTrue(model1.getMainId() == 1);
        assertTrue(model1.getChildId() == 1);

    }

    @Parameters
    @org.junit.Test
    public void test2() throws Exception {
        MainClass model;
        model = (MainClass) dataInvoker.getModel(Collections.singletonList(new TestId(2))).iterator().next();
        assertTrue(model != null);
        assertTrue("22211".equals(model.getMainName()));
        assertTrue(model.getMainId() == 2);
        assertTrue(model.getChildId() == 2);
        assertTrue(model.getMainDate() != null);
    }

    @Parameters
    @org.junit.Test
    public void test25() throws SystemException {

        final TestCriteria tc = new TestCriteria();
        Collection<MainClass> result = dataInvoker.getList(tc).getResultList();
        assertTrue(result.size() == 2);

        tc.addCriterion(TestCriteria.ID, Criteria.Condition.LT, 3);
        result = dataInvoker.getList(tc).getResultList();
        assertTrue(result.size() == 2);
        tc.cleanCriterion();
        tc.addCriterion(TestCriteria.ID, Criteria.Condition.LT, 2);
        result = dataInvoker.getList(tc).getResultList();
        assertEquals(1, result.size() );

    }

    @Parameters
    @org.junit.Test
    public void testLoadChildren() throws SystemException {

        TestCriteria tc = new TestCriteria();
        tc.addRelationLoad("rChild");
        Collection<MainClass> result = dataInvoker.getList(tc).getResultList();
        assertEquals(2, result.size() );
        Iterator<MainClass>iterator=result.iterator();
        assertNotNull(iterator.next().getChild());
        assertNotNull(iterator.next().getChild());

        tc = new TestCriteria();
        tc.addRelationLoad("sChild");
        result = dataInvoker.getList(tc).getResultList();
        assertEquals(2, result.size() );
        iterator=result.iterator();

        assertNotNull(iterator.next().getSecondChild());
        assertNotNull(iterator.next().getSecondChild());
    }

    @Parameters
    @org.junit.Test
    public void testAdd() throws Exception {


        final MainClass newTst = new MainClass();
        newTst.setMainId(3);
        newTst.setMainName("Test3Name");
        newTst.setChildId(1);
        newTst.setMainDate(new Date());
        final MainClass restest = (MainClass) dataInvoker.addModel(Collections.singletonList(newTst)).iterator().next();
        assertTrue(restest.getMainId() == 3);
        assertTrue("Test3Name".equals(restest.getMainName()));

        final TestCriteria tc = new TestCriteria();
        Collection<MainClass> result = dataInvoker.getList(tc).getResultList();
        assertTrue(result.size() == 3);
        dataInvoker.eraseModel(Collections.singletonList(new TestId(3)));
        result = dataInvoker.getList(tc).getResultList();
        assertTrue(result.size() == 2);

    }

    @Parameters
    @org.junit.Test
    public void testTransactAdd() throws Exception  {


        final MainClass newTst = new MainClass();
        newTst.setMainId(3);
        newTst.setMainName("Test3Name");
        newTst.setChildId(1);
        newTst.setMainDate(new Date());

        final PmDataInvoker transactDataInvoker = new TransactThreadInvoker(dataInvoker);
        final TransactInvoker ti = (TransactInvoker) transactDataInvoker;
        ti.commit(null);
        MainClass restest = (MainClass) transactDataInvoker.addModel(Collections.singletonList(newTst)).iterator().next();

        assertEquals(3, restest.getMainId());
        assertTrue("Test3Name".equals(restest.getMainName()));

        final TestCriteria tc = new TestCriteria();
        Collection<MainClass> result = transactDataInvoker.getList(tc).getResultList();
        assertEquals(3, result.size());
        ti.rollBack(null);
        result = transactDataInvoker.getList(tc).getResultList();
        assertEquals(2, result.size());

        restest = (MainClass) transactDataInvoker.addModel(Collections.singletonList(newTst)).iterator().next();
        ti.commit(null);
        ti.rollBack(null);
        result = transactDataInvoker.getList(tc).getResultList();
        assertTrue(result.size() == 3);
        transactDataInvoker.eraseModel(Collections.singletonList(new TestId(3)));
        ti.commit(null);

    }

    public void testTransactRemove() throws Exception  {


        final MainClass newTst = new MainClass();
        newTst.setMainId(3);
        newTst.setMainName("Test3Name");
        newTst.setChildId(1);
        newTst.setMainDate(new Date());
        final TestCriteria tc = new TestCriteria();

        final PmDataInvoker transactDataInvoker = new TransactThreadInvoker(dataInvoker);
        final TransactInvoker ti = (TransactInvoker) transactDataInvoker;
        final MainClass restest =(MainClass)  transactDataInvoker.addModel(Collections.singletonList(newTst)).iterator().next();
        assertTrue(restest.getMainId() == 3);
        ti.commit(null);
        transactDataInvoker.getList(tc);
        transactDataInvoker.eraseModel(Collections.singletonList(new TestId(3)));
        ti.rollBack(null);
        assertTrue(transactDataInvoker.getList(tc).getCountAllRow() == 3);
        transactDataInvoker.eraseModel(Collections.singletonList(new TestId(3)));
        ti.commit(null);
        assertTrue(transactDataInvoker.getList(tc).getCountAllRow() == 2);

    }

    @Before
    public  void setUp() throws Exception {


        commonInvoker = PmDataInvokerFactory.getDataInvoker(dataSource, driver, url, user, password, configurationFile, 1);
        dataInvoker =  commonInvoker;
        childInvoker =  commonInvoker;
        dataInvoker.eraseModel(new TestCriteria());

        childInvoker.eraseModel(new ChildCriteria());
        final MainClass t1 = new MainClass();
        t1.setMainId(1);
        t1.setMainName("11");
        t1.setChildId(1);
        t1.setSecondChildId(2);
        t1.setMainDate(new Date());
        dataInvoker.addModel(Collections.singletonList(t1));
        final MainClass t2 = new MainClass();
        t2.setMainId(2);
        t2.setMainName("22211");
        t2.setSecondChildId(1);
        t2.setChildId(2);
        t2.setMainDate(new Date());
        dataInvoker.addModel(Collections.singletonList(t2));

        final Child ch1 = new Child();
        ch1.setId(1);
        ch1.setName("child1");
        childInvoker.addModel(Collections.singletonList(ch1));
        final Child ch2 = new Child();
        ch2.setId(2);
        ch2.setName("child2");
        childInvoker.addModel(Collections.singletonList(ch2));

    }

    @After
    public void tearDown() throws Exception {
        dataInvoker.close();
    }

}
