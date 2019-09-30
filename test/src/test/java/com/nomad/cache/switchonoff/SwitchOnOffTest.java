package com.nomad.cache.switchonoff;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.nomad.StartFormXml;
import com.nomad.cache.client.CommonTest;
import com.nomad.model.InformationPublisherImpl;
import com.nomad.server.ServerLauncher;
import com.nomad.server.Status;
import com.nomad.server.mbean.StatusMXBean;
import com.nomad.server.statistic.InformationPublisher;
import com.nomad.server.statistic.JavaVMInformationImplMBean;
import com.nomad.server.statistic.JavaVMInformationMXBean;
import com.nomad.server.statistic.service.InformationPublisherServiceImpl;

public class SwitchOnOffTest extends CommonTest {

    private static StartFormXml starter;


    /*
     * test session isolation
     */
    @org.junit.Test
    public void testInformationPublisherServiceImpl() throws Exception {
        InformationPublisher model = new InformationPublisherImpl();
        model.setPort(9889);
        InformationPublisherServiceImpl publisher = new InformationPublisherServiceImpl(model);
        JavaVMInformationMXBean bean = new JavaVMInformationImplMBean();
        bean.setAvailableProcessors(1);
        bean.updateDate(2, 3, 4);
        publisher.publicData(bean, "sn", "type", "nmn");

        JavaVMInformationMXBean bean2 = new JavaVMInformationImplMBean();
        bean2.setAvailableProcessors(12);
        bean2.updateDate(10, 13, 14);
        publisher.publicData(bean2, "sn", "type", "nmn");
        JavaVMInformationMXBean revertedData = (JavaVMInformationMXBean) publisher.getData("sn", "type", "nmn");
        assertEquals(12, revertedData.getAvailableProcessors());
        assertEquals(10, revertedData.getFreeMemory());
        assertEquals(13, revertedData.getTotalMemory());
        assertEquals(14, revertedData.getMaxMemory());
        StatusMXBean status = (StatusMXBean) publisher.getData("cache1", "Status", "");
        assertEquals(Status.STARTED.name(), status.getStatus());
        ServerLauncher cache3 = starter.getLaunchers().get(3);
        cache3.stop();
        status = (StatusMXBean) publisher.getData("cache3", "Status", "");
        assertEquals(Status.SHUTDOWN.name(), status.getStatus());

    }


    @BeforeClass
    public static void setUp() throws Exception {

        commonSetup();
        final String[] files = { "configuration/cacheManager.xml", "configuration/cache1.xml", "configuration/cache2.xml", "configuration/cache3.xml" };
        starter = new StartFormXml();
        starter.startServers(files);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        starter.stop();

    }

}
