package com.nomad.pm.test.pm;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Date;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.nomad.model.TransactInvoker;
import com.nomad.pm.PmDataInvokerFactory;
import com.nomad.pm.exception.SysPmException;
import com.nomad.pm.transactstore.TransactAndBlockThreadInvoker;
import com.nomad.server.PmDataInvoker;
import com.nomad.pm.test.criteria.ChildCriteria;
import com.nomad.pm.test.criteria.TestCriteria;
import com.nomad.pm.test.models.Child;
import com.nomad.pm.test.models.MainClass;
import com.nomad.pm.test.models.TestId;


public class BlockTest {
    private static TransactAndBlockThreadInvoker dataInvoker;
    private static TransactInvoker transactInvoker;

    @org.junit.Test
    public void test1() throws Exception {
        MainClass model1;
        model1 = (MainClass) dataInvoker.getModel(new TestId(1));
        assertTrue(model1 != null);
        assertTrue("11".equals(model1.getMainName()));
        assertTrue(model1.getMainId()==1);
        assertTrue(model1.getChildId()==1);

        dataInvoker.updateModel(Arrays.asList(model1));
        final String oldSessionName=dataInvoker.session;
        dataInvoker.session="ses1";
        try{
            dataInvoker.updateModel(Arrays.asList(model1));
            fail();
        }catch(final SysPmException e){

        }
        dataInvoker.session=oldSessionName;
        dataInvoker.commit(null);
        dataInvoker.updateModel(Arrays.asList(model1));
        dataInvoker.commit(null);



    }

    @org.junit.Test
    public void test2() throws Exception {
        MainClass model1;
        model1 = (MainClass) dataInvoker.getModel(new TestId(1));
        assertTrue(model1 != null);
        assertTrue("11".equals(model1.getMainName()));
        assertTrue(model1.getMainId()==1);
        assertTrue(model1.getChildId()==1);

        dataInvoker.getModel(new TestId(1));
        final String oldSessionName=dataInvoker.session;
        dataInvoker.session="ses1";
        try{
            dataInvoker.updateModel(Arrays.asList(model1));
            fail();
        }catch(final SysPmException e){

        }
        dataInvoker.session=oldSessionName;
        dataInvoker.commit(null);
        dataInvoker.updateModel(Arrays.asList(model1));
        dataInvoker.commit(null);


    }

    @BeforeClass
    public static void start() throws Exception {

        final PmDataInvoker internalDataInvoker = PmDataInvokerFactory.getDataInvoker("b", "org.postgresql.Driver",
                "jdbc:postgresql://localhost:5432/test", "test", "test","pm.cfg.xml",1);

        dataInvoker = new TransactAndBlockThreadInvoker(internalDataInvoker);
        transactInvoker = dataInvoker;

        dataInvoker.eraseModel(new TestCriteria());
        dataInvoker.eraseModel(new ChildCriteria());
        final MainClass t1= new MainClass();
        t1.setMainId(1);
        t1.setMainName("11");
        t1.setChildId(1);
        t1.setMainDate(new Date());
        
        final MainClass t2= new MainClass();
        t2.setMainId(2);
        t2.setMainName("22211");
        t2.setChildId(2);
        t2.setMainDate(new Date());
        dataInvoker.addModel(Arrays.asList(t1,t2));

        final Child ch1= new Child();
        ch1.setId(1);
        ch1.setName("child1");
        
        final Child ch2= new Child();
        ch2.setId(2);
        ch2.setName("child2");
        dataInvoker.addModel(Arrays.asList(ch1,ch2));
        transactInvoker.commit(null);
    }

    @AfterClass
    public static void Finish() throws Exception {
        dataInvoker.close();
    }

}
