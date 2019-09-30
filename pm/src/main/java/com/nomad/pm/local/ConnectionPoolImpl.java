package com.nomad.pm.local;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.server.CacheServerConstants;
import com.nomad.server.ConnectionPool;
import com.nomad.server.ServerContext;
import com.nomad.utility.pool.ObjectPoolImpl;

public class ConnectionPoolImpl extends ObjectPoolImpl<JDBConnection> implements ConnectionPool<JDBConnection> {
    private final String url, user, password, driver;
    private static Logger LOGGER = LoggerFactory.getLogger(ConnectionPool.class);
    private static Object object = new Object();

    @Override
    public synchronized JDBConnection getObject() {
        LOGGER.debug("getObject");
        final JDBConnection result = super.getObject();
        result.setPool(this);
        return result;
    }

    @Override
    public synchronized void freeObjects() {
        LOGGER.debug("free object");
        super.freeObjects();
    }


    public ConnectionPoolImpl(final String url, final String user, final String password, final int poolSize, final int timeout, final ServerContext context,
            String driver) {
        super(poolSize, timeout, timeout * 2, context, false, CacheServerConstants.Statistic.DATABASE_CONNECT_GROUP_NAME);
        this.url = url;
        this.user = user;
        this.password = password;
        this.driver = driver;
        LOGGER.info("init  poolsize:{}", poolSize);

    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized JDBConnection getNewPooledObject() {

        Connection connection;
        try {
            synchronized (object) {
                Class<Driver> cDriver = (Class<Driver>) Class.forName(driver);
                Driver oDriver = cDriver.newInstance();
                Properties info= new Properties();
                info.put("user", user);
                info.put("password", password);
                connection = oDriver.connect(url, info);
                connection.setAutoCommit(true);
                return new JDBConnection(connection);
            }
        } catch (final SQLException e) {
            LOGGER.error("Connect error size:" + pool.size(), e);
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            LOGGER.error("Class not found:", driver);
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            LOGGER.error(e.getMessage(), driver);
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            LOGGER.error(e.getMessage(), driver);
            throw new RuntimeException(e);
        }

    }

    @Override
    public String getPoolId() {
        return url + ":" + user;
    }

    @Override
    public String toString() {
        return "ConnectionPool [url=" + url + ", user=" + user + ", password=" + password + ", driver=" + driver + "]";
    }

}
