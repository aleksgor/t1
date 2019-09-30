package com.nomad.pm.util;

import java.util.HashMap;
import java.util.Map;

import com.nomad.pm.local.ConnectionPoolImpl;
import com.nomad.pm.local.JDBConnection;
import com.nomad.server.ConnectionPool;
import com.nomad.server.ServerContext;

public class ConnectPoolFactory {

    private volatile static Map<String, ConnectionPool<JDBConnection>> connections = new HashMap<>();


    public static ConnectionPool<JDBConnection> getConnectPool(final String name, final String url, final String user, final String password, final int threads,
            final ServerContext context, String driver) {
        ConnectionPool<JDBConnection> result = null;
        synchronized (connections) {
            result = connections.get(name);

            if(result==null){
                result = new ConnectionPoolImpl(url, user, password, threads, 2000, context, driver);
                connections.put(name, result);
            }
        }

        return result;
    }
}
