package com.nomad.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "connect")
public class ConnectModelImpl implements ConnectModel {

    private ConnectStatus status = ConnectStatus.UNKNOWN;
    private CommonClientModel clientManagementServer;
    private CommonClientModel clientManagementClient;
    private ListenerModel listener;
    private int threads;

    @XmlTransient
    private List<String> dataSources;

    @XmlTransient
    private final List<StoreModel> storeModels = new ArrayList<>();

    public ConnectModelImpl() {

    }

    @Override
    public int getThreads() {
        return threads;
    }

    @Override
    public void setThreads(final int threads) {
        this.threads = threads;
    }

    @Override
    public ConnectStatus getStatus() {
        return status;
    }

    @Override
    public void setStatus(final ConnectStatus status) {
        this.status = status;
    }

    @Override
    @XmlElement(type = CommonClientModelImpl.class, name = "managementServer")
    public CommonClientModel getManagementServer() {
        return clientManagementServer;
    }

    @Override
    public void setManagementServer(final CommonClientModel managementServer) {
        clientManagementServer = managementServer;
    }

    @Override
    @XmlElement(type = CommonClientModelImpl.class, name = "managementClient")
    public CommonClientModel getManagementClient() {
        return clientManagementClient;
    }

    @Override
    public void setManagementClient(final CommonClientModel clientManagementClient) {
        this.clientManagementClient = clientManagementClient;
    }

    @Override
    @XmlElement(type = ListenerModelImpl.class, name = "listener")
    public ListenerModel getListener() {
        return listener;
    }

    @Override
    public void setListener(final ListenerModel listener) {
        this.listener = listener;
    }

    @Override
    public List<String> getDataSources() {
        return dataSources;
    }

    @Override
    public void setDataSources(List<String> dataSources) {
        this.dataSources = dataSources;
    }

    @Override
    public List<StoreModel> getStoreModels() {
        return storeModels;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((listener == null) ? 0 : listener.hashCode());
        result = prime * result + ((clientManagementClient == null) ? 0 : clientManagementClient.hashCode());
        result = prime * result + ((clientManagementServer == null) ? 0 : clientManagementServer.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + threads;
        return result;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object)
            return true;
        if (object == null)
            return false;
        if (getClass() != object.getClass())
            return false;
        final ConnectModelImpl other = (ConnectModelImpl) object;
        if (listener == null) {
            if (other.listener != null)
                return false;
        } else if (!listener.equals(other.listener))
            return false;
        if (clientManagementClient == null) {
            if (other.clientManagementClient != null)
                return false;
        } else if (!clientManagementClient.equals(other.clientManagementClient))
            return false;
        if (clientManagementServer == null) {
            if (other.clientManagementServer != null)
                return false;
        } else if (!clientManagementServer.equals(other.clientManagementServer))
            return false;
        if (status != other.status)
            return false;
        if (threads != other.threads)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ConnectModelImpl [status=" + status + ", clientManagementServer=" + clientManagementServer + ", clientManagementClient=" + clientManagementClient + ", listener=" + listener + ", threads=" + threads + ", dataSources="
                + dataSources + ", storeModels=" + storeModels + "]";
    }


}
