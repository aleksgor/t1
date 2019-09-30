package com.nomad.cache.test;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.cache.commonclientserver.BodyImpl;
import com.nomad.cache.test.model.MainTestModel;
import com.nomad.cache.test.model.MainTestModelId;
import com.nomad.client.SimpleCacheClient;
import com.nomad.exception.SystemException;
import com.nomad.message.FullMessage;
import com.nomad.message.OperationStatus;
import com.nomad.model.BaseCommand;
import com.nomad.model.server.ProtocolType;
import com.nomad.server.ServerLauncher;

public class ObjectTest {
    protected static Logger LOGGER = LoggerFactory.getLogger(ObjectTest.class);

    private final String host = "agorshkov-ws.lupus.griddynamics.net";
    private final int port = 2222;
    private final AtomicLong counter = new AtomicLong(0);

    /**
     * @param args
     */
    public static void main(final String[] args) {
        final ObjectTest client = new ObjectTest();
        try {
            client.registerSerialized();
        } catch (final ClassNotFoundException e) {
            LOGGER.error(e.getMessage(),e);
        }
        // client.multithreadTest(5);
        // client.singleTest();
        // client.dbTest();
        // client.speedtest();
        // client.transactTest();
        client.DOSTest(4);
        System.exit(0);
    }

    public void DOSTest(final int count) {
        final EndLessThread[] threads = new EndLessThread[count];
        final Thread[] t = new Thread[count];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new EndLessThread(i * 100000);
        }
        for (int i = 0; i < threads.length; i++) {
            t[i] = new Thread(threads[i]);
            t[i].start();
        }
        try {
            Thread.sleep(1000 * 60 * 1);
        } catch (final InterruptedException e1) {
            LOGGER.error(e1.getMessage(),e1);
        }
        for (int i = 0; i < threads.length; i++) {

            threads[i].stop();
        }

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
                LOGGER.error(e.getMessage(),e);
            }
        }

        @Override
        @SuppressWarnings("unused")
        public void run() {
            System.out.println("Start!");
            while (!stop) {
                counter++;
                final MainTestModel testModel = new MainTestModel();
                System.out.println("put:" + (counter + startPosition));
                testModel.setId(counter + startPosition);
                testModel.setName("name sajdaksjdjsdhaf,hdfv,ashdvf,ashvd,sahvcsajhdvlashdalsdfhgalskdjfasldfjaslhdgfalshfglasdKJN.NCQEWKCQWUCCCVJDHASHDCASJSHDLULULWUECKHDCSDHAJH"
                        + (counter + startPosition));
                testModel.setDate(new Date());
                try {
                    FullMessage fullMessage = client.sendCommandForModel(BaseCommand.PUT, testModel, null);
                    System.out.println("fullMessage:" + fullMessage);

                    final BodyImpl message = (BodyImpl) fullMessage.getBody();
                    if (! OperationStatus.OK.equals(fullMessage.getResult().getOperationStatus())) {
                        System.out.println("Error in send:" + (counter + startPosition));
                    }
                    fullMessage = client.sendCommandForModel(BaseCommand.COMMIT, testModel, null);

                    if (! OperationStatus.OK.equals(fullMessage.getResult().getOperationStatus())) {
                        System.out.println("Error in commit	:" + (counter + startPosition));
                    }
                    ObjectTest.this.counter.incrementAndGet();
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

    public void multiThreadTest(final int count) {
        final long start = System.currentTimeMillis();
        final NewThread[] threads = new NewThread[count];
        final Thread[] t = new Thread[count];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new NewThread();
        }
        for (int i = 0; i < threads.length; i++) {
            t[i] = new Thread(threads[i]);
            t[i].start();
        }
        boolean finish = false;
        while (!finish) {
            for (int i = 0; i < count; i++) {
                finish = true;
                if (t[i].isAlive()) {
                    finish = false;
                }
                try {
                    Thread.sleep(5);
                } catch (final InterruptedException e) {
                    LOGGER.error(e.getMessage(),e);
                }
            }

        }

        System.out.println("finish time: " + (System.currentTimeMillis() - start));

    }

    public void singleTest() throws Exception {
        SimpleCacheClient client = null;
        try {
            client = new SimpleCacheClient(host, port,ProtocolType.TCP);
            final Object o = client.sendCommandForId(BaseCommand.GET, new MainTestModelId(3));
            System.out.println("o: " + o);
        } catch (final SystemException e) {
            LOGGER.error(e.getMessage(),e);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    public void dataBaseTest() throws Exception {
        SimpleCacheClient client = null;

        try {
            client = new SimpleCacheClient(host, port);

            final long time = System.currentTimeMillis() + (20 * 1000);
            int counter = 0;
            int j = 0;

            System.out.println("counter: " + counter);
            while (System.currentTimeMillis() < time) {
                // Object o =
                client.sendCommandForId(BaseCommand.GET, new MainTestModelId(1));
                counter++;
                if (j++ >= 100) {
                    System.out.print((System.currentTimeMillis() - time) + ":" + counter + "\n");
                    j = 0;
                }
            }
            System.out.println("finish counter: " + counter);

        } catch (final SystemException e) {
            LOGGER.error(e.getMessage(),e);
        } finally {
            if (client != null) {
                client.close();
            }
        }

    }

    public void speedTest() throws Exception {
        SimpleCacheClient client = null;

        try {

            final long time = System.currentTimeMillis() + (30 * 1000);
            int counter = 0;
            int j = 0;
            client = new SimpleCacheClient(host, port);

            System.out.println("counter: " + counter);
            while (System.currentTimeMillis() < time) {
                client.sendCommandForId(BaseCommand.GET, new MainTestModelId(1), null);
                counter++;
                System.out.print(".");
                if (j++ >= 100) {
                    System.out.print((System.currentTimeMillis() - time) + ":" + counter + "\n");
                    j = 0;
                }
            }
            System.out.println("finish counter: " + counter);
        } catch (final SystemException e) {
            LOGGER.error(e.getMessage(),e);
        } finally {
            if (client != null) {
                client.close();
            }
        }

    }

    class NewThread implements Runnable {
        private  SimpleCacheClient client ;

        NewThread() {
            try {
                client= new SimpleCacheClient(host, port);
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(),e);
            }
        }

        // This is the entry point for the second thread.
        @Override
        public void run() {
            System.out.println("Start!");
            for (int i = 5000; i > 0; i--) {
                try {
                    client.sendCommandForId(BaseCommand.GET, new MainTestModelId(1));
                } catch (final Exception e) {
                    LOGGER.error(e.getMessage(),e);
                }

            }
            client.close();
        }
    }

    public void transactTest() throws Exception {
        SimpleCacheClient client =null;

        try {
            client= new SimpleCacheClient(host, port);
            FullMessage o = client.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(3));
            client.sendCommand(BaseCommand.COMMIT, o.getHeader().getSessionId());

            final MainTestModel t = new MainTestModel();
            t.setId(3);
            t.setName("test3");
            o = client.sendCommandForModel(BaseCommand.PUT, t, null);
            final String session1 = o.getHeader().getSessionId();

            final String session2 = UUID.randomUUID().toString();
            o = client.sendCommandForId(BaseCommand.GET, new MainTestModelId(3), session2);
            System.out.println("o: " + o);
            o = client.sendCommandForId(BaseCommand.GET, new MainTestModelId(3), session1);
            System.out.println("o: " + o);

            o = client.sendCommand(BaseCommand.COMMIT, session1);
            System.out.println("o: " + o);
        } catch (final SystemException e) {
            LOGGER.error(e.getMessage(),e);
        }finally{
            if (client != null) {
                client.close();
            }

        }
    }

    ServerLauncher server;



    protected void registerSerialized() throws ClassNotFoundException {
    }
}
