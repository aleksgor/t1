package com.nomad.pm.util;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.Vector;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ConnectPool  {


    private  static Logger LOGGER = LoggerFactory.getLogger(ConnectPool.class);
    private volatile Vector<Connection> freeConnects;
    private final int timeout = 10 * 1000;
    private final String user;
    private final String  password;
    private final DataSource dataSource;

    public ConnectPool(final int threads, final String user, final String password, final DataSource dataSource) throws SQLException {
        this.user=user;
        this.password=password;
        this.dataSource = dataSource;
        freeConnects = new Vector<>(threads);
        LOGGER.info("connectPool init:{}",threads);
        for (int i = 0; i < threads; i++) {
            freeConnects.add(dataSource.getConnection(user, password));
        }
    }

    public Connection getConnect() {
        final long start = System.currentTimeMillis();
        Connection result =null;
        while ((result = getConnect0()) == null && (System.currentTimeMillis() - start) < timeout) {
            try {
                Thread.sleep(10);
            } catch (final InterruptedException e) {
                LOGGER.error("cannot sleep", e);
            }
        }

        try {
            if (result.isClosed()){
                result = dataSource.getConnection(user, password);
            }
        } catch (final SQLException e) {
            LOGGER.error(e.getMessage(),e);
        }
        return result;

    }

    private synchronized Connection getConnect0() {
        if(freeConnects.size()<=0){
            return null;
        }
        final Connection result = freeConnects.remove(0);
        return result;
    }

    public synchronized void returnConnect(final Connection connect) {

        freeConnects.add(connect);

    }

    public void closeConnection() {
        for (final Connection cnt : freeConnects) {
            try {
                cnt.close();
            } catch (final SQLException e) {
                LOGGER.error(e.getMessage(),e);
            }
        }

    }



}
