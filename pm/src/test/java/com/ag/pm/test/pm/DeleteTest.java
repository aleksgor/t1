package com.nomad.pm.test.pm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.nomad.exception.ModelNotExistException;
import com.nomad.pm.PmDataInvokerFactory;
import com.nomad.server.PmDataInvoker;
import com.nomad.pm.test.criteria.ChildCriteria;
import com.nomad.pm.test.criteria.TestCriteria;
import com.nomad.pm.test.models.Child;
import com.nomad.pm.test.models.MainClass;
import com.nomad.pm.test.models.TestId;


@RunWith(Parameterized.class)
public class DeleteTest {
    private String driver;
    private String url;
    private String user;
    private String password;
    private String configurationFile;
    private String dataSource;

    private PmDataInvoker dataInvoker;

    public  DeleteTest(String dataSource, String driver, String url, String user,String password, String cfgFile) {
        this.driver=driver;
        this.url=url;
        this.user=user;
        this.password=password;
        this.configurationFile=cfgFile;
        this.dataSource=dataSource;
    }

    @Parameters
    @org.junit.Test
    public void test1() throws Exception {
        setUp( );
        MainClass model1;
        model1 = (MainClass) dataInvoker.getModel(new TestId(1));
        assertNotNull(model1 );
        assertEquals("11",model1.getMainName());
        assertEquals(1,model1.getMainId() );
        assertEquals(1,model1.getChildId() );

        dataInvoker.eraseModel( Arrays.asList(model1.getIdentifier()));
        try{
            model1 = (MainClass) dataInvoker.getModel(new TestId(1));
            fail();
        }catch(final ModelNotExistException e){

        }

    }

    @Parameterized.Parameters
    public  static Collection<Object[]> parametersForTests() {
        return Arrays.asList(new Object[][] {
                {"a",null, "jdbc:postgresql://localhost:5432/test", "test", "test", "pm.cfg.xml"},
                {"b",null, "jdbc:mysql://localhost:3306/test?characterEncoding=utf8", "test", "test", "pm.cfg.xml"}
        });
    }

    @Before
    public  void setUp( ) throws Exception {


        dataInvoker = PmDataInvokerFactory.getDataInvoker(dataSource, driver, url, user, password, configurationFile, 1);
        dataInvoker.eraseModel(new TestCriteria());

        dataInvoker.eraseModel(new ChildCriteria());
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
        dataInvoker.addModel(Collections.singletonList(ch1));
        final Child ch2 = new Child();
        ch2.setId(2);
        ch2.setName("child2");
        dataInvoker.addModel(Collections.singletonList(ch2));

    }

    @After
    public void tearDown() throws Exception {
        dataInvoker.close();
    }

}
