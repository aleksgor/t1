package com.nomad.core;

import static org.junit.Assert.*;

import java.io.File;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;

import com.nomad.exception.ModelNotExistException;
import com.nomad.exception.SystemException;
import com.nomad.server.service.idgenerator.IdGeneratorModel;
import com.nomad.server.service.idgenerator.IdGeneratorModelId;
import com.nomad.server.service.idgenerator.SystemDataInvoker;
import com.nomad.util.DataInvokerPoolImpl;
import com.nomad.utility.DataInvokerPool;
import com.nomad.utility.PooledDataInvoker;

public class SystemDataInvokerTest {

    @org.junit.Test
    public void testServerModelSerialized() throws Exception {
        SystemDataInvoker sDataInvoker = new SystemDataInvoker();
        Properties properties = new Properties();
        properties.put(SystemDataInvoker.BASE_PATH_NAME, "base");

        sDataInvoker.init(properties, null, null);
        IdGeneratorModel model = new IdGeneratorModel();
        model.setIdentifier(new IdGeneratorModelId("key1"));
        model.setValue(new BigInteger("5"));
        sDataInvoker.addModel(Arrays.asList(model));
        File f = new File("base" + File.separator + "key1");
        assertTrue(f.getAbsolutePath(), f.exists());
        assertEquals(f.getAbsolutePath(), 1, f.length());

        model.setValue(new BigInteger("2334"));
        sDataInvoker.addModel(Arrays.asList(model));
        assertTrue(f.getAbsolutePath(), f.exists());
        assertEquals(f.getAbsolutePath(), 4, f.length());

        model.setValue(new BigInteger("2334444"));
        sDataInvoker.updateModel(Arrays.asList(model));
        assertTrue(f.getAbsolutePath(), f.exists());
        assertEquals(f.getAbsolutePath(), 7, f.length());

        model = sDataInvoker.getModel(new IdGeneratorModelId("key1"));
        assertEquals(f.getAbsolutePath(), 2334444, model.getValue().intValue());

        int counter = sDataInvoker.eraseModel(Arrays.asList(new IdGeneratorModelId("key1")));
        assertEquals(f.getAbsolutePath(), 1, counter);
        assertFalse(f.getAbsolutePath(), f.exists());

    }

    @org.junit.Test
    public void MultiThreadTest() throws Exception {
        SystemDataInvoker dataInvoker = new SystemDataInvoker();
        Properties properties = new Properties();
        properties.put(SystemDataInvoker.BASE_PATH_NAME, "base");

        dataInvoker.init(properties, null, null);
        int counter = 40;
        int attempt = 100;
        Thread[] threads = new Thread[counter];
        for (int i = 0; i < counter; i++) {
            threads[i] = new Thread(new T(attempt, dataInvoker));
        }
        for (int i = 0; i < counter; i++) {
            threads[i].start();
        }
        while (!checkStop(threads)) {
            Thread.sleep(200);
        }
    }

    private boolean checkStop(Thread[] threads) {
        for (Thread thread : threads) {
            if (thread.isAlive()) {
                return false;
            }
        }
        return true;
    }

    private class T implements Runnable {
        private final int counter;
        private final SystemDataInvoker dataInvoker;

        public T(int counter, SystemDataInvoker dataInvoker) {
            this.counter = counter;
            this.dataInvoker = dataInvoker;
        }

        @Override
        public void run() {
            for (int i = 0; i < counter; i++) {
                IdGeneratorModel model = new IdGeneratorModel();
                model.setIdentifier(new IdGeneratorModelId("key" + i % 4));
                model.setValue(new BigInteger("" + i));
                try {
                    dataInvoker.addModel(Arrays.asList(model));
                    model = dataInvoker.getModel(new IdGeneratorModelId("key" + i % 4));
                    assertNotNull(model);
                    Thread.sleep(2);
                } catch (SystemException e) {
                    e.printStackTrace();
                    fail();
                } catch (ModelNotExistException e) {
                    e.printStackTrace();
                    fail();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    fail();
                }
            }
        }
    }

    private class T1 implements Runnable {
        private final int counter;
        private final DataInvokerPool pool;

        public T1(int counter, DataInvokerPool pool) {
            this.counter = counter;
            this.pool = pool;
        }

        @Override
        public void run() {
            for (int i = 0; i < counter; i++) {
                IdGeneratorModel model = new IdGeneratorModel();
                model.setIdentifier(new IdGeneratorModelId("key" + i % 4));
                model.setValue(new BigInteger("" + i));
                PooledDataInvoker invoker = null;
                try {
                    invoker = pool.getObject();
                    invoker.addModel(Arrays.asList(model));
                    model = (IdGeneratorModel) invoker.getModel(new IdGeneratorModelId("key" + i % 4));

                    assertNotNull(model);
                    Thread.sleep(2);
                } catch (SystemException e) {
                    e.printStackTrace();
                    fail();
                } catch (ModelNotExistException e) {
                    e.printStackTrace();
                    fail();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    fail();
                } finally {
                    invoker.freeObject();
                }

            }
        }

    }

    @Test
    public void testPool() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put(SystemDataInvoker.BASE_PATH_NAME, "base");
        final DataInvokerPool pool = new DataInvokerPoolImpl(40, 0, "com.nomad.server.service.idgenerator.SystemDataInvoker", properties, null, "system");

        int counter = 40;
        int attempt = 100;
        Thread[] threads = new Thread[counter];
        for (int i = 0; i < counter; i++) {
            threads[i] = new Thread(new T1(attempt, pool));
        }
        for (int i = 0; i < counter; i++) {
            threads[i].start();
        }
        while (!checkStop(threads)) {
            Thread.sleep(200);
        }
    }

}
