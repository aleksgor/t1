package com.nomad.pm.test.statistic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.nomad.model.criteria.AggregateFunction;
import com.nomad.model.criteria.StatisticResult;
import com.nomad.pm.PmDataInvokerFactory;
import com.nomad.server.PmDataInvoker;
import com.nomad.pm.test.criteria.TestCriteria;
import com.nomad.pm.test.models.MainClass;

@RunWith(Parameterized.class)
public class StatisticCountTest {
    private String driver;
    private String url;
    private String user;
    private String password;
    private String configurationFile;
    private String dataSource;

    private PmDataInvoker dataInvoker;

    public StatisticCountTest(String dataSource, String driver, String url, String user, String password, String configurationFile) {
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
        setUp( );
        TestCriteria criteria= new TestCriteria();
        criteria.addStatisticRequirement(null, null, AggregateFunction.COUNT);
        StatisticResult<MainClass> result = dataInvoker.getList(criteria);
        assertNotNull(result);
        assertNotNull(result.getResultList());
        assertEquals(2, result.getResultList().size());
        assertEquals(2, result.getCountAllRow());

        criteria.setStartPosition(0);
        criteria.setPageSize(1);
        result = dataInvoker.getList(criteria);
        assertNotNull(result);
        assertNotNull(result.getResultList());
        assertEquals(1, result.getResultList().size());
        assertEquals(2, result.getCountAllRow());


    }

    @Parameterized.Parameters
    public static Collection<Object[]> parametersForTests() {
        return Arrays.asList(new Object[][] {
                {"a",null, "jdbc:postgresql://localhost:5432/test", "test", "test", "pm.cfg.xml"},
                {"b",null, "jdbc:mysql://localhost:3306/test?characterEncoding=utf8", "test", "test", "pm.cfg.xml"}
        });
    }

    @Before
    public  void setUp( ) throws Exception {


        dataInvoker = PmDataInvokerFactory.getDataInvoker(dataSource, driver, url, user, password, configurationFile, 1);
        dataInvoker.eraseModel(new TestCriteria());

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
    }

    @After
    public void tearDown() throws Exception {
        dataInvoker.close();
    }

}
