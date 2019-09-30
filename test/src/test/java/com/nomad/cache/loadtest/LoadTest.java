package com.nomad.cache.loadtest;

import static org.junit.Assert.fail;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;

import com.nomad.StartFormXml;
import com.nomad.cache.client.CommonTest;
import com.nomad.cache.test.model.MainTestModel;
import com.nomad.cache.test.model.MainTestModelId;
import com.nomad.client.SimpleCacheClient;
import com.nomad.message.FullMessage;
import com.nomad.message.OperationStatus;
import com.nomad.model.BaseCommand;
import com.nomad.model.Model;
import com.nomad.model.server.ProtocolType;

@Ignore
public class LoadTest extends CommonTest {

    private static String host = "localhost";
    private final int port = 2032;
    private final int count = 4;
    private static StartFormXml starter;

    private final AtomicLong commonCounter = new AtomicLong(0);

    @BeforeClass
    public static void start() throws Exception {
        host = InetAddress.getLocalHost().getHostName();
        host="localhost";
        setRemoteJMXBean();
        registerSerialized();
        final String[] files = { "configuration/translator.xml", "configuration/cacheManager.xml", "configuration/cache1.xml", "configuration/cache2.xml",
        "configuration/cache3.xml" };
        starter = new StartFormXml();
        starter.startServers(files);
    }

    @AfterClass
    public static void stop() {
        starter.stop();
    }

    @org.junit.Test
    public void test1() throws Exception {

        final long start = System.currentTimeMillis();
        final EndLessThread[] threads = new EndLessThread[4];
        final Thread[] t = new Thread[count];
        for (int i = 0; i < count; i++) {
            threads[i] = new EndLessThread(i * 100000);
        }
        for (int i = 0; i < count; i++) {

            t[i] = new Thread(threads[i]);
            t[i].start();
            System.out.println("i:"+i+" started");
        }
        Thread.sleep(1000 * 6000 * 1);
        for (final EndLessThread thread : threads) {

            thread.stop();
        }
        System.out.println("finish time: " + (System.currentTimeMillis() - start) + "counter:" + commonCounter.get());

    }

    @org.junit.Test
    public void test2() throws Exception {

        final List<Model> tests=new ArrayList<>();
        tests.add(getNewTestModel(1, "cjhsdvslkbdjvlaksdjbvlakshbdvljashdbvlasd gvlasdjgvlsdgvlaisgdvlsigvlaisgvl"));
        tests.add(getNewTestModel(2, "cjhsdvslkbdjvlaksdjbvlakshbdvljashdbvlasd gvlasdjgvlsdgvlaisgdvlsigvlaisgvl"));
        tests.add(getNewTestModel(3, "cjhsdvslkbdjvlaksdjbvlakshbdvljashdbvlasd gvlasdjgvlsdgvlaisgdvlsigvlaisgvl"));
        tests.add(getNewTestModel(4, "cjhsdvslkbdjvlaksdjbvlakshbdvljashdbvlasd gvlasdjgvlsdgvlaisgdvlsigvlaisgvl"));
        tests.add(getNewTestModel(5, "cjhsdvslkbdjvlaksdjbvlakshbdvljashdbvlasd gvlasdjgvlsdgvlaisgdvlsigvlaisgvl"));
        tests.add(getNewTestModel(6, "cjhsdvslkbdjvlaksdjbvlakshbdvljashdbvlasd gvlasdjgvlsdgvlaisgdvlsigvlaisgvl"));
        tests.add(getNewTestModel(7, "cjhsdvslkbdjvlaksdjbvlakshbdvljashdbvlasd gvlasdjgvlsdgvlaisgdvlsigvlaisgvl"));
        tests.add(getNewTestModel(8, "cjhsdvslkbdjvlaksdjbvlakshbdvljashdbvlasd gvlasdjgvlsdgvlaisgdvlsigvlaisgvl"));
        tests.add(getNewTestModel(9, "cjhsdvslkbdjvlaksdjbvlakshbdvljashdbvlasd gvlasdjgvlsdgvlaisgdvlsigvlaisgvl"));
        tests.add(getNewTestModel(10, "cjhsdvslkbdjvlaksdjbvlakshbdvljashdbvlasd gvlasdjgvlsdgvlaisgdvlsigvlaisgvl"));


        final SimpleCacheClient client = new SimpleCacheClient(host, port,ProtocolType.TCP);
        client.sendCommandForModel(BaseCommand.PUT, tests, null);

        final long start = System.currentTimeMillis();
        final EndLessThreadForId[] threads = new EndLessThreadForId[4];
        final Thread[] t = new Thread[count];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new EndLessThreadForId(1, 10);
        }
        for (int i = 0; i < count; i++) {
            t[i] = new Thread(threads[i]);
            t[i].start();
        }
        Thread.sleep(1000 * 60 * 1);
        for (final EndLessThreadForId thread : threads) {

            thread.stop();
        }
        System.out.println("finish time: " + (System.currentTimeMillis() - start) + "counter:" + commonCounter.get());

    }

    class EndLessThread implements Runnable {

        private boolean stop = false;
        private long counter = 0;
        private int startPosition = 0;
        private SimpleCacheClient client;

        EndLessThread(final int startPosition) {
            try {
                client = new SimpleCacheClient(host, port,ProtocolType.TCP);
                this.startPosition = startPosition;
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        @Override
        public void run() {
            System.out.println("Start!");
            while (!stop) {
                counter++;
                final MainTestModel testModel = new MainTestModel();
                // System.out.println("put:" + (counter + startPosition));
                testModel.setId(counter + startPosition);
                testModel.setName("name sajdaksjdjsdhaf,hdfv,ashdvf,ashvd,sahvcsajhdvlashdalsdfhgalskdjfasldfjaslhdgfalshfglasdKJN.NCQEWKCQWUCCCVJDHASHDCASJSHDLULULWUECKHDCSDHAJH"
                        + (counter + startPosition));
                testModel.setDate(new Date());
                try {
                    final FullMessage fullMessage = client.sendCommandForModel(BaseCommand.PUT, testModel, null);

                    fullMessage.getBody();
                    if (!OperationStatus.OK.equals(fullMessage.getResult().getOperationStatus())) {
                        System.out.println("Error in send:" + (counter + startPosition));
                    }
                    Thread.sleep(100);
                    commonCounter.incrementAndGet();
                } catch (final Exception e) {

                }
            }
            try {
                client.close();
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        public void stop() {
            stop = true;
        }
    }

    class EndLessThreadForId implements Runnable {

        private boolean stop = false;
        private long counter = 0;
        private int startPosition = 0;
        private SimpleCacheClient client;
        private int endPosition;

        EndLessThreadForId(final int startPosition, final int endPosition) {
            try {
                client = new SimpleCacheClient(host, port,ProtocolType.TCP);
                this.startPosition = startPosition;
                counter=startPosition;
                this.endPosition=endPosition;
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        @Override
        public void run() {
            System.out.println("Start!");
            while (!stop) {
                counter++;
                if(counter>endPosition){
                    counter=startPosition;
                }
                try {

                    final FullMessage fullMessage = client.sendCommandForId(BaseCommand.GET, new MainTestModelId(counter), null);
                    if (!OperationStatus.OK.equals(fullMessage.getResult().getOperationStatus())) {
                        System.out.println("Error in commit :" + (counter ));
                        fail();
                    }

                    commonCounter.incrementAndGet();
                } catch (final Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
            try {
                client.close();
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        public void stop() {
            stop = true;
        }
    }
}
