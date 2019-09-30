package com.nomad;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.exception.SystemException;
import com.nomad.io.serializer.SerializerFactory;
import com.nomad.model.ConnectModel;
import com.nomad.model.Serializer;
import com.nomad.model.ServerModel;
import com.nomad.model.management.ManagementClientModel;
import com.nomad.model.management.ManagementClientModelImpl;
import com.nomad.model.management.ManagementServerModel;
import com.nomad.saver.LoadConfiguration;
import com.nomad.server.ServerLauncher;

public class StartFormXml {
    protected static Logger LOGGER = LoggerFactory.getLogger(StartFormXml.class);

    private final List<ServerLauncher> launchers = new ArrayList<>();

    public static void main(final String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: StartFormXml file.xml [file.xml..]");
        }

        final StartFormXml starter = new StartFormXml();

        try {
            starter.startServers(args);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void startServers(final String[] files) throws SystemException {
        for (final String fileName : files) {
            startFormResource(fileName);
        }

    }

    private void startFormResource(final String fileName) throws SystemException {
        LOGGER.info("start:{} ", fileName);

        Enumeration<URL> resources= null;
        try {
            resources = Thread.currentThread().getContextClassLoader().getResources(fileName);
        } catch (IOException e) {
            throw new SystemException(e.getMessage(),e);
        }
        if (!resources.hasMoreElements()) {
            throw new SystemException("resource:" + fileName + " not found!");
        }
        final URL url = resources.nextElement();

        ServerModel serverModel;
        try (final InputStream input = url.openStream()) {
            serverModel = new LoadConfiguration().load(input);
            for (final Serializer serializer : serverModel.getSerializers()) {
                SerializerFactory.registerSerializer(serializer.getClazz(), serializer.getSerializerClazz());
            }
        }catch(IOException e){
            throw new SystemException(e.getMessage(),e);

        } finally {

        }
        // post processing{
        ManagementServerModel managerServer = serverModel.getManagementServerModel();
        if (serverModel.getClients() != null && serverModel.getClients().size() > 0) {
            for (ConnectModel connectModel : serverModel.getClients()) {
                connectModel.setManagementServer(getManagementClientModel(managerServer));
            }
        }
        if (serverModel.getServers() != null && serverModel.getServers().size() > 0) {
            for (ConnectModel connectModel : serverModel.getServers()) {
                connectModel.setManagementClient(getManagementClientModel(managerServer));
            }
        }
        // }
        if (serverModel != null) {
            final ServerLauncher launcher = new ServerLauncher(serverModel);
            launcher.start();
            launchers.add(launcher);
        }
    }

    private ManagementClientModel getManagementClientModel(ManagementServerModel managerServer) {
        ManagementClientModel managementClientModel = new ManagementClientModelImpl();
        managementClientModel.setHost(managerServer.getHost());
        managementClientModel.setPort(managerServer.getPort());
        managementClientModel.setProtocolType(managerServer.getProtocolType());
        managementClientModel.setThreads(2);
        managementClientModel.setTimeout(1000);
        return managementClientModel;

    }
    public void stop() {
        for (final ServerLauncher launcher : launchers) {
            launcher.stop();
        }
    }

    public List<ServerLauncher> getLaunchers() {
        return launchers;
    }

}
