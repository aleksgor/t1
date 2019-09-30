package com.nomad.server.service.storemodelservice;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.nomad.model.ConnectModel;
import com.nomad.model.ServerModel;
import com.nomad.model.StoreModel;
import com.nomad.model.StoreModel.ServerType;
import com.nomad.server.StoreModelService;

public class StoreModelServiceImpl implements StoreModelService {
    private ServerModel serverModel;
    private HashMap<String, StoreModel> storeModels = new HashMap<>();
    private Boolean cacheManager = null;
    private Boolean cache = null;

    @Override
    public boolean isCache(final String modelName) {
        final StoreModel storeModel = storeModels.get(modelName);
        if (storeModel == null) {
            return isCache();
        }
        return (storeModel.isCache());
    }

    @Override
    public boolean isCache() {
        if (cache == null) {
            for (final StoreModel sm : storeModels.values()) {
                if (sm.isCache()) {
                    cache = Boolean.TRUE;
                    return true;
                }
            }
            cache = Boolean.FALSE;
        }
        return cache;
    }

    @Override
    public boolean isCacheManager() {
        if (cacheManager == null) {
            for (final StoreModel sm : storeModels.values()) {
                if (sm.isCacheManager()) {
                    cacheManager = Boolean.TRUE;
                    return true;
                }
            }
            cacheManager = Boolean.FALSE;

        }
        return cacheManager;
    }

    @Override
    public boolean isCacheManager(final String modelName) {

        final StoreModel storeModel = storeModels.get(modelName);
        if (storeModel == null) {
            return isCacheManager();
        }
        return storeModel.isCacheManager();
    }



    @Override
    public boolean isModelTranslator(final String modelName) {
        final StoreModel storeModel = storeModels.get(modelName);
        if (storeModel == null) {
            return false;
        }
        return storeModel.isTranslator();
    }

    public StoreModelServiceImpl(final ServerModel serverModel) {
        this.serverModel = serverModel;

    }

    @Override
    public String getServerId() {
        try {
            return InetAddress.getLocalHost().getHostName() + ":" + serverModel.getManagementServerModel().getPort();
        } catch (final UnknownHostException e) {
        }
        return "localhost:" + serverModel.getManagementServerModel().getPort();
    }

    @Override
    public ServerModel getServerModel() {
        return serverModel;
    }

    @Override
    public void setServerModel(final ServerModel serverModel) {
        this.serverModel = serverModel;

        final HashMap<String, StoreModel> storeModels = new HashMap<>();

        for (final StoreModel storeModel : serverModel.getStoreModels()) {
            storeModels.put(storeModel.getModel(), storeModel);
        }
        this.storeModels = storeModels;
    }

    @Override
    public boolean contentsModel(final String modelName) {
        return storeModels.containsKey(modelName);
    }

    @Override
    public ConnectModel registerServer(final ConnectModel colleague)  {
        final List<ConnectModel> clients = serverModel.getServers();
        if (!searchInList(clients, colleague)) {
            clients.add(colleague);
        }
        for (StoreModel sm : colleague.getStoreModels()) {
            StoreModel smLocal = storeModels.get(sm.getModel());
            if(smLocal == null){
                sm.setServerType(ServerType.CACHE_MANAGER);
                storeModels.put(sm.getModel(), sm);
            }else{
                if(ServerType.CACHE.equals(smLocal.getServerType())){
                    smLocal.setServerType(ServerType.CACHE_CACHE_MANAGER);
                }
            }
        }

        return colleague;
    }

    private boolean searchInList(final Collection<ConnectModel> list, final ConnectModel colleague) {
        for (final ConnectModel client : list) {
            if (colleague.getListener().getHost().equals(client.getListener().getHost()) && colleague.getListener() == client.getListener()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ConnectModel unregisterClient(final ConnectModel colleague) {
        final List<ConnectModel> clients = serverModel.getServers();
        final List<ConnectModel> newClients = new ArrayList<>(clients.size());
        for (final ConnectModel client : clients) {
            if (!colleague.getListener().equals(client.getListener())) {
                newClients.add(client);
            }
        }
        serverModel.getServers().clear();
        serverModel.getServers().addAll(newClients);
        return colleague;
    }

    @Override
    public ConnectModel registerClient(final ConnectModel colleague) {
        final List<ConnectModel> clients = serverModel.getClients();
        for (final ConnectModel client : clients) {
            if (colleague.getListener().getHost().equals(client.getListener().getHost()) && colleague.getListener() == client.getListener()) {
                if (colleague.getThreads() != client.getThreads()) {
                    client.setThreads(colleague.getThreads());
                }
                return client;
            }
        }
        clients.add(colleague);
        return colleague;
    }


    @Override
    public void start() {
        serverModel.getStoreModels();
        for (final StoreModel storeModel : serverModel.getStoreModels()) {
            storeModels.put(storeModel.getModel(), storeModel);
        }
        cacheManager = null;
        cache = null;

    }

    @Override
    public void stop() {
        storeModels.clear();

    }
}
