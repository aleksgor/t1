package com.nomad.cache.test;


import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.cache.client.CommonTest;
import com.nomad.cache.commonclientserver.BodyImpl;
import com.nomad.cache.test.model.MainTestModel;
import com.nomad.cache.test.model.MainTestModelId;
import com.nomad.client.SimpleCacheClient;
import com.nomad.message.FullMessage;
import com.nomad.message.OperationStatus;
import com.nomad.model.BaseCommand;
import com.nomad.model.Model;

public class TestForLongStart extends CommonTest {
    protected static Logger LOGGER = LoggerFactory.getLogger(TestForLongStart.class);

    private final AtomicLong commonCounter = new AtomicLong(0);
    private final int port=2022;
    private static String host="localhost";

    @SuppressWarnings("unused")
    public static void main(final String[] args) throws Exception {

        final TestForLongStart test= new TestForLongStart();
        host = InetAddress.getLocalHost().getHostName();
        test.test1(3);
        test.test1(4);
        //    test.test1(2);
        final SimpleCacheClient client= new SimpleCacheClient(host, 2022);
        final SimpleCacheClient client1= new SimpleCacheClient(host, 2022);
        final SimpleCacheClient client2= new SimpleCacheClient(host, 2022);
        final SimpleCacheClient client3= new SimpleCacheClient(host, 2022);
        final SimpleCacheClient client4= new SimpleCacheClient(host, 2022);

        Thread.sleep(10000000);


    }



    public void test1(final int count ) throws Exception {

        final long start = System.currentTimeMillis();
        final EndLessThread[] threads = new EndLessThread[count];
        final Thread[] t = new Thread[count];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new EndLessThread(i * 100000);
        }
        for (int i = 0; i < threads.length; i++) {
            t[i] = new Thread(threads[i]);
            t[i].start();
        }
        Thread.sleep(1000 * 2 * 1);
        for (int i = 0; i < threads.length; i++) {
            threads[i].stop();
        }
        for (int i = 0; i < threads.length; i++) {
            while(!threads[i].isStop()){
                Thread.sleep(50);
            }
        }
        System.out.println("finish time: " + (System.currentTimeMillis() - start) + "counter:" + commonCounter.get());

    }

    public void test2(final int count) throws Exception {

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


        final SimpleCacheClient client = new SimpleCacheClient(host, port);
        client.sendCommandForModel(BaseCommand.PUT, tests, null);

        final long start = System.currentTimeMillis();
        final EndLessThreadForId[] threads = new EndLessThreadForId[count];
        final Thread[] t = new Thread[count];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new EndLessThreadForId(1, 10);
        }
        for (int i = 0; i < threads.length; i++) {
            t[i] = new Thread(threads[i]);
            t[i].start();
        }
        Thread.sleep(1000 * 60 * 1);
        for (int i = 0; i < threads.length; i++) {

            threads[i].stop();
        }
        System.out.println("finish time: " + (System.currentTimeMillis() - start) + "counter:" + commonCounter.get());

    }

    class EndLessThread implements Runnable {

        private boolean stop = false;
        private long counter = 0;
        private int startPosition = 0;
        private final SimpleCacheClient client;
        private volatile boolean active = true;


        EndLessThread(final int startPosition) throws Exception {
            client = new SimpleCacheClient(host, port);
            this.startPosition = startPosition;
        }

        @Override
        @SuppressWarnings("unused")
        public void run() {
            System.out.println("Start!");
            active = false;
            while (!stop) {
                counter++;
                final MainTestModel testModel = new MainTestModel();
                // System.out.println("put:" + (counter + startPosition));
                testModel.setId(counter + startPosition);
                testModel.setName("name sajdaksjdjsdhaf,hdfv,ashdvf,ashvd,sahvcsajhdvlashdalsdfhgalskdjfasldfjaslhdgfalshfglasdKJN.NCQEWKCQWUCCCVJDHASHDCASJSHDLULULWUECKHDCSDHAJH"
                        + (counter + startPosition));
                testModel.setDate(new Date());
                try {
                    FullMessage fullMessage = client.sendCommandForModel(BaseCommand.PUT, testModel, null);

                    final BodyImpl message = (BodyImpl) fullMessage.getBody();
                    if (!OperationStatus.OK.equals(fullMessage.getResult().getOperationStatus())) {
                        System.out.println("Error in send:" + (counter + startPosition));
                    }
                    fullMessage = client.sendCommandForModel(BaseCommand.COMMIT, testModel, null);

                    if (!OperationStatus.OK.equals(fullMessage.getResult().getOperationStatus())) {
                        System.out.println("Error in commit :" + (counter + startPosition));
                    }
                    commonCounter.incrementAndGet();
                } catch (final Exception e) {

                }
            }
            System.out.println("Stop!");
            try {
                client.close();
            } catch (final Exception e) {

                LOGGER.error(e.getMessage(),e);
            }
            active = true;

        }

        public void stop() {
            stop = true;
        }
        public boolean isStop() {
            return active;
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
                client = new SimpleCacheClient(host, port);
                this.startPosition = startPosition;
                counter=startPosition;
                this.endPosition=endPosition;
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(),e);
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

                    }

                    commonCounter.incrementAndGet();
                } catch (final Exception e) {

                }
            }
            try {
                client.close();
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(),e);
            }
        }

        public void stop() {
            stop = true;
        }
    }
}
