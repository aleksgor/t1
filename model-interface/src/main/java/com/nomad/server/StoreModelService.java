package com.nomad.server;

import com.nomad.model.ConnectModel;
import com.nomad.model.ServerModel;

public interface StoreModelService extends ServiceInterface {

    boolean isCache(String modelName);

    boolean isCache();

    boolean isCacheManager();

    boolean isCacheManager(String modelName);

    boolean isModelTranslator(String modelName);

    String getServerId();

    ServerModel getServerModel();

    void setServerModel(ServerModel serverModel);

    boolean contentsModel(String modelName);

    ConnectModel registerServer(ConnectModel colleague) ;

    ConnectModel unregisterClient(ConnectModel colleague);

    ConnectModel registerClient(ConnectModel colleague);

}
