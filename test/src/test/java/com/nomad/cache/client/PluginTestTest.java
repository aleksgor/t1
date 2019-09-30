package com.nomad.cache.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

import com.nomad.cache.test.model.MainTestModelId;
import com.nomad.client.SimpleCacheClient;
import com.nomad.exception.SystemException;
import com.nomad.message.FullMessage;
import com.nomad.message.OperationStatus;
import com.nomad.model.CommandPluginModelImpl;
import com.nomad.model.DataSourceModelImpl;
import com.nomad.model.ListenerModelImpl;
import com.nomad.model.ServerModelImpl;
import com.nomad.model.StoreModelImpl;
import com.nomad.model.session.SessionServerModel;
import com.nomad.server.ServerLauncher;

public class PluginTestTest extends CommonTest {

    private static ServerLauncher launcher;

    private static int port = 2222;
    private static SimpleCacheClient client;

    /*
     * test session isolation
     */
    @org.junit.Test
    public void test1() throws Exception {
        Thread.sleep(1000);
        FullMessage message = client.sendCommandForId("getId", new MainTestModelId());
        final MainTestModelId id1 = (MainTestModelId) message.getBody().getResponse().getIdentifiers().iterator().next();

        message = client.sendCommandForId("getId", new MainTestModelId(), null);
        final MainTestModelId id2 = (MainTestModelId) message.getBody().getResponse().getIdentifiers().iterator().next();

        assertEquals((id1.getId() + 1), id2.getId());

    }

    @org.junit.Test(timeout = 25000)
    public void testMultiThread() throws Exception {
        final EndLessThread[] threads = new EndLessThread[8];
        final Thread[] t = new Thread[threads.length];
        final HashSet<Long> results = new HashSet<>();
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new EndLessThread(results);
        }
        for (int i = 0; i < threads.length; i++) {
            t[i] = new Thread(threads[i]);
            t[i].setName("test testMultithread" + i);
            t[i].start();
        }
        Thread.sleep(1000 * 20);
        for (final EndLessThread thread : threads) {
            thread.stop();
        }
    }

    class EndLessThread implements Runnable {

        private boolean stop = false;
        private volatile HashSet<Long> results;
        private final SimpleCacheClient client;

        EndLessThread(final HashSet<Long> in) throws Exception {
            results = in;
            client = new SimpleCacheClient(host, port);
        }

        @Override
        public void run() {
            while (!stop) {
                FullMessage message;
                try {
                    message = client.sendCommandForId("getId", new MainTestModelId(3));
                    final MainTestModelId id1 = (MainTestModelId) message.getBody().getResponse().getIdentifiers().iterator().next();
                    if (!OperationStatus.OK.equals(message.getResult().getOperationStatus())) {
                        fail();
                    }

                    synchronized (results) {
                        if (results.contains(id1.getId())) {
                            Assert.fail();
                        }
                        results.add(id1.getId());
                    }

                } catch (final SystemException e) {
                    if (!stop) {
                        LOGGER.warn(e.getMessage());
                    }
                } catch (final Exception e) {
                    if (!stop) {
                        LOGGER.error(e.getMessage(), e);
                        fail();
                    }
                }

            }
            client.close();
        }

        public void stop() {
            stop = true;
        }
    }

    @BeforeClass
    public static void setUp() throws Exception {

        commonSetup();

        // launcher.start();

        final ListenerModelImpl listener = new ListenerModelImpl();
        listener.setPort(2222);
        listener.setMinThreads(10);
        listener.setMaxThreads(10);
        listener.setBacklog(10);
        final List<StoreModelImpl> models = new ArrayList<>();

        final SessionServerModel sessionServer = getSessionServerModel("", 0, 0);

        models.add(getStoreData("MainTestModel", "com.nomad.cache.test.model.MainTestModel"));
        models.add(getStoreData("Child", "com.nomad.cache.test.model.Child"));

        final ServerModelImpl serverModel = new ServerModelImpl();
        serverModel.setManagementServerModel(getManagementServerModel(2224, host, 2, 2000));
        serverModel.getStoreModels().addAll(models);
        serverModel.getListeners().add(listener);
        serverModel.setSessionServerModel(sessionServer);
        final DataSourceModelImpl dataSource = new DataSourceModelImpl();
        dataSource.setName("a");
        dataSource.setThreads(12);
        dataSource.setTimeOut(10000);
        dataSource.setClazz("com.nomad.cache.test.userdataaccess.BaseDataInvoker");
        dataSource.addProperty("user", "sa");
        dataSource.addProperty("password", "");
        dataSource.addProperty("url", "jdbc:hsqldb:hsql://localhost/test");
        dataSource.addProperty("driver", "org.hsqldb.jdbcDriver");
        serverModel.addDataSources(dataSource);
        serverModel.setCalculateStatistic(false);

        final CommandPluginModelImpl plugin = new CommandPluginModelImpl();
        plugin.setCheckDelay(10);
        plugin.setClazz("com.nomad.plugin.IdGenerator");
        plugin.setPoolSize(10);
        plugin.getProperties().put("DataSourceName", "a");
        plugin.setTimeout(10);

        final CommandPluginModelImpl proxyPlugin = new CommandPluginModelImpl();
        proxyPlugin.setClazz("com.nomad.plugin.IdGeneratorProxy");
        proxyPlugin.setCheckDelay(10);
        proxyPlugin.setPoolSize(10);
        proxyPlugin.setTimeout(10);

        serverModel.getCommandPlugins().add(plugin);

        launcher = new ServerLauncher(serverModel);

        launcher.start();

        client = new SimpleCacheClient(host, port);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        close(client);
        launcher.stop();

    }

}
