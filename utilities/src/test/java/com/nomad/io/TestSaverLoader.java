package com.nomad.io;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.InetAddress;

import org.junit.Test;

import com.nomad.model.CacheMatcherModel;
import com.nomad.model.CacheMatcherModelImpl;
import com.nomad.model.CommandPluginModel;
import com.nomad.model.CommandPluginModelImpl;
import com.nomad.model.CommonClientModel;
import com.nomad.model.CommonClientModelImpl;
import com.nomad.model.CommonServerModel;
import com.nomad.model.CommonServerModelImpl;
import com.nomad.model.ConnectModel;
import com.nomad.model.ConnectModelImpl;
import com.nomad.model.ConnectStatus;
import com.nomad.model.DataSourceModel;
import com.nomad.model.DataSourceModelImpl;
import com.nomad.model.ListenerModel;
import com.nomad.model.ListenerModelImpl;
import com.nomad.model.SaveClientModelImpl;
import com.nomad.model.SaveServerModelImpl;
import com.nomad.model.ServerModel;
import com.nomad.model.ServerModelImpl;
import com.nomad.model.StoreModel;
import com.nomad.model.StoreModel.ServerType;
import com.nomad.model.StoreModel.StoreType;
import com.nomad.model.StoreModelImpl;
import com.nomad.model.command.CommandServerModel;
import com.nomad.model.command.CommandServerModelImpl;
import com.nomad.model.idgenerator.IdGeneratorClientModel;
import com.nomad.model.idgenerator.IdGeneratorClientModelImpl;
import com.nomad.model.idgenerator.IdGeneratorServerModel;
import com.nomad.model.idgenerator.IdGeneratorServerModelImpl;
import com.nomad.model.management.ManagementServerModel;
import com.nomad.model.management.ManagementServerModelImpl;
import com.nomad.model.saveserver.SaveClientModel;
import com.nomad.model.saveserver.SaveServerModel;
import com.nomad.model.server.ProtocolType;
import com.nomad.model.session.SessionClientModel;
import com.nomad.model.session.SessionClientModelImpl;
import com.nomad.model.session.SessionServerModel;
import com.nomad.model.session.SessionServerModelImp;
import com.nomad.saver.LoadConfiguration;


public class TestSaverLoader {

    @Test
    public void test2() throws Exception {

        // test

        // File f= File.createTempFile("tmp", "xx");
        File f = new File("a2.xml");
        f = new File(f.getAbsolutePath());
        if (f.exists()) {
            f.delete();
        }
        try {
            final LoadConfiguration loader = new LoadConfiguration();
            final ServerModelImpl serverModel = getServerModelImpl();
            loader.save(f, serverModel);
            final ServerModel copy = loader.load(f);
            assertEquals(serverModel, copy);

        } finally {
            // f.delete();
        }
    }

    private ServerModelImpl getServerModelImpl() throws Exception {
        final String host = InetAddress.getLocalHost().getHostName();

        final ServerModelImpl serverModel = new ServerModelImpl();

        serverModel.setManagementServerModel(getManagementServerModel(110, host, 2, 2000));
        serverModel.setServerName("serverName");
        serverModel.setPluginPath("pp");
        serverModel.getSaveClientModels().add(getSaveClientModel(200));
        serverModel.getSaveServerModels().add(getSaveServerModel(300));
        serverModel.setSessionServerModel(getSessionServerModel(400));
        serverModel.getSessionClientModels().add(getSessionClientModel(500));
        serverModel.getServers().add(getConnectModel(600));
        serverModel.getClients().add(getConnectModel(700));
        serverModel.getCommandPlugins().add(getCommandPluginModel(800));

        final DataSourceModel dataSourceModel = getDataSourceModel(900);
        serverModel.addDataSources(dataSourceModel);

        serverModel.getListeners().add(getListenerModel(1000));

        final StoreModel storeModel = getStoreModel(1100);
        serverModel.getStoreModels().add(storeModel);

        final CommandServerModel commandServerModel = getCommandServerModel("ss");
        serverModel.setCommandServerModel(commandServerModel);

        serverModel.setIdGeneratorServerModel(getIdGeneratorServerModel());
        serverModel.getIdGeneratorClientModels().add(getIdGeneratorClientModel(88));
        serverModel.getIdGeneratorClientModels().add(getIdGeneratorClientModel(89));
        return serverModel;
    }

    private CommandServerModel getCommandServerModel(final String string) {
        final CommandServerModel result = new CommandServerModelImpl();
        result.setHost(string + "host");
        result.setPort(15000);
        result.setMaxThreads(15001);
        result.setMinThreads(15002);
        return result;
    }

    private CommonClientModel getCommonClientModel(final int prefix) {
        final CommonClientModel result = new CommonClientModelImpl();
        result.setHost(prefix + "host");
        result.setPort(prefix + 1);
        result.setThreads(prefix + 2);
        result.setTimeout(prefix + 3);
        return result;
    }

    @SuppressWarnings("unused")
    private CommonServerModel getCommonServerModel(final int prefix) {
        final CommonServerModel result = new CommonServerModelImpl();
        result.setHost(prefix + "host");
        result.setPort(prefix + 1);
        result.setMinThreads(prefix + 2);
        result.setMaxThreads(prefix + 3);
        result.setKeepAliveTime(prefix + 3);
        return result;
    }

    private ListenerModel getListenerModel(final int prefix) {
        final ListenerModel result = new ListenerModelImpl();
        result.setBacklog(prefix);
        result.setHost(prefix + "host");
        result.setMaxThreads(prefix + 1);
        result.setMinThreads(prefix + 2);
        result.setPort(prefix + 3);
        result.setProtocolVersion(prefix + "4");
        result.setStatus(2);
        result.getProperties().put("key" + prefix, "value" + prefix);
        result.getProperties().put("key" + prefix + 1, "value" + prefix + 1);

        return result;
    }

    private ConnectModel getConnectModel(final int index) {
        final ConnectModel result = new ConnectModelImpl();
        result.setManagementClient(getCommonClientModel(12345));
        result.setManagementServer(getCommonClientModel(12346));
        result.setListener(getListenerModel(12347));
        result.setStatus(ConnectStatus.OK);
        result.setThreads(index + 10);
        return result;
    }

    private SaveClientModel getSaveClientModel(final int index) {
        final SaveClientModel saveClientModel = new SaveClientModelImpl();
        saveClientModel.setHost("host1" + index);
        saveClientModel.setPort(index);
        saveClientModel.setThreads(index + 1);
        saveClientModel.setTimeout(index + 2);
        return saveClientModel;
    }

    private SaveServerModel getSaveServerModel(final int index) {
        final SaveServerModel saveServerModel = new SaveServerModelImpl();
        saveServerModel.setHost("host " + index);
        saveServerModel.setPort(index + 1);
        saveServerModel.setSessionTimeout(index + 2);
        saveServerModel.setMinThreads(index + 3);
        saveServerModel.setMaxThreads(index + 4);
        return saveServerModel;
    }

    private SessionServerModel getSessionServerModel(final int index) {
        final SessionServerModel sessionServerModel = new SessionServerModelImp();
        sessionServerModel.setHost("sessdd" + index);
        sessionServerModel.setPort(index);
        sessionServerModel.setSessionTimeLive(index + 1);
        sessionServerModel.setMinThreads(index + 2);
        sessionServerModel.setMaxThreads(index + 3);
        sessionServerModel.setKeepAliveTime(index + 4);

        return sessionServerModel;
    }

    private SessionClientModel getSessionClientModel(final int index) {
        final SessionClientModel sessionClientModel = new SessionClientModelImpl();
        sessionClientModel.setHost("5host" + index);
        sessionClientModel.setPort(index);
        sessionClientModel.setThreads(index + 1);
        sessionClientModel.setTimeout(index + 2);

        return sessionClientModel;
    }

    private CommandPluginModel getCommandPluginModel(final int index) {
        final CommandPluginModel result = new CommandPluginModelImpl();
        result.setCheckDelay(index);
        result.setClazz("class" + index);
        result.setPoolSize(index + 1);
        result.setTimeout(index + 2);
        return result;
    }

    private DataSourceModel getDataSourceModel(final int index) {
        final DataSourceModel dataSourceModel = new DataSourceModelImpl();
        dataSourceModel.setClazz("clazz" + index);
        dataSourceModel.setName("name" + index);
        dataSourceModel.setThreads(index);
        dataSourceModel.getProperties().put("key" + index, "value" + index);
        dataSourceModel.getProperties().put("key" + index + 1, "value" + index + 1);
        return dataSourceModel;
    }

    private StoreModel getStoreModel(final int index) {
        final StoreModel result = new StoreModelImpl();
        result.setCopyCount(index);
        result.setDataSource("dataSource" + index);
        result.setModel("model" + index);
        result.setClazz("pkg" + index);
        result.setReadThrough(true);
        result.setServerType(ServerType.CACHE);
        result.setStoreType(StoreType.ZIPPED_BYTES);
        result.setWriteThrough(false);
        result.setCacheMatcherModel(getCacheMatcherModel(1200));
        return result;
    }

    private CacheMatcherModel getCacheMatcherModel(final int index) {
        final CacheMatcherModel result = new CacheMatcherModelImpl();
        result.setClazz("clazz" + index);
        result.getProperties().put("key" + index, "value" + index);
        result.getProperties().put("key" + index + 1, "value" + index + 1);
        return result;
    }

    protected static ManagementServerModel getManagementServerModel(final int port, final String host, final int threads, final int keepAliveTime) {
        final ManagementServerModel managementServerModel = new ManagementServerModelImpl();
        managementServerModel.setPort(port);
        managementServerModel.setHost(host);
        managementServerModel.setMinThreads(threads);
        managementServerModel.setMaxThreads(threads);
        managementServerModel.setKeepAliveTime(keepAliveTime);
        return managementServerModel;
    }

    private IdGeneratorServerModel getIdGeneratorServerModel() {
        final IdGeneratorServerModel result = new IdGeneratorServerModelImpl();
        result.setPort(3123);
        result.setHost("1362d23df2dgv");
        result.setMinThreads(45);
        result.setMaxThreads(636);
        result.setKeepAliveTime(456);
        result.setIncrement(3939);
        result.setTimeOut(29290);
        result.getModelSource().put("eweq", "eeqwe");
        result.getModelSource().put("eweq1", "eeqwe1");
        result.setProtocolType(ProtocolType.TCP);
        return result;

    }

    private IdGeneratorClientModel getIdGeneratorClientModel(int parameter) {
        final IdGeneratorClientModel result = new IdGeneratorClientModelImpl();
        result.setPort(3123);
        result.setHost("1362d23df2dgv");
        result.setProtocolType(ProtocolType.SSL);
        result.setThreads(2233+parameter);
        result.setTimeout(89900+parameter);
        result.getModelNames().add("sdasdasd" + parameter);
        result.getModelNames().add("sdasdadewqsd" + parameter);
        return result;

    }

}
