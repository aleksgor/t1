package com.nomad.server.service.management;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.nomad.cache.commonclientserver.ManagementMessageImpl;
import com.nomad.exception.SystemException;
import com.nomad.message.ManagementMessage;
import com.nomad.model.CommonClientModel;
import com.nomad.model.management.ManagementClientModel;
import com.nomad.model.management.ManagementClientModelImpl;
import com.nomad.server.ServerContext;
import com.nomad.server.managementserver.ManagementClientConnectionPoolImpl;
import com.nomad.server.service.ManagementClientConnectionPool;
import com.nomad.server.service.ManagementService;

public class ManagementServiceImpl implements ManagementService {
    private final Map<String, ManagementClientConnectionPool> pools = new ConcurrentHashMap<String, ManagementClientConnectionPool>();
    private final ServerContext context;

    public ManagementServiceImpl(final ServerContext context) {
        this.context = context;
    }

    @Override
    public void start() throws SystemException {

    }

    @Override
    public ManagementClientConnectionPool getClientPool(final CommonClientModel server) {
        if (server != null) {
            final String serverName = server.getHost() + ":" + server.getPort();
            ManagementClientConnectionPool pool = pools.get(serverName);
            if (pool == null) {
                final ManagementClientModel mcm = new ManagementClientModelImpl();
                mcm.setHost(server.getHost());
                mcm.setPort(server.getPort());
                mcm.setProtocolType(server.getProtocolType());
                mcm.setThreads(server.getThreads());
                mcm.setTimeout(server.getTimeout());
                pool = new ManagementClientConnectionPoolImpl(mcm, context);
                pools.put(serverName, pool);
            }
            return pool;
        }
        return null;
    }

    @Override
    public void stop() {
        for (final ManagementClientConnectionPool pool : pools.values()) {
            pool.stop();
        }
    }

    @Override
    public ManagementMessage getCommand(final String command, final File f) throws SystemException {

        final ManagementMessage result = new ManagementMessageImpl();
        result.setCommand(command);
        result.setModelName(f.getName());

        final byte[] ab = new byte[1024];
        int counter = 0;
        try {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream((int) f.length()); InputStream ins = new FileInputStream(f)) {
                while ((counter = ins.read(ab)) >= 0) {
                    out.write(ab, 0, counter);
                }

                out.flush();
            }
            result.setData(ab);
            return result;
        } catch (IOException e) {
            throw new SystemException(e);
        }
    }
}
