package com.nomad.cache.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.nomad.cache.test.model.Child;
import com.nomad.client.SimpleCacheClient;
import com.nomad.exception.ModelNotExistException;
import com.nomad.message.FullMessage;
import com.nomad.message.OperationStatus;
import com.nomad.model.BaseCommand;
import com.nomad.model.CommandPluginModelImpl;
import com.nomad.model.ConnectModelImpl;
import com.nomad.model.DataSourceModelImpl;
import com.nomad.model.Identifier;
import com.nomad.model.ListenerModelImpl;
import com.nomad.model.ServerModelImpl;
import com.nomad.model.ServiceCommand;
import com.nomad.model.StoreModel.ServerType;
import com.nomad.model.session.SessionClientModelImpl;
import com.nomad.model.session.SessionServerModelImp;
import com.nomad.pm.PmDataInvokerFactory;
import com.nomad.server.PmDataInvoker;
import com.nomad.server.ServerLauncher;

public class TestUnknownModel extends CommonTest {

    private static ServerLauncher translator;
    private static ServerLauncher launcherManager;
    private static ServerLauncher launcherCache1;
    private static ServerLauncher launcherCache2;
    private static ServerLauncher launcherCache3;

    private static int portTranslator = 2032;
    private static int port1 = 2132;
    private static int port2 = 2232;
    private static int port3 = 2332;
    private static int port4 = 2432;
    private static SimpleCacheClient cacheTranslator;
    private static SimpleCacheClient cacheManager;
    private static SimpleCacheClient cache1;
    private static SimpleCacheClient cache2;
    private static SimpleCacheClient cache3;
    private static PmDataInvoker dataInvoker;

    @Test
    public void test1() throws Exception {

        final Child char1 = getChildModel(1, "child1");
        FullMessage message = null;
        message = cacheTranslator.sendCommandForId(BaseCommand.DELETE, char1.getIdentifier(), null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        try {
            dataInvoker.getModel(char1.getIdentifier());
            fail("child model in database!");
        } catch (final ModelNotExistException e) {

        }
        message = cacheTranslator.sendCommandForModel(BaseCommand.PUT, char1, null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertNotNull(dataInvoker.getModel(char1.getIdentifier()));

        message = cacheManager.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE, char1.getIdentifier(), null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals(1, message.getBody().getResponse().getIdentifiers().size());

        message = cache1.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE, char1.getIdentifier(), null);
        assertEquals(OperationStatus.UNSUPPORTED_MODEL_NAME, message.getResult().getOperationStatus());

        message = cache2.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE, char1.getIdentifier(), null);
        assertEquals(OperationStatus.UNSUPPORTED_MODEL_NAME, message.getResult().getOperationStatus());

    }
    @Test
    public void test2() throws Exception {

        final com.nomad.cache.test.model.MainTestModel testModel = getNewTestModel(1, "test2");
        FullMessage message = null;

        message = cacheTranslator.sendCommandForId(BaseCommand.DELETE, testModel.getIdentifier(), null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        try {
            dataInvoker.getModel(testModel.getIdentifier());
            fail("Test model in database!");
        } catch (final ModelNotExistException e) {

        }
        message = cacheTranslator.sendCommandForModel(BaseCommand.PUT, testModel, null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertNotNull(dataInvoker.getModel(testModel.getIdentifier()));

        message = cacheManager.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE, testModel.getIdentifier(), null);
        assertEquals(OperationStatus.UNSUPPORTED_MODEL_NAME, message.getResult().getOperationStatus());

        int counter=0;

        message = cache1.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE, testModel.getIdentifier(), null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        counter += message.getBody().getResponse().getIdentifiers().size();

        message = cache2.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE, testModel.getIdentifier(), null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        counter += message.getBody().getResponse().getIdentifiers().size();

        message = cache3.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE, testModel.getIdentifier(), null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        counter += message.getBody().getResponse().getIdentifiers().size();

        assertEquals(2,counter);


    }

    @Test
    public void test3() throws Exception {
        final com.nomad.cache.test.model.MainTestModel testModel = getNewTestModel(1, "test2");
        FullMessage message = null;

        message = cacheTranslator.sendCommandForId(BaseCommand.DELETE, testModel.getIdentifier(), null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        try {
            dataInvoker.getModel(testModel.getIdentifier());
            fail("Test model in database!");
        } catch (final ModelNotExistException e) {
        }
        message =cacheTranslator.sendCommand(BaseCommand.START_NEW_SESSION);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        final String session=message.getHeader().getSessionId();


        message = cacheTranslator.sendCommandForModel(BaseCommand.PUT, testModel, session);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        try {
            dataInvoker.getModel(testModel.getIdentifier());
            fail("Test model in database!");
        } catch (final ModelNotExistException e) {
        }

        message = cacheManager.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE, testModel.getIdentifier(), null);
        assertEquals(OperationStatus.UNSUPPORTED_MODEL_NAME, message.getResult().getOperationStatus());

        int counter=0;

        test(2, testModel.getIdentifier());

        message =cacheTranslator.sendCommand(BaseCommand.COMMIT,session);
        test(2, testModel.getIdentifier());
        assertNotNull(dataInvoker.getModel(testModel.getIdentifier()));

        testModel.setName("changedTest");
        final String session2=    cacheTranslator.sendCommand(BaseCommand.START_NEW_SESSION).getHeader().getSessionId();
        message = cacheTranslator.sendCommandForModel(BaseCommand.PUT, testModel, session2);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        message =cacheTranslator.sendCommand(BaseCommand.COMMIT,session2);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        message = cacheTranslator.sendCommandForId(BaseCommand.GET, testModel.getIdentifier(), session);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals("changedTest", ((com.nomad.cache.test.model.MainTestModel) message.getBody().getResponse().getResultList().iterator().next()).getName());

        counter=0;
        com.nomad.cache.test.model.MainTestModel testModel1 = getTestFromCache(cache1, testModel.getIdentifier());

        if (testModel1 != null) {
            assertEquals("changedTest", testModel1.getName());
            counter++;
        }
        testModel1 = getTestFromCache(cache2, testModel.getIdentifier());
        if (testModel1 != null) {
            assertEquals("changedTest", testModel1.getName());
            counter++;
        }
        testModel1 = getTestFromCache(cache3, testModel.getIdentifier());
        if (testModel1 != null) {
            assertEquals("changedTest", testModel1.getName());
            counter++;
        }
        assertEquals(2, counter);

        testModel.setName("changedTest2");
        final String session3=    cacheTranslator.sendCommand(BaseCommand.START_NEW_SESSION).getHeader().getSessionId();
        message = cacheTranslator.sendCommandForModel(BaseCommand.PUT, testModel, session3);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        message =cacheTranslator.sendCommand(BaseCommand.ROLLBACK,session3);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());

        message = cacheTranslator.sendCommandForId(BaseCommand.GET, testModel.getIdentifier(), session);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        assertEquals("changedTest", ((com.nomad.cache.test.model.MainTestModel) message.getBody().getResponse().getResultList().iterator().next()).getName());
        counter=0;
        testModel1 = getTestFromCache(cache1, testModel.getIdentifier());
        if (testModel1 != null) {
            assertEquals("changedTest", testModel1.getName());
            counter++;
        }
        testModel1 = getTestFromCache(cache2, testModel.getIdentifier());
        if (testModel1 != null) {
            assertEquals("changedTest", testModel1.getName());
            counter++;
        }
        testModel1 = getTestFromCache(cache3, testModel.getIdentifier());
        if (testModel1 != null) {
            assertEquals("changedTest", testModel1.getName());
            counter++;
        }
        assertEquals(2, counter);


    }
    private void test(final int i, final Identifier id) throws Exception {
        int counter=0;
        FullMessage message = cache1.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE, id, null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        counter += message.getBody().getResponse().getIdentifiers().size();

        message = cache2.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE, id, null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        counter += message.getBody().getResponse().getIdentifiers().size();

        message = cache3.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE, id, null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        counter += message.getBody().getResponse().getIdentifiers().size();

        assertEquals(i,counter);
    }
    private com.nomad.cache.test.model.MainTestModel getTestFromCache(final SimpleCacheClient cache , final Identifier id) throws Exception{
        final FullMessage message = cache.sendCommandForId(ServiceCommand.GET_FROM_CACHE, id, null);
        assertEquals(OperationStatus.OK, message.getResult().getOperationStatus());
        if (message.getBody().getResponse().getResultList().size() > 0) {
            return (com.nomad.cache.test.model.MainTestModel) message.getBody().getResponse().getResultList().iterator().next();
        }
        return null;
    }
    @BeforeClass
    public static void setUp() throws Exception {
        registerSerialized();

        final SessionServerModelImp sessionServer = getSessionServerModel(host, 5888, 25);

        final SessionClientModelImpl sessionClient = getSessionClientModel(host, 5888, 5);


        final DataSourceModelImpl dataSourceModel = new DataSourceModelImpl();
        dataSourceModel.setName("a");
        dataSourceModel.setThreads(10);
        dataSourceModel.setTimeOut(1000);
        dataSourceModel.setClazz("com.nomad.cache.test.userdataaccess.BaseDataInvoker");
        dataSourceModel.addProperty("user", "sa");
        dataSourceModel.addProperty("password", "");
        dataSourceModel.addProperty("url", "jdbc:hsqldb:hsql://localhost/test");
        dataSourceModel.addProperty("driver", "org.hsqldb.jdbcDriver");

        final ServerModelImpl cacheTranslatorModel = new ServerModelImpl();
        // serverModel.getStoreModels().addAll(models1);
        cacheTranslatorModel.getListeners().add(getListenerModel(host,portTranslator, 10));
        cacheTranslatorModel.setManagementServerModel(getManagementServerModel(2042, host, 10, 2000));
        cacheTranslatorModel.setServerName("translator");
        cacheTranslatorModel.setCalculateStatistic(false);
        cacheTranslatorModel.setTrustSessions(true);

        translator = new ServerLauncher(cacheTranslatorModel);
        translator.start();

        final ServerModelImpl cacheManagerModel = new ServerModelImpl();
        cacheManagerModel.getStoreModels().add(getStoreData("Child", "com.nomad.cache.test.model.Child",ServerType.CACHE_CACHE_MANAGER));
        cacheManagerModel.getStoreModels().add(getStoreData("MainTestModel", "com.nomad.cache.test.model.MainTestModel",ServerType.CACHE_MANAGER));
        final ListenerModelImpl listener = getListenerModel(host, port1, 30);
        cacheManagerModel.getListeners().add(listener);
        cacheManagerModel.setManagementServerModel(getManagementServerModel(2142, host, 10, 2000));
        cacheManagerModel.setServerName("cache manager");
        cacheManagerModel.getProperties().setProperty("SessionStorePath", "/opt/t1/sessionstore/1");
        cacheManagerModel.getSessionClientModels().add(sessionClient);
        cacheManagerModel.setSessionServerModel(sessionServer);
        cacheManagerModel.getSaveClientModels().add(getSaveClientModel(host,5224, 5));
        cacheManagerModel.setCalculateStatistic(false);


        cacheManagerModel.addDataSources(dataSourceModel);
        final ConnectModelImpl connectModel = getConnectModel(cacheManagerModel, cacheTranslatorModel, 10, listener);

        cacheManagerModel.getClients().add(connectModel);

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
        proxyPlugin.setTimeout(60);

        cacheManagerModel.getCommandPlugins().add(plugin);

        dataInvoker = PmDataInvokerFactory.getDataInvoker("b", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:hsql://localhost/test", "sa", "", null, 1);

        launcherManager = new ServerLauncher(cacheManagerModel);

        launcherManager.start();

        final ServerModelImpl serverModel2 = new ServerModelImpl();
        serverModel2.getStoreModels().add(getStoreData("MainTestModel", "com.nomad.cache.test.model",ServerType.CACHE));
        final ListenerModelImpl listener2= getListenerModel(host,port2, 6);
        serverModel2.getListeners().add(listener2);
        serverModel2.setManagementServerModel(getManagementServerModel(2242, host, 2, 200));
        serverModel2.setServerName("second");
        serverModel2.setCalculateStatistic(false);

        serverModel2.addDataSources(dataSourceModel);

        serverModel2.getCommandPlugins().add(plugin);
        serverModel2.getProperties().setProperty("SessionStorePath", "/opt/t1/sessionstore/2");
        serverModel2.getSaveServerModels().add(getSaveServerModel(host,5224, 25));
        serverModel2.getSaveClientModels().add(getSaveClientModel(host,5224, 5));

        final ConnectModelImpl connectModel2 = getConnectModel(serverModel2, cacheManagerModel, 5, listener2);

        serverModel2.getClients().add(connectModel2);
        serverModel2.setCalculateStatistic(false);
        serverModel2.setTrustSessions(true);


        launcherCache1 = new ServerLauncher(serverModel2);

        launcherCache1.start();

        final ServerModelImpl serverModel3 = new ServerModelImpl();
        serverModel3.getStoreModels().add(getStoreData("MainTestModel", "com.nomad.cache.test.model",ServerType.CACHE));
        final ListenerModelImpl listener3=getListenerModel(host,port3, 6);
        serverModel3.getListeners().add(listener3);
        serverModel3.setManagementServerModel(getManagementServerModel(2342, host, 2, 2000));
        serverModel3.setServerName("second");

        serverModel3.addDataSources(dataSourceModel);

        serverModel3.getCommandPlugins().add(plugin);
        serverModel3.getProperties().setProperty("SessionStorePath", "/opt/t1/sessionstore/3");
        serverModel3.getSaveClientModels().add(getSaveClientModel(host,5224, 5));
        serverModel3.setCalculateStatistic(false);

        final ConnectModelImpl connectModel3 = getConnectModel(serverModel3, cacheManagerModel, 5, listener3);

        serverModel3.getClients().add(connectModel3);
        serverModel3.setCalculateStatistic(false);
        serverModel3.setTrustSessions(true);

        launcherCache2 = new ServerLauncher(serverModel3);

        launcherCache2.start();

        //------
        final ServerModelImpl serverModel4 = new ServerModelImpl();
        serverModel4.getStoreModels().add(getStoreData("MainTestModel", "com.nomad.cache.test.model",ServerType.CACHE));
        final ListenerModelImpl listener4=getListenerModel(host,port4, 6);
        serverModel4.getListeners().add(listener4);
        serverModel4.setManagementServerModel(getManagementServerModel(2442, host, 2, 2000));
        serverModel4.setServerName("second");

        serverModel4.addDataSources(dataSourceModel);

        serverModel4.getCommandPlugins().add(plugin);
        serverModel4.getProperties().setProperty("SessionStorePath", "/opt/t1/sessionstore/3");
        serverModel4.getSaveClientModels().add(getSaveClientModel(host,5224, 5));
        serverModel4.setCalculateStatistic(false);
        serverModel4.setTrustSessions(true);

        final ConnectModelImpl connectModel4 = getConnectModel(serverModel4, cacheManagerModel, 5, listener4);

        serverModel4.getClients().add(connectModel4);

        launcherCache3 = new ServerLauncher(serverModel4);

        launcherCache3.start();

        //------
        cacheTranslator = new SimpleCacheClient(host, portTranslator);
        cacheManager = new SimpleCacheClient(host, port1);
        cache1 = new SimpleCacheClient(host, port2);
        cache2 = new SimpleCacheClient(host, port3);
        cache3 = new SimpleCacheClient(host, port4);

    }

    @AfterClass
    public static void down() throws Exception {
        cacheTranslator.close();
        cacheManager.close();
        cache1.close();
        cache2.close();
        cache3.close();
        dataInvoker.close();

        translator.stop();
        launcherManager.stop();
        launcherCache1.stop();
        launcherCache2.stop();
        launcherCache3.stop();

    }

}
