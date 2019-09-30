package com.nomad.utility;

import java.util.Collection;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.datadefinition.DataDefinitionServiceImpl;
import com.nomad.datadefinition.DataDefinitionStoreService;
import com.nomad.exception.SystemException;
import com.nomad.model.ServerModel;
import com.nomad.server.DataDefinitionService;
import com.nomad.server.ExecutorServiceProvider;
import com.nomad.server.PmDataInvoker;
import com.nomad.server.SchedulerService;
import com.nomad.server.SchedulerServiceImpl;
import com.nomad.server.ServerContext;
import com.nomad.server.processing.ProxyProcessingInterface;
import com.nomad.server.statistic.InformationPublisher;
import com.nomad.server.statistic.InformationPublisherService;
import com.nomad.server.statistic.service.InformationPublisherServiceImpl;

public class SimpleServerContext extends HashMap<String, Object> implements ServerContext {
    private static Logger LOGGER = LoggerFactory.getLogger(SimpleServerContext.class);

    DataDefinitionStoreService dataDefinitionStoreService;

    public SimpleServerContext() {
    }
    @Override
    public DataInvokerPool getDataInvoker(String name) {
        return null;
    }

    @Override
    public void saveDataInvoker(String name, DataInvokerPool pool) {

    }

    @Override
    public Collection<DataInvokerPool> getAllDataInvokers() {
        return null;
    }

    @Override
    public void addDataInvoker(String name, DataInvokerPool pool) {

    }

    @Override
    public Object get(ServiceName contextName) {
        return super.get(contextName.name());
    }

    @Override
    public Object remove(ServiceName contextName) {
        return super.remove(contextName.name());
    }

    @Override
    public void clear() {
        super.clear();
    }

    @Override
    public ServerModel getServerModel() {
        return null;
    }

    @Override
    public SchedulerService getScheduledExecutorService() {

        final ServerModel serverModel = getServerModel();
        synchronized (this) {
            SchedulerService scheduler = (SchedulerService) get(ServiceName.SCHEDULE_SERVICE);

            if (scheduler == null) {
                if (serverModel == null) {
                    scheduler = new SchedulerServiceImpl("No name scheduler 2");
                } else {
                    scheduler = new SchedulerServiceImpl(serverModel.getServerName());
                }
                put(ServiceName.SCHEDULE_SERVICE.name(), scheduler);
            }
            return scheduler;
        }
    }

    @Override
    public ProxyProcessingInterface getProxyProcessing(String name) throws SystemException {
        return null;
    }

    @Override
    public void close() {
        SchedulerService scheduler = (SchedulerService) get(ServiceName.SCHEDULE_SERVICE.name());
        if (scheduler != null) {
            scheduler.stop();
        }

    }

    @Override
    public DataDefinitionService getDataDefinitionService(String name) throws SystemException {
        if (name == null) {
            name = "";
        }
        if (dataDefinitionStoreService == null) {
            dataDefinitionStoreService = new DataDefinitionStoreService();
        }
        if (dataDefinitionStoreService.getDataDefinitionService(name) == null) {
            DataDefinitionService dataDefinitionService = new DataDefinitionServiceImpl(name, null, null);
            dataDefinitionStoreService.putDataDefinitionService(name, dataDefinitionService);
            dataDefinitionService.start();
        }
        return dataDefinitionStoreService.getDataDefinitionService(name);
    }

    @Override
    public DataDefinitionService getDataDefinitionService(String name, String fileName) throws SystemException {
        if (name == null) {
            name = "";
        }
        if (dataDefinitionStoreService == null) {
            dataDefinitionStoreService = new DataDefinitionStoreService();
        }
        if (dataDefinitionStoreService.getDataDefinitionService(name) == null) {
            DataDefinitionService dataDefinitionService = new DataDefinitionServiceImpl(name, fileName, null);
            dataDefinitionStoreService.putDataDefinitionService(name, dataDefinitionService);
            dataDefinitionService.start();
        }
        return dataDefinitionStoreService.getDataDefinitionService(name);
    }

    @Override
    public void putDataDefinitionService(DataDefinitionService dataDefinitionService, String name) {
        if (name == null) {
            name = "";
        }
        if (dataDefinitionStoreService == null) {
            dataDefinitionStoreService = new DataDefinitionStoreService();
        }
        dataDefinitionStoreService.putDataDefinitionService(name, dataDefinitionService);
    }

    @Override
    public Object put(ServiceName contextName, Object value) {
        return super.put(contextName.name(), value);
    }

    @Override
    public ExecutorServiceProvider getExecutorServiceProvider() {
        return null;
    }

    @Override
    public InformationPublisherService getInformationPublisherService() {
        InformationPublisherService result = (InformationPublisherService) get(ServiceName.STATISTIC_PUBLISHER.name());
        if (result == null) {
            InformationPublisher model = null;
            ServerModel serverModel = getServerModel();
            if (serverModel != null) {
                model = serverModel.getStatisticPublisher();
            }

            try {
                result = new InformationPublisherServiceImpl(model);
                put(ServiceName.STATISTIC_PUBLISHER, result);
            } catch (SystemException e) {
                LOGGER.error(e.getMessage());
            }
        }
        return result;
    }

    @Override
    public String getServerName() {
        ServerModel serverModel = getServerModel();
        if (serverModel != null) {
            return serverModel.getServerName();
        }
        return "";
    }

    @Override
    public PmDataInvoker getPmDataInvoker(String name) {
        NamedPmDataInvoker store = getNamedPmDataInvoker();
        return store.getPmDataInvoker(name);
    }

    @Override
    public void putPmDataInvoker(String name, PmDataInvoker invoker){
        NamedPmDataInvoker store=getNamedPmDataInvoker();
        store.putPmDataInvoker(name, invoker);

    }

    private NamedPmDataInvoker getNamedPmDataInvoker() {
        NamedPmDataInvoker store = (NamedPmDataInvoker) get(ServiceName.PM_DATA_INVOKER);
        if(store==null){
            store= new NamedPmDataInvoker();
            put(ServiceName.PM_DATA_INVOKER.name(), store);
        }
        return store;
    }
}
