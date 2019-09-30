package com.nomad.cache.client;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.nomad.cache.test.model.TestCriteria;
import com.nomad.client.SimpleCacheClient;
import com.nomad.client.SingleCacheClient;
import com.nomad.model.CommandPluginModelImpl;
import com.nomad.model.DataSourceModelImpl;
import com.nomad.model.ListenerModelImpl;
import com.nomad.model.ServerModelImpl;
import com.nomad.model.StoreModelImpl;
import com.nomad.model.StoreModel.ServerType;
import com.nomad.model.server.ProtocolType;
import com.nomad.pm.PmDataInvokerFactory;
import com.nomad.server.PmDataInvoker;
import com.nomad.server.ServerLauncher;

public class CriteriaTest extends CommonTest {

    private static ServerLauncher launcher;

    private static String host = "localhost";
    private static int port = 2222;

    private static SingleCacheClient client;
    private static PmDataInvoker dataInvoker;

    /*
     * test session isolation
     */
    @org.junit.Test
    public void test1() throws Exception {
        final TestCriteria criteria = new TestCriteria();
        criteria.setPageSize(10);
        client.getModels(criteria, null);


    }


    @BeforeClass
    public static void setUp() throws Exception {


        final ListenerModelImpl listener = new ListenerModelImpl();
        listener.setPort(2222);
        listener.setMinThreads(10);
        listener.setMaxThreads(10);
        listener.setBacklog(10);
        final List<StoreModelImpl> models = new ArrayList<>();

        StoreModelImpl storeModel = getStoreData("MainTestModel", "com.nomad.cache.test.model.MainTestModel");
        storeModel.setServerType(ServerType.CACHE);
        storeModel.setCopyCount(1);
        models.add(storeModel);
        StoreModelImpl storeModelForChild = getStoreData("Child", "com.nomad.cache.test.model.Child");
        storeModelForChild.setServerType(ServerType.CACHE);
        storeModelForChild.setCopyCount(1);
        models.add(storeModelForChild);

        final ServerModelImpl serverModel = new ServerModelImpl();
        serverModel.setCommandServerModel(getCommandServerModel(2221,host,  2, 2000));
        serverModel.setManagementServerModel(getManagementServerModel(2224,host,  10, 2000));
        serverModel.getStoreModels().addAll(models);
        serverModel.getListeners().add(listener);
        final DataSourceModelImpl dataSourceModel = new DataSourceModelImpl();
        dataSourceModel.setName("a");
        dataSourceModel.setTimeOut(10000);
        dataSourceModel.setThreads(12);
        dataSourceModel.setClazz("com.nomad.cache.test.userdataaccess.BaseDataInvoker");
        dataSourceModel.addProperty("user", "sa");
        dataSourceModel.addProperty("password", "");
        dataSourceModel.addProperty("url", "jdbc:hsqldb:hsql://localhost/test");
        dataSourceModel.addProperty("driver", "org.hsqldb.jdbcDriver");
        serverModel.addDataSources(dataSourceModel);

        final CommandPluginModelImpl plugin = new CommandPluginModelImpl();
        plugin.setCheckDelay(10);
        plugin.setClazz("com.nomad.plugin.IdGenerator");
        plugin.setPoolSize(10);
        plugin.getProperties().put("DataSourceName", "a");
        plugin.setTimeout(10);

        serverModel.getCommandPlugins().add(plugin);
        serverModel.setCalculateStatistic(false);

        dataInvoker = PmDataInvokerFactory.getDataInvoker("b", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:hsql://localhost/test", "sa", "", null, 1);

        launcher = new ServerLauncher(serverModel);
        launcher.start();

        client = new SimpleCacheClient(host, port, ProtocolType.TCP);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        client.close();
        launcher.stop();
        dataInvoker.close();
        dataInvoker=null;
    }

}
