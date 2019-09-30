package com.nomad.cache.client;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.nomad.cache.test.model.MainTestModelId;
import com.nomad.datadefinition.DataDefinitionServiceImpl;
import com.nomad.model.Identifier;
import com.nomad.model.SaveClientModelImpl;
import com.nomad.model.SaveServerModelImpl;
import com.nomad.model.ServerModel;
import com.nomad.model.ServerModelImpl;
import com.nomad.model.saveserver.SaveClientModel;
import com.nomad.model.saveserver.SaveServerModel;
import com.nomad.server.DataDefinitionService;
import com.nomad.server.SaveService;
import com.nomad.server.ServerContext;
import com.nomad.server.ServerContext.ServiceName;
import com.nomad.server.ServerContextImpl;
import com.nomad.server.StoreModelService;
import com.nomad.server.service.saveservice.SaveServer;
import com.nomad.server.service.saveservice.SaveServiceImpl;
import com.nomad.server.service.storemodelservice.StoreModelServiceImpl;

public class TestSaveService extends CommonTest {

    private static SaveService service1;
    private static SaveService service2;
    private static SaveService service3;
    private static SaveServer saveServer;
    private static ServerContext context;

    @Test
    public void testMulti() throws Exception {
        final List<Identifier> l1 = new ArrayList<>();
        l1.add(new MainTestModelId(1));
        l1.add(new MainTestModelId(2));
        final List<Identifier> l2 = new ArrayList<>();
        l2.add(new MainTestModelId(2));
        l2.add(new MainTestModelId(3));
        assertEquals(2, service1.isReadyToSave(l1, "23").size());
        assertEquals(1, service2.isReadyToSave(l2, "23").size());
        assertEquals(0, service3.isReadyToSave(l2, "23").size());

        service1.cleanSession(Collections.singletonList("23"));
        service2.cleanSession(Collections.singletonList("23"));
        service3.cleanSession(Collections.singletonList("23"));
        assertEquals(2, service2.isReadyToSave(l2, "23").size());

    }

    @BeforeClass
    public static void start() throws Exception {
        final DataDefinitionService dataDefinition = new DataDefinitionServiceImpl(null, "model.xml", null);
        dataDefinition.start();
        context = new ServerContextImpl();
        context.putDataDefinitionService(dataDefinition, null);
        context = getServerContext(1, context);
        saveServer = new SaveServer(getSaveServerModel(5151), context);

        final SaveClientModel saveClientModel1 = getSaveClientModel(5151);
        final SaveClientModel saveClientModel2 = getSaveClientModel(5151);
        final SaveClientModel saveClientModel3 = getSaveClientModel(5151);

        saveServer.start();
        final ServerModel serverModel1 = new ServerModelImpl();
        serverModel1.setServerId(1);
        service1 = new SaveServiceImpl(Collections.singletonList(saveClientModel1), getServerContext(1, context));
        service2 = new SaveServiceImpl(Collections.singletonList(saveClientModel2), getServerContext(2, context));
        service3 = new SaveServiceImpl(Collections.singletonList(saveClientModel3), getServerContext(3, context));
        service1.start();
        service2.start();
        service3.start();
        Thread.sleep(100);

    }

    private static ServerContext getServerContext(final int clientId, final ServerContext context) {
        final ServerModel serverModel = new ServerModelImpl();
        serverModel.setServerId(clientId);
        final StoreModelService storeModelService = new StoreModelServiceImpl(serverModel);
        context.put(ServiceName.STORE_MODEL_SERVICE, storeModelService);
        return context;
    }
    @AfterClass
    public static void stop() {
        service1.stop();
        service2.stop();
        service3.stop();
        saveServer.stop();
        context.close();
    }

    private static SaveServerModel getSaveServerModel(final int port) {
        final SaveServerModelImpl saveServerModel = new SaveServerModelImpl();
        saveServerModel.setPort(port);
        saveServerModel.setHost("localhost");
        saveServerModel.setMinThreads(6);
        saveServerModel.setMaxThreads(6);
        saveServerModel.setSessionTimeout(10000);

        return saveServerModel;
    }

    private static SaveClientModel getSaveClientModel(final int port) {
        final SaveClientModel saveClientModel = new SaveClientModelImpl();
        saveClientModel.setPort(port);
        saveClientModel.setHost("localhost");
        saveClientModel.setThreads(2);
        return saveClientModel;
    }

}
