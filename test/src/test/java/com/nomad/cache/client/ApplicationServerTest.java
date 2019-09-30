package com.nomad.cache.client;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;

import com.nomad.cache.commonclientserver.ManagementMessageImpl;
import com.nomad.cache.test.model.MainTestModelId;
import com.nomad.client.ClientPooledInterface;
import com.nomad.client.SimpleCacheClient;
import com.nomad.communication.binders.ServerFactory;
import com.nomad.datadefinition.DataDefinitionServiceImpl;
import com.nomad.message.FullMessage;
import com.nomad.message.ManagementMessage;
import com.nomad.message.OperationStatus;
import com.nomad.model.CommandPluginModelImpl;
import com.nomad.model.DataSourceModelImpl;
import com.nomad.model.ListenerModelImpl;
import com.nomad.model.ManagerCommand;
import com.nomad.model.ServerModelImpl;
import com.nomad.model.StoreModelImpl;
import com.nomad.model.management.ManagementClientModel;
import com.nomad.model.management.ManagementClientModelImpl;
import com.nomad.model.server.ProtocolType;
import com.nomad.server.DataDefinitionService;
import com.nomad.server.ServerContext;
import com.nomad.server.ServerContextImpl;
import com.nomad.server.ServerLauncher;
import com.nomad.server.service.ManagementService;
import com.nomad.server.service.management.ManagementServiceImpl;

@Ignore
public class ApplicationServerTest extends CommonTest {
    private static ServerLauncher launcher;

    private static int port = 2222;
    private static int managementPort = 2226;
    private static SimpleCacheClient client ;
    private static ClientPooledInterface<ManagementMessage, ManagementMessage> managerClient ;

    //    @org.junit.Test
    public void test1() throws Exception {
        FullMessage message = client.sendCommandForId("getId", new MainTestModelId(3));
        assertEquals(OperationStatus.INVALID_OPERATION_NAME, message.getResult().getOperationStatus());

        final File f = new File("/opt/t1/GenerateIdPlugin/target/GenerateIdPlugin-1.0.0-SNAPSHOT.jar");
        final ManagementService managementService = new ManagementServiceImpl(null);
        ManagementMessage managementMessage = managementService.getCommand(ManagerCommand.UPLOAD_PLUGIN.toString(), f);
        ManagementMessage answer = managerClient.sendMessage(managementMessage);

        assertEquals(OperationStatus.OK, answer.getResult().getOperationStatus());

        // register command
        final CommandPluginModelImpl pluginModel= new CommandPluginModelImpl();
        pluginModel.setCheckDelay(60);
        pluginModel.setClazz("com.nomad.plugin.IdGenerator");
        pluginModel.setPoolSize(10);
        pluginModel.setTimeout(10);
        pluginModel.getProperties().put("DataSourceName", "a");

        managementMessage = new ManagementMessageImpl();
        managementMessage.setCommand(ManagerCommand.REGISTER_COMMAND.toString());
        managementMessage.setData(pluginModel);
        answer = managerClient.sendMessage(managementMessage);

        assertEquals(OperationStatus.OK, answer.getResult().getOperationStatus());

        message = client.sendCommandForId("getId", new MainTestModelId(3));
        message = client.sendCommandForId("getId", new MainTestModelId(3));

    }


    @BeforeClass
    public static void setUp() throws Exception {


        commonSetup();

        // launcher.start();

        final ListenerModelImpl listenerModel = getListenerModel(host, port, 10);

        final List<StoreModelImpl> models = new ArrayList<>();
        models.add(getStoreData("MainTestModel", "com.nomad.cache.test.model.MainTestModel"));
        models.add(getStoreData("Child", "com.nomad.cache.test.model.Child"));

        final ServerModelImpl serverModel = new ServerModelImpl();
        serverModel.setManagementServerModel(getManagementServerModel(managementPort, host, 2, 2000));

        serverModel.getStoreModels().addAll(models);
        serverModel.getListeners().add(listenerModel);
        serverModel.setCalculateStatistic(false);

        final DataSourceModelImpl dataSourceModel = new DataSourceModelImpl();
        dataSourceModel.setName("a");
        dataSourceModel.setThreads(8);
        dataSourceModel.setTimeOut(1000);
        dataSourceModel.setClazz("com.nomad.cache.test.userdataaccess.BaseDataInvoker");
        dataSourceModel.addProperty("user", "sa");
        dataSourceModel.addProperty("password", "");
        dataSourceModel.addProperty("url", "jdbc:hsqldb:hsql://localhost/test");
        dataSourceModel.addProperty("driver", "org.hsqldb.jdbcDriver");
        serverModel.addDataSources(dataSourceModel);
        serverModel.setPluginPath("/opt/t1/repository");
        serverModel.setCalculateStatistic(false);
        launcher = new ServerLauncher(serverModel);
        launcher.start();
        client= new SimpleCacheClient(host, port);

        final ServerContext context = new ServerContextImpl();
        final DataDefinitionService dataDefinitionService = new DataDefinitionServiceImpl("a", null, null);
        dataDefinitionService.start();
        context.putDataDefinitionService(dataDefinitionService, "a");

        final ManagementClientModel managementClientModel = new ManagementClientModelImpl();
        managementClientModel.setHost(host);
        managementClientModel.setPort(2226);
        managementClientModel.setProtocolType(ProtocolType.TCP);
        managementClientModel.setThreads(1);
        managerClient = ServerFactory.getPooledClient(managementClientModel, context);

    }

    @AfterClass
    public static void tearDown() throws Exception {
        launcher.stop();
        client.close();
    }


}
