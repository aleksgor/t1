package com.nomad.server;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.core.ExecutorServiceProviderImpl;
import com.nomad.core.ProxyProcessing;
import com.nomad.exception.SystemException;
import com.nomad.model.ServerModel;
import com.nomad.server.processing.ProxyProcessingInterface;
import com.nomad.utility.DataInvokerPool;
import com.nomad.utility.SimpleServerContext;

@SuppressWarnings("serial")
public class ServerContextImpl extends SimpleServerContext {
    @SuppressWarnings("unused")
    private static Logger LOGGER = LoggerFactory.getLogger(ServerContextImpl.class);

    private volatile Map<String, DataInvokerPool> dataInvokerList = new HashMap<>();

    @Override
    public DataInvokerPool getDataInvoker(final String name) {
        return dataInvokerList.get(name);
    }

    @Override
    public void saveDataInvoker(final String name, final DataInvokerPool pool) {
        dataInvokerList.put(name, pool);
    }

    @Override
    public void addDataInvoker(final String name, final DataInvokerPool pool) {
        dataInvokerList.put(name, pool);
    }


    @Override
    public Object put(final ServiceName contextName, final Object value) {
        return put(contextName.toString(), value);
    }

    @Override
    public Collection<DataInvokerPool> getAllDataInvokers() {
        return dataInvokerList.values();
    }

    @Override
    public ServerModel getServerModel() {
        final StoreModelService server = (StoreModelService) get(ServerContext.ServiceName.STORE_MODEL_SERVICE);
        if (server == null) {
            return null;
        }
        return server.getServerModel();
    }

    @Override
    public void close() {
        for (final DataInvokerPool dataInvokerPool : dataInvokerList.values()) {
            dataInvokerPool.close();
        }
        for (final Object object : values()) {
            if (object instanceof ServiceInterface) {
                final ServiceInterface service = (ServiceInterface) object;
                service.stop();
            }
        }

    }

    @Override
    public synchronized ExecutorServiceProvider getExecutorServiceProvider() {
        ExecutorServiceProvider executorServiceProvider = (ExecutorServiceProvider) get(ServiceName.EXECUTOR_PROVIDER_SERVICE.name());
        if (executorServiceProvider == null) {
            ServerModel model = getServerModel();
            int count = 50;
            if (model != null) {
                count = model.getMaxExecThreads();
            }
            executorServiceProvider = new ExecutorServiceProviderImpl("ExecutorServiceProvider", count);
            put(ServiceName.EXECUTOR_PROVIDER_SERVICE.name(), executorServiceProvider);
        }
        return executorServiceProvider;
    }

    @Override
    public ProxyProcessingInterface getProxyProcessing(String name) throws  SystemException {
        return new ProxyProcessing(this, name);
    }

}
