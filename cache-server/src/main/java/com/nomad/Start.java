package com.nomad;

import java.io.File;
import java.io.FileInputStream;
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
import com.nomad.model.Serializer;
import com.nomad.model.ServerModel;
import com.nomad.saver.LoadConfiguration;
import com.nomad.server.ServerLauncher;

public class Start {
    private static final Logger LOGGER = LoggerFactory.getLogger(Start.class);
    private final List<ServerLauncher> launchers = new ArrayList<>();

    public static void main(final String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: Start file.xml [file.xml..]");
        }
        final Start starter = new Start();
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
        InputStream input;
        try {
            final File f = new File(fileName);
            if (f.exists()) {
                input = new FileInputStream(f);
            } else {

                final Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(fileName);
                if (!resources.hasMoreElements()) {
                    throw new IOException("resource:" + fileName + " not found!");
                }
                final URL url = resources.nextElement();
                input = url.openStream();
            }
            final LoadConfiguration loader = new LoadConfiguration();
            final ServerModel servermodel = loader.load(input);
            servermodel.getSerializers();
            for (final Serializer serializer : servermodel.getSerializers()) {
                SerializerFactory.registerSerializer(serializer.getClazz(), serializer.getSerializerClazz());
            }
            final ServerLauncher launcher = new ServerLauncher(servermodel);

            launcher.start();
            launchers.add(launcher);
        } catch (IOException e) {
            throw new SystemException(e.getMessage(), e);
        }
    }

    public void stop() {
        for (final ServerLauncher launcher : launchers) {
            launcher.stop();
        }
    }
}
