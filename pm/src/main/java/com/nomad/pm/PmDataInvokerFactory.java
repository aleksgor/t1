package com.nomad.pm;

import java.io.Serializable;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.exception.SystemException;
import com.nomad.model.Model;
import com.nomad.pm.util.ConnectPoolFactory;
import com.nomad.server.DataDefinitionService;
import com.nomad.server.PmDataInvoker;
import com.nomad.server.ServerContext;

public class PmDataInvokerFactory implements Serializable {
    protected static final Logger LOGGER = LoggerFactory.getLogger(PmDataInvokerFactory.class);

    public PmDataInvokerFactory() {
    }

    public static  PmDataInvoker getDataInvoker(final String connectName, final String driver, final String url, final String user,
            final String password,
            final String configurationFile, final int threads) throws SystemException {
        PmDataInvoker result = getDataInvoker(connectName, driver, url, user, password, configurationFile, threads, null);
        return result;
    }

    public static <T extends Model> PmDataInvoker getDataInvoker(final String connectName, String driver, final String url, final String user,
            final String password,
            final String configurationFile, final int threads, final ServerContext context) throws SystemException {

        LOGGER.debug(" !!!DataInvokerFactory threads: {}", threads);
        PmDataInvoker result = null;

        final String classInvoker = getClassOfDataInvoker(url);
        if (driver == null) {
            driver = getDriverByUrl(url);
        }
        if (classInvoker == null) {
            throw new SystemException("Cannot define class for " + driver);
        }
        result = load(classInvoker);
        DataDefinitionService dataDefinitionService = null;

        if (context != null) {
            dataDefinitionService = context.getDataDefinitionService(connectName, configurationFile);
        }
        result.setDataDefinitionService(dataDefinitionService);

        result.setConnectPool(ConnectPoolFactory.getConnectPool(connectName, url, user, password, threads, context, driver));

        final Properties properties = new Properties();
        if (configurationFile != null) {
            properties.setProperty("fileConfiguration", configurationFile);
        }
        result.init(properties, context, connectName);
        LOGGER.debug("DataInvokerFactory: loaded");

        return result;
    }

    private static PmDataInvoker load(final String nameClass) throws SystemException {
        try {
            LOGGER.debug("PmDataInvoker load:{}", nameClass);
            final Class<?> clazz = Class.forName(nameClass);
            final PmDataInvoker dataInvoker = (PmDataInvoker) clazz.newInstance();
            return dataInvoker;
        } catch (final ClassNotFoundException e) {
            LOGGER.error("Cannot found class:" + nameClass);
            throw new SystemException("Cannot found class:" + nameClass);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new SystemException("Cannot found class:" + nameClass);
        }

    }

    protected static String getClassOfDataInvoker(final String url) throws SystemException {
        String result = "";
        LOGGER.debug("url:{}", url);
        if (url != null) {
            if (url.toLowerCase().indexOf("postgresql") >= 0) {
                result = "com.nomad.pm.generator.PostgresGenerator";
            } else if (url.toLowerCase().indexOf("oracle") >= 0) {
                result = "com.nomad.pm.generator.OracleGenerator";
            } else if (url.toLowerCase().indexOf("mysql") >= 0) {
                result = "com.nomad.pm.generator.MysqlGenerator";
            } else if (url.toLowerCase().indexOf("hsqldb") >= 0) {
                result = "com.nomad.pm.generator.HsqlGenerator";
            } else {
                result = "com.nomad.pm.generator.Sql92Generator";
            }
        }
        return result;
    }

    protected static String getDriverByUrl(final String url) throws SystemException {
        String result = "";
        LOGGER.debug("url:{}", url);
        if (url != null) {
            if (url.toLowerCase().indexOf("postgresql") >= 0) {
                result = "org.postgresql.Driver";
            }
            if (url.toLowerCase().indexOf("oracle") >= 0) {
                result = "oracle.jdbc.driver.OracleDriver";
            }
            if (url.toLowerCase().indexOf("mysql") >= 0) {
                result = "com.mysql.jdbc.Driver";
            }
        }
        return result;
    }

}
