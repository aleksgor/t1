package com.nomad.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.nomad.model.command.CommandServerModel;
import com.nomad.model.command.CommandServerModelImpl;
import com.nomad.model.idgenerator.IdGeneratorClientModel;
import com.nomad.model.idgenerator.IdGeneratorClientModelImpl;
import com.nomad.model.idgenerator.IdGeneratorServerModel;
import com.nomad.model.idgenerator.IdGeneratorServerModelImpl;
import com.nomad.model.management.ManagementServerModel;
import com.nomad.model.management.ManagementServerModelImpl;
import com.nomad.model.saveserver.SaveClientModel;
import com.nomad.model.saveserver.SaveServerModel;
import com.nomad.model.session.SessionClientModel;
import com.nomad.model.session.SessionClientModelImpl;
import com.nomad.model.session.SessionServerModel;
import com.nomad.model.session.SessionServerModelImp;
import com.nomad.server.statistic.InformationPublisher;

@XmlRootElement(name="serverModel")
public class ServerModelImpl implements ServerModel {

    @XmlElementWrapper(name = "storeModels")
    @XmlElement(type = StoreModelImpl.class, name = "storeModel")
    private final List<StoreModel> storeModels = new ArrayList<>();

    @XmlElementWrapper(name = "listeners")
    @XmlElement(type = ListenerModelImpl.class, name = "listener")
    private final List<ListenerModel> listeners = new ArrayList<>();
    @XmlElementWrapper(name = "commandPlugins")
    @XmlElement(type = CommandPluginModelImpl.class, name = "commandPlugin")
    private final List<CommandPluginModel> commandPlugins = new ArrayList<>();
    @XmlElementWrapper(name = "servers")
    @XmlElement(type = ConnectModelImpl.class, name = "connect")
    private final List<ConnectModel> servers = new ArrayList<>();

    @XmlElementWrapper(name = "clients")
    @XmlElement(type = ConnectModelImpl.class, name = "connect")
    private final List<ConnectModel> clients = new ArrayList<>();
    private String serverName = "";
    private String pluginPath;

    @XmlElementWrapper(name = "dataSources")
    @XmlElement(type = DataSourceModelImpl.class, name = "dataSource")
    private final List<DataSourceModel> dataSources = new ArrayList<>();

    private final Properties properties = new Properties();

    private SessionServerModel sessionServerModel;
    private SessionCallBackServerModel sessionCallBackServerModel;

    @XmlElementWrapper(name = "sessionClients")
    @XmlElement(type = SessionClientModelImpl.class, name = "sessionClient")
    private final List<SessionClientModel> sessionClientModels = new ArrayList<>();

    // TODO only one!!!
    @XmlElementWrapper(name = "saveServers")
    @XmlElement(type = SaveServerModelImpl.class, name = "saveServer")
    private final List<SaveServerModel> saveServerModels = new ArrayList<>();

    @XmlElementWrapper(name = "saveClients")
    @XmlElement(type = SaveClientModelImpl.class, name = "saveClient")
    private final List<SaveClientModel> saveClientModels = new ArrayList<>();
    // id generator
    @XmlElementWrapper(name = "idGeneratorClients")
    @XmlElement(type = IdGeneratorClientModelImpl.class, name = "idGeneratorClient")
    private final List<IdGeneratorClientModel> idGeneratorClientModels = new ArrayList<>();

    private IdGeneratorServerModel idGeneratorServerModel;

    private boolean trustSessions = true;
    // id generator

    private InformationPublisher statisticPublisher;

    private boolean calculateStatistic = true;

    private int maxExecThreads = 50;

    @XmlElementWrapper(name = "serializers")
    @XmlElement(type = SerializerImpl.class, name = "serializer")
    final private List<Serializer> serializers = new ArrayList<>();
    // command
    private CommandServerModel commandServerModel;
    // management
    private ManagementServerModel managementServerModel;

    private boolean localSessions = false;
    private long serverId;

    @Override
    public int getMaxExecThreads() {
        return maxExecThreads;
    }

    @Override
    public void setMaxExecThreads(int maxExecThread) {
        this.maxExecThreads = maxExecThread;
    }


    @Override
    public boolean isTrustSessions() {
        return trustSessions;
    }

    @Override
    public void setTrustSessions(final boolean trustSessions) {
        this.trustSessions = trustSessions;
    }

    @Override
    public boolean isLocalSessions() {
        return localSessions;
    }

    @Override
    public void setLocalSessions(final boolean localSessions) {
        this.localSessions = localSessions;
    }

    @Override
    public List<SessionClientModel> getSessionClientModels() {
        return sessionClientModels;
    }

    @Override
    public SessionServerModel getSessionServerModel() {
        return sessionServerModel;
    }

    @Override
    @XmlElement( name = "sessionServer", type = SessionServerModelImp.class)
    public void setSessionServerModel(final SessionServerModel sessionServer) {
        sessionServerModel = sessionServer;
    }

    @Override
    public String getServerName() {
        return serverName;
    }

    @Override
    public void setServerName(final String serverName) {
        this.serverName = serverName;
    }

    @Override
    public List<CommandPluginModel> getCommandPlugins() {
        return commandPlugins;
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public List<DataSourceModel> getDataSources() {
        return dataSources;
    }

    @Override
    public DataSourceModel getDataSources(final String dataSourceName) {
        for (final DataSourceModel dataSource : dataSources) {
            if (dataSource.getName().equals(dataSourceName)) {
                return dataSource;
            }
        }
        return null;
    }

    @Override
    public void addDataSources(final DataSourceModel dataInvoker) {
        dataSources.add(dataInvoker);
    }

    @Override
    public List<ConnectModel> getClients() {
        return clients;
    }

    @Override
    public List<ConnectModel> getServers() {
        return servers;
    }

    @Override
    public String getPluginPath() {
        return pluginPath;
    }

    @Override
    public void setPluginPath(final String pluginPath) {
        this.pluginPath = pluginPath;
    }

    @Override
    public List<StoreModel> getStoreModels() {
        return storeModels;
    }

    @Override
    public StoreModel getStoreModel(String name) {
        for (StoreModel storeModel : storeModels) {
            if (storeModel.getModel().equals(name)) {
                return storeModel;
            }
        }
        return null;
    }

    @Override
    public List<ListenerModel> getListeners() {
        return listeners;
    }

    @Override
    public List<Serializer> getSerializers() {
        return serializers;
    }

    @Override
    public boolean isCalculateStatistic() {
        return calculateStatistic;
    }

    @Override
    public void setCalculateStatistic(final boolean calculateStatistic) {
        this.calculateStatistic = calculateStatistic;
    }

    @Override
    @XmlElement(type = SessionCallBackServerModelImp.class)
    public SessionCallBackServerModel getSessionCallBackServerModel() {
        return sessionCallBackServerModel;
    }

    @Override
    public void setSessionCallBackServerModel(final SessionCallBackServerModel sessionCallBackServerModel) {
        this.sessionCallBackServerModel = sessionCallBackServerModel;
    }


    @Override
    @XmlElement(type = CommandServerModelImpl.class)
    public CommandServerModel getCommandServerModel() {
        return commandServerModel;
    }

    @Override
    public void setCommandServerModel(final CommandServerModel commandServerModel) {
        this.commandServerModel = commandServerModel;
    }

    @Override
    public ManagementServerModel getManagementServerModel() {
        return managementServerModel;
    }


    @Override
    @XmlElement( name = "managementServer", type = ManagementServerModelImpl.class)
    public void setManagementServerModel(final ManagementServerModel managementServerModel) {
        this.managementServerModel = managementServerModel;
    }

    @Override
    public List<SaveServerModel> getSaveServerModels() {
        return saveServerModels;
    }

    @Override
    public List<SaveClientModel> getSaveClientModels() {
        return saveClientModels;
    }

    @Override
    public long getServerId() {
        return serverId;
    }

    @Override
    public void setServerId(final long serverId) {
        this.serverId = serverId;
    }

    @Override
    @XmlElement(type = InformationPublisherImpl.class)
    public InformationPublisher getStatisticPublisher() {
        return statisticPublisher;
    }

    @Override
    public void setStatisticPublisher(InformationPublisher statisticPublisher) {
        this.statisticPublisher = statisticPublisher;
    }

    @Override
    @XmlElement(type = IdGeneratorServerModelImpl.class, name = "idGeneratorServer")
    public IdGeneratorServerModel getIdGeneratorServerModel() {
        return idGeneratorServerModel;
    }

    @Override
    public void setIdGeneratorServerModel(IdGeneratorServerModel idGeneratorServerModel) {
        this.idGeneratorServerModel = idGeneratorServerModel;
    }

    @Override
    public List<IdGeneratorClientModel> getIdGeneratorClientModels() {
        return idGeneratorClientModels;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (calculateStatistic ? 1231 : 1237);
        result = prime * result + ((clients == null) ? 0 : clients.hashCode());
        result = prime * result + ((commandPlugins == null) ? 0 : commandPlugins.hashCode());
        result = prime * result + ((commandServerModel == null) ? 0 : commandServerModel.hashCode());
        result = prime * result + ((dataSources == null) ? 0 : dataSources.hashCode());
        result = prime * result + ((idGeneratorClientModels == null) ? 0 : idGeneratorClientModels.hashCode());
        result = prime * result + ((idGeneratorServerModel == null) ? 0 : idGeneratorServerModel.hashCode());
        result = prime * result + ((listeners == null) ? 0 : listeners.hashCode());
        result = prime * result + (localSessions ? 1231 : 1237);
        result = prime * result + ((managementServerModel == null) ? 0 : managementServerModel.hashCode());
        result = prime * result + maxExecThreads;
        result = prime * result + ((pluginPath == null) ? 0 : pluginPath.hashCode());
        result = prime * result + ((properties == null) ? 0 : properties.hashCode());
        result = prime * result + ((saveClientModels == null) ? 0 : saveClientModels.hashCode());
        result = prime * result + ((saveServerModels == null) ? 0 : saveServerModels.hashCode());
        result = prime * result + ((serializers == null) ? 0 : serializers.hashCode());
        result = prime * result + (int) (serverId ^ (serverId >>> 32));
        result = prime * result + ((serverName == null) ? 0 : serverName.hashCode());
        result = prime * result + ((servers == null) ? 0 : servers.hashCode());
        result = prime * result + ((sessionCallBackServerModel == null) ? 0 : sessionCallBackServerModel.hashCode());
        result = prime * result + ((sessionClientModels == null) ? 0 : sessionClientModels.hashCode());
        result = prime * result + ((sessionServerModel == null) ? 0 : sessionServerModel.hashCode());
        result = prime * result + ((statisticPublisher == null) ? 0 : statisticPublisher.hashCode());
        result = prime * result + ((storeModels == null) ? 0 : storeModels.hashCode());
        result = prime * result + (trustSessions ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ServerModelImpl other = (ServerModelImpl) obj;
        if (calculateStatistic != other.calculateStatistic)
            return false;
        if (clients == null) {
            if (other.clients != null)
                return false;
        } else if (!clients.equals(other.clients))
            return false;
        if (commandPlugins == null) {
            if (other.commandPlugins != null)
                return false;
        } else if (!commandPlugins.equals(other.commandPlugins))
            return false;
        if (commandServerModel == null) {
            if (other.commandServerModel != null)
                return false;
        } else if (!commandServerModel.equals(other.commandServerModel))
            return false;
        if (dataSources == null) {
            if (other.dataSources != null)
                return false;
        } else if (!dataSources.equals(other.dataSources))
            return false;
        if (idGeneratorClientModels == null) {
            if (other.idGeneratorClientModels != null)
                return false;
        } else if (!idGeneratorClientModels.equals(other.idGeneratorClientModels))
            return false;
        if (idGeneratorServerModel == null) {
            if (other.idGeneratorServerModel != null)
                return false;
        } else if (!idGeneratorServerModel.equals(other.idGeneratorServerModel))
            return false;
        if (listeners == null) {
            if (other.listeners != null)
                return false;
        } else if (!listeners.equals(other.listeners))
            return false;
        if (localSessions != other.localSessions)
            return false;
        if (managementServerModel == null) {
            if (other.managementServerModel != null)
                return false;
        } else if (!managementServerModel.equals(other.managementServerModel))
            return false;
        if (maxExecThreads != other.maxExecThreads)
            return false;
        if (pluginPath == null) {
            if (other.pluginPath != null)
                return false;
        } else if (!pluginPath.equals(other.pluginPath))
            return false;
        if (properties == null) {
            if (other.properties != null)
                return false;
        } else if (!properties.equals(other.properties))
            return false;
        if (saveClientModels == null) {
            if (other.saveClientModels != null)
                return false;
        } else if (!saveClientModels.equals(other.saveClientModels))
            return false;
        if (saveServerModels == null) {
            if (other.saveServerModels != null)
                return false;
        } else if (!saveServerModels.equals(other.saveServerModels))
            return false;
        if (serializers == null) {
            if (other.serializers != null)
                return false;
        } else if (!serializers.equals(other.serializers))
            return false;
        if (serverId != other.serverId)
            return false;
        if (serverName == null) {
            if (other.serverName != null)
                return false;
        } else if (!serverName.equals(other.serverName))
            return false;
        if (servers == null) {
            if (other.servers != null)
                return false;
        } else if (!servers.equals(other.servers))
            return false;
        if (sessionCallBackServerModel == null) {
            if (other.sessionCallBackServerModel != null)
                return false;
        } else if (!sessionCallBackServerModel.equals(other.sessionCallBackServerModel))
            return false;
        if (sessionClientModels == null) {
            if (other.sessionClientModels != null)
                return false;
        } else if (!sessionClientModels.equals(other.sessionClientModels))
            return false;
        if (sessionServerModel == null) {
            if (other.sessionServerModel != null)
                return false;
        } else if (!sessionServerModel.equals(other.sessionServerModel))
            return false;
        if (statisticPublisher == null) {
            if (other.statisticPublisher != null)
                return false;
        } else if (!statisticPublisher.equals(other.statisticPublisher))
            return false;
        if (storeModels == null) {
            if (other.storeModels != null)
                return false;
        } else if (!storeModels.equals(other.storeModels))
            return false;
        if (trustSessions != other.trustSessions)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ServerModelImpl [storeModels=" + storeModels + ", listeners=" + listeners + ", commandPlugins=" + commandPlugins + ", servers=" + servers + ", clients=" + clients
                + ", serverName=" + serverName + ", pluginPath=" + pluginPath + ", dataSources=" + dataSources + ", properties=" + properties
                + ", sessionServerModel=" + sessionServerModel + ", sessionCallBackServerModel=" + sessionCallBackServerModel + ", sessionClientModels=" + sessionClientModels
                + ", saveServerModels=" + saveServerModels + ", saveClientModels=" + saveClientModels + ", trustSessions=" + trustSessions + ", statisticPublisher="
                + statisticPublisher + ", calculateStatistic=" + calculateStatistic + ", maxExecThreads=" + maxExecThreads + ", serializers=" + serializers
                + ", commandServerModel=" + commandServerModel + ", managementServerModel=" + managementServerModel + ", localSessions=" + localSessions + ", serverId=" + serverId
                + ", idGeneratorClientModels=" + idGeneratorClientModels + " idGeneratorServerModel" + idGeneratorServerModel + "]";
    }


}
