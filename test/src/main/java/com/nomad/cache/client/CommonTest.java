package com.nomad.cache.client;

import java.util.Set;

import org.junit.ClassRule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.cache.commonclientserver.BodyImpl;
import com.nomad.cache.commonclientserver.BodyImplSerializer;
import com.nomad.cache.test.model.Child;
import com.nomad.cache.test.model.MainTestModel;
import com.nomad.cache.test.model.NoIdTestModel;
import com.nomad.cache.test.model.Price;
import com.nomad.client.SingleCacheClient;
import com.nomad.io.serializer.SerializerFactory;
import com.nomad.model.CommonClientModel;
import com.nomad.model.CommonClientModelImpl;
import com.nomad.model.CommonServerModel;
import com.nomad.model.CommonServerModelImpl;
import com.nomad.model.ConnectModelImpl;
import com.nomad.model.ListenerModel;
import com.nomad.model.ListenerModelImpl;
import com.nomad.model.SaveClientModelImpl;
import com.nomad.model.SaveServerModelImpl;
import com.nomad.model.ServerModel;
import com.nomad.model.StoreModel.ServerType;
import com.nomad.model.StoreModel.StoreType;
import com.nomad.model.StoreModelImpl;
import com.nomad.model.command.CommandServerModel;
import com.nomad.model.command.CommandServerModelImpl;
import com.nomad.model.management.ManagementServerModel;
import com.nomad.model.management.ManagementServerModelImpl;
import com.nomad.model.saveserver.SaveClientModel;
import com.nomad.model.saveserver.SaveServerModel;
import com.nomad.model.session.SessionClientModelImpl;
import com.nomad.model.session.SessionServerModelImp;
import com.nomad.server.ServerLauncher;

public class CommonTest {
    protected static Logger LOGGER = LoggerFactory.getLogger(CommonTest.class);

    protected static byte version = 0x1;
    protected static String host = "localhost";


    protected static void setRemoteJMXBean() {
        System.setProperty("com.sun.management.jmxremote", "true");
        System.setProperty("com.sun.management.jmxremote.authenticate", "false");
        System.setProperty("com.sun.management.jmxremote.port", "9245");
        System.setProperty("com.sun.management.jmxremote.ssl", "false");
        System.setProperty("java.rmi.server.hostname", "localhost");

    }

    @ClassRule
    public static TestRule watchman = new TestWatcher() {
        @Override
        protected void starting(final Description description) {
            String mN = description.getMethodName();
            if (mN == null) {
                mN = "setUpBeforeClass..";
            }
            System.out.println(String.format("starting..JUnit-Test: %s", description.getClassName()));
            printThreads();
        }

        @Override
        protected void finished(final Description description) {
            String mN = description.getMethodName();
            if (mN == null) {
                mN = "setUpBeforeClass..";
            }
            System.out.println(String.format("stoping..JUnit-Test: %s", description.getClassName()));
            printThreads();
        }

    };

    protected static void commonSetup() throws Exception {
        host = "localhost";
        version = 0x1;
    }


    protected static SaveServerModel getSaveServerModel(final String host, final int port, final int threads) {
        final SaveServerModel saveServerModel = new SaveServerModelImpl();
        saveServerModel.setHost(host);
        saveServerModel.setPort(port);
        saveServerModel.setMinThreads(threads);
        saveServerModel.setMaxThreads(threads);
        return saveServerModel;
    }

    protected static SaveClientModel getSaveClientModel(final String host,final int port, final int threads) {
        final SaveClientModel saveClientModel = new SaveClientModelImpl();
        saveClientModel.setHost(host);
        saveClientModel.setPort(port);
        saveClientModel.setThreads(threads);
        return saveClientModel;
    }

    protected static SessionServerModelImp getSessionServerModel(final String host, final int port, final int threads) {
        final SessionServerModelImp sessionServerModel = new SessionServerModelImp();
        sessionServerModel.setHost(host);
        sessionServerModel.setPort(port);
        sessionServerModel.setMinThreads(threads);
        sessionServerModel.setMaxThreads(threads);
        sessionServerModel.setKeepAliveTime(6000);
        sessionServerModel.setSessionTimeLive(1000000);
        return sessionServerModel;
    }
    protected static SessionClientModelImpl getSessionClientModel(final String host,final int port, final int threads) {
        final SessionClientModelImpl sessionClientModel = new SessionClientModelImpl();
        sessionClientModel.setHost(host);
        sessionClientModel.setPort(port);
        sessionClientModel.setThreads(threads);
        sessionClientModel.setTimeout(1000);
        return sessionClientModel;
    }

    protected static ListenerModelImpl getListenerModel(final String host,final int port, final int threads) {
        final ListenerModelImpl listener = new ListenerModelImpl();
        listener.setPort(port);
        listener.setMinThreads(threads);
        listener.setMaxThreads(threads);
        listener.setBacklog(10);
        listener.setHost(host);
        listener.setProtocolVersion("003");
        return listener;
    }

    protected static void registerSerialized() throws ClassNotFoundException {
        SerializerFactory.registerSerializer(BodyImpl.class, BodyImplSerializer.class);
    }

    protected static StoreModelImpl getStoreData(final String name, final String clazz) {
        final StoreModelImpl storeModel = new StoreModelImpl();
        storeModel.setModel(name);
        storeModel.setClazz(clazz);
        storeModel.setReadThrough(true);
        storeModel.setWriteThrough(true);
        storeModel.setDataSource("a");
        storeModel.setServerType(ServerType.ALL);
        storeModel.setStoreType(StoreType.BYTES);
        storeModel.setCopyCount(2);
        return storeModel;
    }
    /*
     * 1- cache
     * 2- cm
     * 3 - c+cm
     *
     */
    protected static StoreModelImpl getStoreData(final String name, final String clazz, final ServerType serverType) {
        final StoreModelImpl storeModel = new StoreModelImpl();
        storeModel.setModel(name);
        storeModel.setClazz(clazz);
        storeModel.setReadThrough(true);
        storeModel.setWriteThrough(true);
        storeModel.setDataSource("a");
        storeModel.setServerType(serverType);
        storeModel.setStoreType(StoreType.BYTES);
        storeModel.setCopyCount(2);
        return storeModel;
    }

    protected static MainTestModel getNewTestModel(final int id, final String name) {
        final MainTestModel result = new MainTestModel();
        result.setId(id);
        result.setName(name);
        return result;
    }

    protected static Price getPriceModel(final int id, final String name) {
        final Price result = new Price();
        result.setId(id);
        result.setName(name);
        long mid=id/8;
        result.setMainId(mid);
        return result;
    }

    protected static NoIdTestModel getNewNoIdTestModel(final String name) {
        final NoIdTestModel result = new NoIdTestModel();
        result.setName(name);
        return result;
    }

    protected MainTestModel getNewTestModel(final int id, final String name, final int childId) {
        final MainTestModel result = new MainTestModel();
        result.setId(id);
        result.setName(name);
        result.setChildId(childId);
        return result;

    }

    protected Child getChildModel(final int id, final String name) {
        final Child result = new Child();
        result.setId(id);
        result.setName(name);
        return result;
    }

    protected static ManagementServerModel getManagementServerModel(final int port,final String host,final int threads,final int keepAliveTime){
        final ManagementServerModel managementServer = new ManagementServerModelImpl();
        managementServer.setPort(port);
        managementServer.setHost(host);
        managementServer.setMinThreads(threads);
        managementServer.setMaxThreads(threads);
        managementServer.setKeepAliveTime(keepAliveTime);
        return managementServer;
    }

    protected static CommonClientModel getCommonClient(final String host, final int port ,final int threads, final int timeout){
        final CommonClientModel client = new CommonClientModelImpl();
        client.setHost(host);
        client.setPort(port);
        client.setThreads(threads);
        client.setTimeout(timeout);
        return client;
    }
    protected static CommonServerModel getCommonServerModel(final int port,final String host,final int minThreads,final int maxThreads,final int keepAliveTime){
        final CommonServerModel server= new CommonServerModelImpl();
        server.setPort(port);
        server.setHost(host);
        server.setMinThreads(minThreads);
        server.setMaxThreads(maxThreads);
        server.setKeepAliveTime(keepAliveTime);
        return server;
    }

    protected static CommandServerModel getCommandServerModel(final int port,final String host,final int threads,final int keepAliveTime){
        final CommandServerModel server = new CommandServerModelImpl();
        server.setPort(port);
        server.setHost(host);
        server.setMinThreads(threads);
        server.setMaxThreads(threads);
        server.setKeepAliveTime(keepAliveTime);
        return server;
    }

    protected static CommonClientModel getCommonClient(final ListenerModel listener, final int threads) {
        final CommonClientModel client = new CommonClientModelImpl();
        client.setHost(listener.getHost());
        client.setPort(listener.getPort());
        client.setThreads(threads);
        client.setTimeout(listener.getBacklog());
        return client;
    }


    protected static ConnectModelImpl getConnectModel(final ServerModel server, final ServerModel client, final int threads, final ListenerModel listener) {
        final ConnectModelImpl result = new ConnectModelImpl();
        final CommonClientModel clientModel = new CommonClientModelImpl();
        clientModel.setHost(server.getManagementServerModel().getHost());
        clientModel.setPort(server.getManagementServerModel().getPort());
        clientModel.setProtocolType(server.getManagementServerModel().getProtocolType());
        clientModel.setThreads(threads);
        clientModel.setTimeout(2000);

        result.setManagementServer(clientModel);
        final CommonClientModel clientModel2 = new CommonClientModelImpl();
        clientModel2.setHost(client.getManagementServerModel().getHost());
        clientModel2.setPort(client.getManagementServerModel().getPort());
        clientModel2.setThreads(1);
        clientModel2.setTimeout(1000);
        result.setManagementClient(clientModel2);

        result.setListener(listener);
        result.setThreads(threads);

        return result;

    }
    protected static void printThreads(){
        final Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        // if (threadSet.size() > 7) {
        System.out.println("Threads:" + threadSet.size() + " :" + threadSet);
        // }
    }

    protected static void printFullInfoThreads() {
        final Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        // if (threadSet.size() > 7) {
        System.out.println("Threads:" + threadSet.size() + " :" + threadSet);
        // }
    }

    protected static void close(final SingleCacheClient client){
        if(client!=null){
            client.close();
        }
    }
    protected static void close(ServerLauncher launcher){
        if(launcher!=null){
            launcher.stop();
        }
    }

}
