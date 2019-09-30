package com.nomad.cache.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;

import com.nomad.cache.test.model.Child;
import com.nomad.cache.test.model.ChildId;
import com.nomad.cache.test.model.MainTestModel;
import com.nomad.cache.test.model.MainTestModelId;
import com.nomad.client.SimpleCacheClient;
import com.nomad.message.FullMessage;
import com.nomad.message.OperationStatus;
import com.nomad.model.BaseCommand;
import com.nomad.model.CommandPluginModelImpl;
import com.nomad.model.ConnectModelImpl;
import com.nomad.model.DataSourceModelImpl;
import com.nomad.model.ListenerModelImpl;
import com.nomad.model.ModelSource;
import com.nomad.model.ServerModelImpl;
import com.nomad.model.ServiceCommand;
import com.nomad.model.StoreModel.ServerType;
import com.nomad.model.StoreModelImpl;
import com.nomad.server.ServerLauncher;

public class ThroughPutTest extends CommonTest {

    private static int port = 2222;

    private static ServerLauncher launcher;
    private static ServerLauncher launcher2;
    private static SimpleCacheClient client;

    @org.junit.Test
    public void testThroughPut() throws Exception {
        final SimpleCacheClient client2 = new SimpleCacheClient(host, 2432);

        final Child child = new Child();
        child.setId(2);
        child.setName("childname");

        final MainTestModel test2 = new MainTestModel();
        test2.setId(2);
        test2.setName("test2");

        final MainTestModel test3 = new MainTestModel();
        test3.setId(3);
        test3.setName("test3");

        client.sendCommandForId(BaseCommand.DELETE, new ChildId(2L));

        FullMessage answer = client.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE, new ChildId(2L));
        assertEquals(OperationStatus.UNSUPPORTED_MODEL_NAME, answer.getResult().getOperationStatus());

        answer = client2.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE, new ChildId(2L));
        assertEquals(OperationStatus.OK, answer.getResult().getOperationStatus());
        assertTrue(answer.getBody().getResponse().getIdentifiers().isEmpty());

        client.sendCommandForModel(BaseCommand.PUT, child);

        answer = client2.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE, new ChildId(2L));
        assertEquals(OperationStatus.OK, answer.getResult().getOperationStatus());
        //
        client.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(2L), null);
        client.sendCommandForId(BaseCommand.DELETE, new MainTestModelId(3L), null);

        client.sendCommandForModel(BaseCommand.PUT, test2, null);
        client.sendCommandForModel(BaseCommand.PUT, test3, null);

        answer = client2.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE, new MainTestModelId(2L));
        assertEquals(OperationStatus.OK, answer.getResult().getOperationStatus());
        assertEquals(1, answer.getBody().getResponse().getIdentifiers().size());

        answer = client.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE, new MainTestModelId(2L));
        assertEquals(OperationStatus.OK, answer.getResult().getOperationStatus());
        assertEquals(1, answer.getBody().getResponse().getIdentifiers().size());

        answer = client2.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE, new MainTestModelId(3L));
        assertEquals(OperationStatus.OK, answer.getResult().getOperationStatus());
        assertEquals(1, answer.getBody().getResponse().getIdentifiers().size());
        answer = client.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE, new MainTestModelId(3L), null);
        assertEquals(OperationStatus.OK, answer.getResult().getOperationStatus());
        assertEquals(1, answer.getBody().getResponse().getIdentifiers().size());

        int counter = 0;

        answer = client.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE, new MainTestModelId(2L), null);
        assertEquals(OperationStatus.OK, answer.getResult().getOperationStatus());
        counter += answer.getBody().getResponse().getIdentifiers().size();



        answer = client.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE, new MainTestModelId(3L), null);
        assertEquals(OperationStatus.OK, answer.getResult().getOperationStatus());
        counter += answer.getBody().getResponse().getIdentifiers().size();

        assertEquals(2, counter);

    }

    public void testAddModelToServer() throws Exception {
        final SimpleCacheClient client2 = new SimpleCacheClient(host, 2422);

        final Child child2 = new Child();
        child2.setId(2);
        child2.setName("childname2");

        final Child child3 = new Child();
        child3.setId(3);
        child3.setName("childname3");

        final MainTestModel test2 = new MainTestModel();
        test2.setId(2);
        test2.setName("test2");

        final MainTestModel test3 = new MainTestModel();
        test3.setId(3);
        test3.setName("test3");

        client.sendCommandForId(BaseCommand.DELETE, new ChildId(2L));

        FullMessage answer = client.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE, new ChildId(2L));
        assertEquals(OperationStatus.ERROR, answer.getResult().getOperationStatus());

        // ---------
        final StoreModelImpl storeModel = getStoreData("Child", "com.nomad.cache.test.model.Child");

        final ModelSource modelSource = new ModelSource();
        modelSource.setStoreModel(storeModel);
        final DataSourceModelImpl dataSourceModel = new DataSourceModelImpl();
        dataSourceModel.setName("a");
        dataSourceModel.setTimeOut(1000);
        dataSourceModel.setThreads(20);
        modelSource.setDataSourceModel(dataSourceModel);
        // TODO
        // mcl1.sendCommand(ManagerCommand.RegisterModel, ms);
        client.sendCommandForId(BaseCommand.DELETE, new ChildId(2L), null);
        client.sendCommandForId(BaseCommand.DELETE, new ChildId(3L), null);

        client.sendCommandForModel(BaseCommand.PUT, child2, null);
        client.sendCommandForModel(BaseCommand.PUT, child3, null);

        answer = client.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE, new ChildId(2L), null);
        answer = client2.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE, new ChildId(2L), null);

        if (OperationStatus.EMPTY_ANSWER.equals(answer.getResult().getOperationStatus())) {
            answer = client.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE, new ChildId(3L), null);
            assertEquals(OperationStatus.OK, answer.getResult().getOperationStatus());

        } else {
            answer = client.sendCommandForId(ServiceCommand.IN_LOCAL_CACHE, new ChildId(3L), null);
            assertEquals(OperationStatus.EMPTY_ANSWER, answer.getResult().getOperationStatus());

        }

    }

    @After
    public void stop() {
        if (launcher != null) {
            launcher.stop();
        }
        if (launcher2 != null) {
            launcher2.stop();
        }
    }

    @Before
    public void start() throws Exception {
        commonSetup();
        registerSerialized();

        version = 0x1;

        final List<StoreModelImpl> models1 = new ArrayList<>();
        models1.add(getStoreData("MainTestModel", "com.nomad.cache.test.model.MainTestModel", ServerType.CACHE_CACHE_MANAGER));
        models1.add(getStoreData("Child", "com.nomad.cache.test.model.Child", ServerType.CACHE_MANAGER));
        final List<StoreModelImpl> models2 = new ArrayList<>();
        models2.add(getStoreData("Child", "com.nomad.cache.test.model.Child", ServerType.CACHE));
        models2.add(getStoreData("MainTestModel", "com.nomad.cache.test.model.MainTestModel", ServerType.CACHE));

        final ServerModelImpl serverModel = new ServerModelImpl();
        serverModel.setTrustSessions(true);

        serverModel.getStoreModels().addAll(models1);
        serverModel.getListeners().add(getListenerModel(host,2222, 5));
        serverModel.getListeners().add(getListenerModel(host,2232, 5));
        serverModel.setManagementServerModel(getManagementServerModel(2242, host, 2, 2000));
        serverModel.setServerName("main");
        serverModel.getProperties().setProperty("SessionStorePath", "/opt/t1/sessionstore/1");
        serverModel.setSessionServerModel(getSessionServerModel(host,5884, 10));
        serverModel.getSessionClientModels().add(getSessionClientModel(host,5884, 5));
        serverModel.getSaveServerModels().add(getSaveServerModel(host,5882, 10));
        serverModel.getSaveClientModels().add(getSaveClientModel(host,5882, 5));

        final DataSourceModelImpl dataSourceModel = new DataSourceModelImpl();
        dataSourceModel.setName("a");
        dataSourceModel.setThreads(10);
        dataSourceModel.setClazz("com.nomad.cache.test.userdataaccess.BaseDataInvoker");
        dataSourceModel.addProperty("user", "sa");
        dataSourceModel.addProperty("password", "");
        dataSourceModel.addProperty("url", "jdbc:hsqldb:hsql://localhost/test");
        dataSourceModel.addProperty("driver", "org.hsqldb.jdbcDriver");
        dataSourceModel.setTimeOut(10000);
        serverModel.addDataSources(dataSourceModel);

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

        serverModel.getCommandPlugins().add(plugin);

        launcher = new ServerLauncher(serverModel);

        launcher.start();

        final ServerModelImpl serverModel2 = new ServerModelImpl();
        serverModel2.setTrustSessions(true);
        serverModel2.getStoreModels().addAll(models2);

        final ListenerModelImpl listener2=getListenerModel(host,2422, 5);
        serverModel2.getListeners().add(listener2);
        serverModel2.getListeners().add(getListenerModel(host,2432, 5));
        serverModel2.setManagementServerModel(getManagementServerModel(2442, host, 2, 2000));
        serverModel2.setServerName("second");
        serverModel2.getSessionClientModels().add(getSessionClientModel(host, 5884, 5));
        serverModel2.getSaveClientModels().add(getSaveClientModel(host,5882, 5));


        serverModel2.addDataSources(dataSourceModel);

        serverModel2.getCommandPlugins().add(plugin);
        serverModel2.getProperties().setProperty("SessionStorePath", "/opt/t1/sessionstore/2");

        final ConnectModelImpl connectModel = getConnectModel(serverModel2, serverModel, 8, listener2);


        serverModel2.getClients().add(connectModel);

        launcher2 = new ServerLauncher(serverModel2);

        launcher2.start();
        client= new SimpleCacheClient(host,port);

    }

}
