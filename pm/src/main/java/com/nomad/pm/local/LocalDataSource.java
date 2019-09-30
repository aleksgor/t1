package com.nomad.pm.local;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LocalDataSource implements DataSource {
    protected static Logger LOGGER = LoggerFactory.getLogger(LocalDataSource.class);
    private Connection connection = null;
    private final String url;
    private final String driver;
    private int LoginTimeout=1000;
    private String user ;
    private String password;

    public LocalDataSource(final String url,  final String driver){

        this.url=url;
        this.driver= driver;
    }

    public void close() {
        LOGGER.info("connection close!");
        /*		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
            LOGGER.error(e.getMessage(),e);
			}
		} */
    }

    @Override
    public int getLoginTimeout() throws SQLException {

        return LoginTimeout;
    }

    @Override
    public void setLoginTimeout(final int seconds) throws SQLException {
        LoginTimeout=seconds;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public void setLogWriter(final PrintWriter out) throws SQLException {

    }

    @Override
    public Connection getConnection() throws SQLException {
        if (connection.isClosed()){
            LOGGER.info("Connection Closed! try to reconnect");
            reconnect();
            if (connection.isClosed()){
                LOGGER.info("Connection Closed!");
            }
        }
        return connection;
    }

    @Override
    public Connection getConnection(final String userName, final String password) throws SQLException {
        LOGGER.debug("Connection Opened!");
        user = userName;
        this.password=password;
        Class<?> clazz;
        try {
            clazz = Class.forName(driver);
            clazz.newInstance();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(),e);
        }
        connection = DriverManager.getConnection(url, userName, password);
        connection.setAutoCommit(true);

        return connection;
    }

    public void reconnect() {
        try {
            connection = DriverManager.getConnection(url, user,password);
        } catch (final SQLException e) {
            LOGGER.error(e.getMessage(),e);
        }

    }

    @Override
    public boolean isWrapperFor(final Class<?> clazz) throws SQLException {
        return false;
    }

    @Override
    public <T> T unwrap(final Class<T> clazz) throws SQLException {
        return null;
    }

    @Override
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }
}
