package com.nomad.model;

import java.util.List;

public interface ConnectModel {
    int getThreads();

    void setThreads(int threads);

    ConnectStatus getStatus();

    void setStatus(ConnectStatus status);

    CommonClientModel getManagementServer();

    void setManagementServer(CommonClientModel managementServer);

    CommonClientModel getManagementClient();

    void setManagementClient(CommonClientModel managementClient);

    ListenerModel getListener();

    void setListener(ListenerModel listener);

    List<String> getDataSources();

    void setDataSources(List<String> dataSources);

    List<StoreModel> getStoreModels();

}
