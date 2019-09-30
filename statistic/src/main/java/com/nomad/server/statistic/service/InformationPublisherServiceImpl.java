package com.nomad.server.statistic.service;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.ExportException;
import java.util.HashMap;

import javax.management.Attribute;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.exception.SystemException;
import com.nomad.server.statistic.InformationPublisher;
import com.nomad.server.statistic.InformationPublisherService;

public class InformationPublisherServiceImpl implements InformationPublisherService {
    protected static Logger LOGGER = LoggerFactory.getLogger(InformationPublisherServiceImpl.class);
    public static final String CACHE_SERVER_PREFIX = "com.nomad.cacheServer.";

    private MBeanServer server;
    private final InformationPublisher publisherModel;
    private boolean security = false;
    private int port = 9999;
    final HashMap<String, Object> environment = new HashMap<String, Object>();
    JMXServiceURL url;

    public InformationPublisherServiceImpl(InformationPublisher model) throws SystemException {
        publisherModel = model;
        if (model == null || model.getHost() == null || model.getHost().length() == 0) {
            // locale mode
            server = getServer();
        } else {
            // remote mode
        }
    }

    @Override
    public void start() throws SystemException {

    }

    @Override
    public void stop() {

    }

    private MBeanServer getServer() throws SystemException  {
        if (server == null) {
            security = Boolean.parseBoolean(System.getProperty("com.sun.management.jmxremote.authenticate"));
            if (publisherModel != null) {
                security = publisherModel.isSecurity();
            }
            String property = System.getProperty("com.sun.management.jmxremote.port");
            if (property != null) {
                port = Integer.parseInt(property);
            }
            if (publisherModel != null && publisherModel.getPort() != 0) {
                port = publisherModel.getPort();
            }
            System.setProperty("java.rmi.server.randomIDs", "true");

            //
            LOGGER.info("Create RMI registry on port " + port);
            try {
                LocateRegistry.createRegistry(port);
            } catch (ExportException e) {
                LOGGER.warn("port:" + port + " already in use");
            } catch (RemoteException e) {
                throw new SystemException(e.getMessage(),e);
            }

            LOGGER.info("Get the platform's MBean server");
            server = ManagementFactory.getPlatformMBeanServer();
            if (security) {
                LOGGER.info("securityEnabled");
                final SslRMIClientSocketFactory clientFactory = new SslRMIClientSocketFactory();
                final SslRMIServerSocketFactory serverFactory = new SslRMIServerSocketFactory();
                environment.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, clientFactory);
                environment.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, serverFactory);

                environment.put("jmx.remote.x.password.file", "password.properties");
                environment.put("jmx.remote.x.access.file", "access.properties");

            }
            LOGGER.info("Create an RMI connector server");
            if (port == 0) {

            }
            try {
            url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:" + port + "/jmxrmi");

            JMXConnectorServer serverConnect = JMXConnectorServerFactory.newJMXConnectorServer(url, environment, server);

            LOGGER.info("Start the RMI connector server");
                serverConnect.start();

            } catch (IOException e) {
                ;
            } finally {

            }
            LOGGER.info("Started the RMI connector server");
            // Start the RMI connector server.
            //

        }
        return server;
    }

    @Override
    public void publicData(final Object bean, final String serverName, final String type, final String name) {
        final ObjectName objectName;
        try {
            objectName = getObjectName(serverName, type, name);
        } catch (Throwable e) {
            return;
        }
        try (JMXConnector jmxc = JMXConnectorFactory.connect(url, environment)) {
            if (!jmxc.getMBeanServerConnection().isRegistered(objectName)) {
                try {
                    final MBeanServer beanServer = getServer();
                    beanServer.registerMBean(bean, objectName);

                } catch (final InstanceAlreadyExistsException e) {
                    LOGGER.warn("Instance Already Exists:" + e.getMessage());
                } catch (final Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            } else {
                MBeanInfo beanInfo = jmxc.getMBeanServerConnection().getMBeanInfo(objectName);
                MBeanAttributeInfo[] attributes = beanInfo.getAttributes();
                for (MBeanAttributeInfo mBeanAttributeInfo : attributes) {
                    try {
                        String attributeName = mBeanAttributeInfo.getName();
                        Object value = new PropertyDescriptor(attributeName, bean.getClass()).getReadMethod().invoke(bean);
                        Attribute attribute = new Attribute(attributeName, value);
                        jmxc.getMBeanServerConnection().setAttribute(objectName, attribute);
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            }
        } catch (Exception x) {
            LOGGER.error(x.getMessage(), x);
        }
    }

    @Override
    public Object getData(final String serverName, final String type, final String name) throws SystemException {
        try (JMXConnector jmxc = JMXConnectorFactory.connect(url, environment)) {
            final ObjectName objectName = getObjectName(serverName, type, name);
            if (jmxc.getMBeanServerConnection().isRegistered(objectName)) {
                try {
                    MBeanInfo beanInfo = jmxc.getMBeanServerConnection().getMBeanInfo(objectName);
                    MBeanAttributeInfo[] attributes = beanInfo.getAttributes();
                    Class<?> beanClass = Class.forName(beanInfo.getClassName());
                    Object bean = beanClass.newInstance();
                    for (MBeanAttributeInfo mBeanAttributeInfo : attributes) {
                        String attributeName = mBeanAttributeInfo.getName();
                        Object value = jmxc.getMBeanServerConnection().getAttribute(objectName, attributeName);
                        new PropertyDescriptor(attributeName, bean.getClass()).getWriteMethod().invoke(bean, value);
                    }
                    return bean;
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        } catch (Exception x) {
            throw new SystemException(x.getMessage(),x);
        }
        return null;
    }

    private static ObjectName getObjectName(final String serverName, final String type, final String name) throws MalformedObjectNameException {
        String beanName = CACHE_SERVER_PREFIX + serverName;
        if (type != null && type.length() > 0) {
            beanName += ":type=" + type;
        }
        if (name != null && name.length() > 0) {
            beanName += ", name=" + name;
        }
        return new ObjectName(beanName);

    }

}
