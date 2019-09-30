package com.nomad.server;

import java.util.Collection;
import java.util.Map;

import com.nomad.exception.SystemException;
import com.nomad.model.ServerModel;
import com.nomad.server.processing.ProxyProcessingInterface;
import com.nomad.server.statistic.InformationPublisherService;
import com.nomad.utility.DataInvokerPool;

public interface ServerContext extends Map<String, Object> {
    public enum ServiceName {
        STORE_MODEL_SERVICE, INTERNAL_TRANSACT_DATA_STORE, PROXY_PLUGIN, OBJECT_PLUGINS, SESSION_SERVICE, CHILDREN_SERVICE, SAVE_SERVICE, BLOCK_SERVICE, LISTENERS, SCHEDULE_SERVICE, SESSION_CALLBACK_CLIENT, MANAGEMENT_SERVICE, EXECUTOR_PROVIDER_SERVICE, STATISTIC_PUBLISHER, PM_DATA_INVOKER, ID_GENERATOR_SERVICE

    }

    DataInvokerPool getDataInvoker(String name);

    void saveDataInvoker(String name, DataInvokerPool pool);

    Collection<DataInvokerPool> getAllDataInvokers();

    void addDataInvoker(String name, DataInvokerPool pool);

    Object get(ServiceName contextName);

    Object put(ServiceName contextName, Object value);

    Object remove(ServiceName contextName);

    @Override
    void clear();

    ServerModel getServerModel();

    SchedulerService getScheduledExecutorService();

    void close();

    DataDefinitionService getDataDefinitionService(String name) throws SystemException;

    void putDataDefinitionService(DataDefinitionService service, String name);

    ExecutorServiceProvider getExecutorServiceProvider();

    InformationPublisherService getInformationPublisherService();

    String getServerName();

    DataDefinitionService getDataDefinitionService(String name, String fileName) throws SystemException;

    PmDataInvoker getPmDataInvoker(String name);

    void putPmDataInvoker(String name, PmDataInvoker invoker);

    ProxyProcessingInterface getProxyProcessing(String name) throws SystemException;
}
