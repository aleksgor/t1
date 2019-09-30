package com.nomad.pm.test.pm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.nomad.model.Criteria;
import com.nomad.model.Criteria.Condition;
import com.nomad.model.Identifier;
import com.nomad.model.criteria.CriteriaGroupItem;
import com.nomad.model.criteria.CriteriaItemImpl;
import com.nomad.pm.PmDataInvokerFactory;
import com.nomad.pm.exception.SysPmException;
import com.nomad.server.PmDataInvoker;
import com.nomad.pm.test.criteria.ChildCriteria;
import com.nomad.pm.test.criteria.TestCriteria;
import com.nomad.pm.test.models.Child;
import com.nomad.pm.test.models.MainClass;
import com.nomad.pm.test.models.TestId;

@RunWith(Parameterized.class)
public class CriteriaTest {
    private  String driver;
    private String url;
    private String user;
    private String password;
    private String configurationFile;
    private String dataSource;

    private static PmDataInvoker dataInvoker;

    public  CriteriaTest(String dataSource, String driver, String url, String user,String password, String cfgFile) {
        this.driver=driver;
        this.url=url;
        this.user=user;
        this.password=password;
        this.configurationFile=cfgFile;
        this.dataSource=dataSource;
    }

    @Parameterized.Parameters
    public  static Collection<Object[]> parametersForTests() {
        return Arrays.asList(new Object[][] {
                {"a",null, "jdbc:postgresql://localhost:5432/test", "test", "test", "pm.cfg.xml"},
                {"b",null, "jdbc:mysql://localhost:3306/test?characterEncoding=utf8", "test", "test", "pm.cfg.xml"}
        });
    }


    @Parameters
    @org.junit.Test
    public void test1() throws Exception {
        TestCriteria tcr = new TestCriteria();

        Collection<MainClass> result = dataInvoker.getList(tcr).getResultList();
        assertEquals(4, result.size());

        tcr.addCriterion(TestCriteria.ID, TestCriteria.Condition.EQ, 2);
        result = dataInvoker.getList(tcr).getResultList();
        assertEquals(1, result.size());
        assertEquals(2, result.iterator().next().getMainId());

        tcr = new TestCriteria();
        tcr.addCriterion(TestCriteria.ID, TestCriteria.Condition.NE, 2);
        tcr.addOrderAsc(TestCriteria.ID);
        result = dataInvoker.getList(tcr).getResultList();
        assertEquals(3, result.size());
        Iterator<MainClass> iterator=result.iterator();
        assertEquals(1, iterator.next().getMainId());
        assertEquals(3, iterator.next().getMainId());
        assertEquals(4, iterator.next().getMainId());

        tcr = new TestCriteria();
        tcr.addCriterion(TestCriteria.ID, TestCriteria.Condition.GE, 2);
        tcr.addOrderAsc(TestCriteria.ID);
        result = dataInvoker.getList(tcr).getResultList();
        assertEquals(3, result.size());
        iterator=result.iterator();

        assertEquals(2, iterator.next().getMainId());
        assertEquals(3, iterator.next().getMainId());
        assertEquals(4, iterator.next().getMainId());

        tcr = new TestCriteria();
        tcr.addCriterion(TestCriteria.ID, TestCriteria.Condition.GT, 2);
        tcr.addOrderAsc(TestCriteria.ID);
        result = dataInvoker.getList(tcr).getResultList();
        assertEquals(2, result.size());
        iterator=result.iterator();
        assertEquals(3, iterator.next().getMainId());
        assertEquals(4, iterator.next().getMainId());

        tcr = new TestCriteria();
        tcr.addCriterion(TestCriteria.ID, TestCriteria.Condition.LE, 2);
        tcr.addOrderAsc(TestCriteria.ID);
        result = dataInvoker.getList(tcr).getResultList();
        assertEquals(2, result.size());
        iterator=result.iterator();
        assertEquals(1, iterator.next().getMainId());
        assertEquals(2, iterator.next().getMainId());

        tcr = new TestCriteria();
        tcr.addCriterion(TestCriteria.ID, TestCriteria.Condition.LT, 2);
        tcr.addOrderAsc(TestCriteria.ID);
        result = dataInvoker.getList(tcr).getResultList();
        assertEquals(1, result.size());
        assertEquals(1, result.iterator().next().getMainId());

        tcr = new TestCriteria();
        tcr.addCriterion(TestCriteria.ID, TestCriteria.Condition.GE, 2);
        tcr.addOrderDesc(TestCriteria.ID);
        result = dataInvoker.getList(tcr).getResultList();
        assertEquals(3, result.size());
        iterator=result.iterator();
        assertEquals(4, iterator.next().getMainId());
        assertEquals(3, iterator.next().getMainId());
        assertEquals(2, iterator.next().getMainId());

    }

    @Parameters
    @org.junit.Test
    public void testLike() throws Exception {
        TestCriteria tcr = new TestCriteria();
        Collection<MainClass> result;
        tcr.addCriterion(TestCriteria.NAME, TestCriteria.Condition.LIKE_RIGHT, "222");
        tcr.addOrderDesc(TestCriteria.ID);
        result = dataInvoker.getList(tcr).getResultList();
        assertEquals(3, result.size());
        Iterator<MainClass> iterator=result.iterator();

        assertEquals(4, iterator.next().getMainId());
        assertEquals(3, iterator.next().getMainId());
        assertEquals(2, iterator.next().getMainId());

        tcr = new TestCriteria();
        tcr.addCriterion(TestCriteria.NAME, TestCriteria.Condition.LIKE_LEFT, "1");
        tcr.addOrderDesc(TestCriteria.ID);
        result = dataInvoker.getList(tcr).getResultList();
        assertEquals(2, result.size());
        iterator=result.iterator();
        assertEquals(2, iterator.next().getMainId());
        assertEquals(1, iterator.next().getMainId());

        tcr = new TestCriteria();
        tcr.addCriterion(TestCriteria.SECOND_CHILD_NAME, TestCriteria.Condition.LIKE_ALL, "il");
        tcr.addOrderAsc(TestCriteria.ID);
        result = dataInvoker.getList(tcr).getResultList();
        assertEquals(4, result.size());
        iterator=result.iterator();
        assertEquals(1, iterator.next().getMainId());
        assertEquals(2, iterator.next().getMainId());
        assertEquals(3, iterator.next().getMainId());
        assertEquals(4, iterator.next().getMainId());

        tcr = new TestCriteria();
        tcr.addCriterion(TestCriteria.SECOND_CHILD_NAME, TestCriteria.Condition.LIKE_ALL, "ld1");
        tcr.addOrderAsc(TestCriteria.ID);
        result = dataInvoker.getList(tcr).getResultList();
        assertEquals(2, result.size());
        iterator=result.iterator();
        assertEquals(2, iterator.next().getMainId());
        assertEquals(3, iterator.next().getMainId());

        tcr = new TestCriteria();
        tcr.addCriterion(TestCriteria.SECOND_CHILD_NAME, TestCriteria.Condition.EQ_MASK, "*ld1");
        tcr.addOrderAsc(TestCriteria.ID);
        result = dataInvoker.getList(tcr).getResultList();
        assertEquals(2, result.size());
        iterator=result.iterator();
        assertEquals(2, iterator.next().getMainId());
        assertEquals(3, iterator.next().getMainId());

        tcr = new TestCriteria();
        tcr.addCriterion(TestCriteria.SECOND_CHILD_NAME, TestCriteria.Condition.EQ_MASK, "child?");
        tcr.addOrderAsc(TestCriteria.ID);
        result = dataInvoker.getList(tcr).getResultList();
        assertEquals(4, result.size());

    }

    @Parameters
    @org.junit.Test
    public void testRelation() throws Exception {
        TestCriteria tcr = new TestCriteria();
        Collection<MainClass> result;

        tcr = new TestCriteria();
        tcr.addRelationLoad(TestCriteria.RELATIONR);
        tcr.addOrderAsc("id", TestCriteria.RELATIONR);
        tcr.addOrderDesc(TestCriteria.ID);
        result = dataInvoker.getList(tcr).getResultList();
        assertEquals(4, result.size());
        Iterator<MainClass> iterator=result.iterator();

        assertEquals(3, iterator.next().getMainId());
        assertEquals(1, iterator.next().getMainId());

        tcr = new TestCriteria();
        tcr.addRelationLoad(TestCriteria.RELATIONS);
        tcr.addOrderAsc("id", TestCriteria.RELATIONS);
        tcr.addOrderDesc(TestCriteria.ID);
        result = dataInvoker.getList(tcr).getResultList();
        assertEquals(4, result.size());
        iterator=result.iterator();
        assertEquals(3, iterator.next().getMainId());
        assertEquals(2, iterator.next().getMainId());

        tcr = new TestCriteria();
        tcr.addCriterion(TestCriteria.RELATIONR+"."+"id", Criteria.Condition.EQ, 1);
        tcr.addOrderAsc(TestCriteria.ID);
        result = dataInvoker.getList(tcr).getResultList();
        assertEquals(2, result.size());
        iterator=result.iterator();
        assertEquals(1, iterator.next().getMainId());
        assertEquals(3, iterator.next().getMainId());

        tcr = new TestCriteria();
        tcr.addCriterion(TestCriteria.RELATIONR+"."+"id", Criteria.Condition.EQ, 1);
        tcr.addCriterion(TestCriteria.ID, Criteria.Condition.EQ, 3);
        tcr.addOrderAsc(TestCriteria.ID);
        result = dataInvoker.getList(tcr).getResultList();
        assertEquals(1, result.size());
        assertEquals(3, result.iterator().next().getMainId());
        assertNull( result.iterator().next().getChild());
        assertNull( result.iterator().next().getSecondChild());

        tcr = new TestCriteria();
        tcr.addRelationLoad(TestCriteria.RELATIONS);
        tcr.addCriterion(TestCriteria.RELATIONR+"."+"id", Criteria.Condition.EQ, 1);
        tcr.addCriterion(TestCriteria.ID, Criteria.Condition.EQ, 3);
        tcr.addOrderAsc(TestCriteria.ID);
        result = dataInvoker.getList(tcr).getResultList();
        assertEquals(1, result.size());
        assertEquals(3, result.iterator().next().getMainId());
        assertNull( result.iterator().next().getChild());
        assertNotNull( result.iterator().next().getSecondChild());

    }

    @Parameters
    @org.junit.Test
    public void testIn() throws Exception {
        TestCriteria tcr = new TestCriteria();
        Collection<MainClass> result;

        tcr = new TestCriteria();
        final List<Object> params = new ArrayList<>();
        params.add(1);
        params.add(4);
        params.add(3);
        tcr.addCriterion(TestCriteria.ID, Condition.IN, params);
        tcr.addOrderAsc(TestCriteria.ID);
        result = dataInvoker.getList(tcr).getResultList();
        assertEquals(3, result.size());
        Iterator<MainClass> iterator=result.iterator();
        assertEquals(1, iterator.next().getMainId());
        assertEquals(3, iterator.next().getMainId());
        assertEquals(4, iterator.next().getMainId());


        final ChildCriteria chCrit= new ChildCriteria();
        chCrit.addCriterion(ChildCriteria.ID, Condition.EQ, 1);

        tcr = new TestCriteria();
        tcr.addCriterion(TestCriteria.RELATIONR, chCrit);
        tcr.addOrderAsc(TestCriteria.ID);
        result = dataInvoker.getList(tcr).getResultList();
        assertEquals(2, result.size());

        tcr = new TestCriteria();
        tcr.addCriterion(TestCriteria.RELATIONR, chCrit);
        tcr.addCriterion(TestCriteria.NAME, Condition.LIKE_ALL, "213");
        tcr.addOrderAsc(TestCriteria.ID);
        result = dataInvoker.getList(tcr).getResultList();
        assertEquals(1, result.size());
        assertEquals(3, result.iterator().next().getMainId());

    }

    @Parameters
    @org.junit.Test
    public void testOuterJoin() throws Exception {
        TestCriteria tcr = new TestCriteria();
        Collection<MainClass> result;

        tcr = new TestCriteria();
        tcr.addRelationLoad(TestCriteria.RELATIONO);
        tcr.addOrderAsc(TestCriteria.ID);
        result = dataInvoker.getList(tcr).getResultList();
        assertEquals(4, result.size());
        Iterator <MainClass> iterator=result.iterator();
        MainClass first=iterator.next();
        MainClass second=iterator.next();
        MainClass third=iterator.next();
        MainClass forth=iterator.next();
        
        assertEquals(1, first.getMainId());
        assertNotNull(first.getThirdChild());
        assertEquals(2, second.getMainId());
        assertNotNull(second.getThirdChild());
        assertEquals(3, third.getMainId());
        assertNotNull(third.getThirdChild());
        assertEquals(4, forth.getMainId());
        assertNotNull(forth.getThirdChild());

        tcr = new TestCriteria();
        tcr.addRelationLoad(TestCriteria.RELATIONO);
        tcr.addRelationLoad(TestCriteria.RELATIONS);
        tcr.addRelationLoad(TestCriteria.RELATIONR);
        tcr.addOrderAsc(TestCriteria.ID);
        result = dataInvoker.getList(tcr).getResultList();
        assertEquals(4, result.size());
        iterator=result.iterator();
        first=iterator.next();
        second=iterator.next();
        third=iterator.next();
        forth=iterator.next();

        assertEquals(1, first.getMainId());
        assertNotNull(first.getChild());
        assertNotNull(first.getSecondChild());
        assertNotNull(first.getThirdChild());

        assertEquals(2, second.getMainId());
        assertNotNull(second.getChild());
        assertNotNull(second.getSecondChild());
        assertNotNull(second.getThirdChild());
        assertEquals(3, third.getMainId());
        assertNotNull(third.getChild());
        assertNotNull(third.getSecondChild());
        assertNotNull(third.getThirdChild());

        assertEquals(4, forth.getMainId());
        assertNotNull(forth.getChild());
        assertNotNull(forth.getSecondChild());
        assertNotNull(forth.getThirdChild());
    }

    @Parameters
    @org.junit.Test
    public void testAND() throws Exception {
        TestCriteria tcr = new TestCriteria();
        Collection<MainClass> result;

        tcr = new TestCriteria();
        CriteriaGroupItem criteriaGroup= new CriteriaGroupItem();
        criteriaGroup.addCriteriaItem(new CriteriaItemImpl(TestCriteria.ID, 1, Condition.EQ)).addCriteriaItem(new CriteriaItemImpl(TestCriteria.ID, 2, Condition.EQ));
        tcr.addORCriterion(criteriaGroup);
        tcr.addOrderAsc(TestCriteria.ID);
        result = dataInvoker.getList(tcr).getResultList();
        assertEquals(2, result.size());
        Iterator<MainClass>iterator= result.iterator();
        assertEquals(1, iterator.next().getMainId());
        assertEquals(2, iterator.next().getMainId());

        tcr = new TestCriteria();
        tcr.addRelationLoad(TestCriteria.RELATIONR);
        criteriaGroup= new CriteriaGroupItem();
        criteriaGroup.addCriteriaItem(new CriteriaItemImpl(TestCriteria.ID, 1, Condition.EQ)).addCriteriaItem(new CriteriaItemImpl(TestCriteria.ID, 2, Condition.EQ));
        tcr.addORCriterion(criteriaGroup);
        tcr.addOrderAsc(TestCriteria.ID);
        result = dataInvoker.getList(tcr).getResultList();
        assertEquals(2, result.size());
        iterator= result.iterator();
        MainClass first=iterator.next();
        MainClass second=iterator.next();

        assertEquals(1, first.getMainId());
        assertNotNull(first.getChild());
        assertEquals(2, second.getMainId());

        tcr = new TestCriteria();
        tcr.addRelationLoad(TestCriteria.RELATIONR);
        tcr.addRelationLoad(TestCriteria.RELATIONS);
        criteriaGroup= new CriteriaGroupItem();
        criteriaGroup.addCriteriaItem(new CriteriaItemImpl(TestCriteria.ID, 1, Condition.EQ)).addCriteriaItem(new CriteriaItemImpl(TestCriteria.ID, 2, Condition.EQ));
        tcr.addORCriterion(criteriaGroup);
        tcr.addOrderAsc(TestCriteria.ID);
        result = dataInvoker.getList(tcr).getResultList();
        assertEquals(2, result.size());
        iterator= result.iterator();
        first=iterator.next();
         second=iterator.next();
        assertEquals(1, first.getMainId());
        assertNotNull(first.getChild());
        assertNotNull(first.getSecondChild());
        assertEquals(2, second.getMainId());

        tcr = new TestCriteria();
        tcr.addRelationLoad(TestCriteria.RELATIONR);
        tcr.addRelationLoad(TestCriteria.RELATIONS);
        criteriaGroup= new CriteriaGroupItem();
        criteriaGroup.addCriteriaItem(new CriteriaItemImpl(TestCriteria.ID, 1, Condition.EQ)).addCriteriaItem(new CriteriaItemImpl(TestCriteria.ID, 2, Condition.EQ));
        tcr.addORCriterion(criteriaGroup);
        tcr.addOrderAsc(TestCriteria.ID);
        result = dataInvoker.getList(tcr).getResultList();
        assertEquals(2, result.size());
        iterator= result.iterator();
        first=iterator.next();
         second=iterator.next();
        assertEquals(1, first.getMainId());
        assertNotNull(first.getChild());
        assertNotNull(first.getSecondChild());
        assertEquals(2, second.getMainId());

    }

    @Parameters
    @org.junit.Test
    public void testPage() throws Exception {
        TestCriteria tcr = new TestCriteria();
        Collection<MainClass> result;

        tcr = new TestCriteria();
        tcr.setPageSize(2);
        tcr.addOrderAsc(TestCriteria.ID);
        result = dataInvoker.getList(tcr).getResultList();
        assertEquals(2, result.size());
        Iterator<MainClass>iterator= result.iterator();

        assertEquals(1, iterator.next().getMainId());
        assertEquals(2, iterator.next().getMainId());

        tcr.setPageSize(2);
        tcr.setStartPosition(tcr.getStartPosition() + tcr.getPageSize());
        // tcr.next();
        result = dataInvoker.getList(tcr).getResultList();
        assertEquals(2, result.size());
        iterator= result.iterator();
        assertEquals(3, iterator.next().getMainId());
        assertEquals(4, iterator.next().getMainId());

    }

    @org.junit.Test
    public void testBlob() throws Exception {
        TestCriteria tcr = new TestCriteria();
        Collection<MainClass> result;

        tcr = new TestCriteria();
        tcr.addOrderAsc(TestCriteria.ID);
        result = dataInvoker.getList(tcr).getResultList();
        assertEquals(4, result.size());

        final byte [] data=result.iterator().next().getBlob();
        assertEquals(2048, data.length);

        tcr.setBinaryLoad(false);
        result = dataInvoker.getList(tcr).getResultList();
        assertEquals(4, result.size());
        assertNull( result.iterator().next().getBlob());


    }

    @Parameters
    @org.junit.Test
    public void testMessage() throws Exception {
        final TestCriteria tcr = new TestCriteria();

        Collection<MainClass> result = dataInvoker.getList(tcr).getResultList();
        assertEquals(4, result.size());

        tcr.addCriterion(TestCriteria.fakeName, Condition.EQ, "1");
        try{
            result = dataInvoker.getList(tcr).getResultList();
            fail();
        }catch(final SysPmException pme){
            assertEquals("Field:'fakeName' not found in model:'Test'", pme.getMessage());
        }
        tcr.cleanCriterion();
        tcr.addOrderAsc(TestCriteria.fakeName);
        try{
            result = dataInvoker.getList(tcr).getResultList();
            fail();
        }catch(final SysPmException pme){
            assertEquals("field:'fakeName' not found in:'Test'", pme.getMessage());
        }
        tcr.cleanCriterion();
        tcr.addOrderAsc(TestCriteria.fakechildName);
        try{
            result = dataInvoker.getList(tcr).getResultList();
            fail();
        }catch(final SysPmException pme){
            assertEquals("field:'rChild.fakechildName' not found in:'Test'", pme.getMessage());
        }

        tcr.cleanCriterion();
        tcr.addRelationLoad(TestCriteria.fakeRelation);
        try{
            result = dataInvoker.getList(tcr).getResultList();
            fail();
        }catch(final SysPmException pme){
            assertEquals("Relation:'fakeRelation' not found in table:'Test'", pme.getMessage());
        }

    }

    @Before
    public  void setUp() throws Exception {

        final byte []  blob= new byte[2048];
        for(int i=0;i<blob.length;i++){
            blob[i]=(byte) (i % 127);
        }
        dataInvoker = PmDataInvokerFactory.getDataInvoker(dataSource, driver, url, user, password, configurationFile, 1);

        dataInvoker.eraseModel(new TestCriteria());
        dataInvoker.eraseModel(new ChildCriteria());

        dataInvoker.addModel(Collections.singletonList(getTest(1, "11", 1, new Date(),2,blob)));
        dataInvoker.addModel(Collections.singletonList(getTest(2, "22211", 2, new Date(),1,blob)));
        dataInvoker.addModel(Collections.singletonList(getTest(3, "22213", 1, new Date(),1,blob)));
        dataInvoker.addModel(Collections.singletonList(getTest(4, "22214", 2, new Date(),2,blob)));

        dataInvoker.addModel(Collections.singletonList(getChild(1, "child1")));
        dataInvoker.addModel(Collections.singletonList(getChild(2, "child2")));


    }

    @Parameters
    @org.junit.Test
    public void testSelectIds() throws Exception {
        final TestCriteria tcr = new TestCriteria();

        Collection<Identifier> result = dataInvoker.getIds(tcr).getIdentifiers();
        assertEquals(4, result.size());

        tcr.addCriterion(TestCriteria.ID, Condition.LE,2);
        tcr.addOrderAsc(TestCriteria.ID);
        result = dataInvoker.getIds(tcr).getIdentifiers();
        assertEquals(2, result.size());
        Iterator<Identifier> iterator=result.iterator();
        assertEquals(1, ((TestId) iterator.next()).getMainId());
        assertEquals(2, ((TestId) iterator.next()).getMainId());

        tcr.cleanCriterion();

        tcr.addCriterion(TestCriteria.ID, Condition.LE,2);
        tcr.addOrderDesc(TestCriteria.ID);
        result = dataInvoker.getIds(tcr).getIdentifiers();
        assertEquals(2, result.size());
         iterator=result.iterator();
        assertEquals(2, ((TestId) iterator.next()).getMainId());
        assertEquals(1, ((TestId)iterator.next()).getMainId());

    }

    private static MainClass getTest(final int id, final String name, final int chId, final Date date, final int sch, final byte[] blob) {
        final MainClass t1 = new MainClass();
        t1.setMainId(id);
        t1.setMainName(name);
        t1.setChildId(chId);
        t1.setSecondChildId(sch);
        t1.setMainDate(date);
        t1.setBlob(blob);
        return t1;
    }

    private static Child getChild(final int id, final String name) {
        final Child ch2 = new Child();
        ch2.setId(id);
        ch2.setName(name);
        return ch2;
    }

    @After
    public void tearDown() throws Exception {
        dataInvoker.close();
    }

}
