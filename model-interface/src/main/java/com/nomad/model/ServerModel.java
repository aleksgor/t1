package com.nomad.model;

import java.util.List;
import java.util.Properties;

import com.nomad.model.command.CommandServerModel;
import com.nomad.model.idgenerator.IdGeneratorClientModel;
import com.nomad.model.idgenerator.IdGeneratorServerModel;
import com.nomad.model.management.ManagementServerModel;
import com.nomad.model.saveserver.SaveClientModel;
import com.nomad.model.saveserver.SaveServerModel;
import com.nomad.model.session.SessionClientModel;
import com.nomad.model.session.SessionServerModel;
import com.nomad.server.statistic.InformationPublisher;

public interface ServerModel {
    boolean isTrustSessions();

    void setTrustSessions(final boolean trustSessions);

    boolean isLocalSessions();

    void setLocalSessions(final boolean localSessions);

    List<SessionClientModel> getSessionClientModels();

    SessionServerModel getSessionServerModel();

    void setSessionServerModel(final SessionServerModel sessionServer);

    String getServerName();

    void setServerName(final String serverName);

    List<CommandPluginModel> getCommandPlugins();

    Properties getProperties();

    DataSourceModel getDataSources(final String dataSourceName);

    List<DataSourceModel> getDataSources();

    void addDataSources(final DataSourceModel dataInvoker);

    List<ConnectModel> getClients();

    List<ConnectModel> getServers();

    String getPluginPath();

    void setPluginPath(final String pluginPath);

    List<StoreModel> getStoreModels();

    List<ListenerModel> getListeners();

    List<Serializer> getSerializers();

    boolean isCalculateStatistic();

    void setCalculateStatistic(final boolean calculateStatistic);

    SessionCallBackServerModel getSessionCallBackServerModel();

    void setSessionCallBackServerModel(final SessionCallBackServerModel sessionCallBackServerModel);

    CommandServerModel getCommandServerModel();

    void setCommandServerModel(final CommandServerModel commandServerModel);

    ManagementServerModel getManagementServerModel();

    void setManagementServerModel(final ManagementServerModel managementServerModel);

    List<SaveServerModel> getSaveServerModels();

    List<SaveClientModel> getSaveClientModels();

    long getServerId();

    void setServerId(long serverId);

    void setMaxExecThreads(int maxExecThread);

    int getMaxExecThreads();

    InformationPublisher getStatisticPublisher();

    void setStatisticPublisher(InformationPublisher statisticPublisher);

    StoreModel getStoreModel(String name);

    IdGeneratorServerModel getIdGeneratorServerModel();

    void setIdGeneratorServerModel(IdGeneratorServerModel idGeneratorServerModel);

    List<IdGeneratorClientModel> getIdGeneratorClientModels();

}
