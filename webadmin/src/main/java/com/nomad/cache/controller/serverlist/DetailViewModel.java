package com.nomad.cache.controller.serverlist;

import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanOperationInfo;
import javax.management.ObjectName;

public class DetailViewModel {
    private String host;
    private int port;
    private String type;
    private String name;
    private String detail;
    private final List<DetailViewModel> servers = new ArrayList<>();
    private Object  bean;
    private String id;
    private MBeanOperationInfo[] operations=new MBeanOperationInfo[0];
    private ObjectName objectName;
    private Object data;
    private final String fullName;

    public DetailViewModel(String id, String fullName){
        this.id=id;
        this.fullName=fullName;
    }
    public String getId() {
        return id;
    }

    public ObjectName getObjectName() {
        return objectName;
    }
    public void setObjectName(ObjectName objectName) {
        this.objectName = objectName;
    }
    public Object getBean() {
        return bean;
    }
    public void setBean(Object bean) {
        this.bean = bean;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DetailViewModel> getServers() {
        return servers;
    }

    public MBeanOperationInfo[] getOperations() {
        return operations;
    }
    public void setOperations(MBeanOperationInfo[] operations) {
        this.operations = operations;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((data == null) ? 0 : data.hashCode());
        result = prime * result + ((detail == null) ? 0 : detail.hashCode());
        result = prime * result + ((host == null) ? 0 : host.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + port;
        result = prime * result + ((servers == null) ? 0 : servers.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        DetailViewModel other = (DetailViewModel) obj;
        if (data == null) {
            if (other.data != null)
                return false;
        } else if (!data.equals(other.data))
            return false;
        if (detail == null) {
            if (other.detail != null)
                return false;
        } else if (!detail.equals(other.detail))
            return false;
        if (host == null) {
            if (other.host != null)
                return false;
        } else if (!host.equals(other.host))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (port != other.port)
            return false;
        if (servers == null) {
            if (other.servers != null)
                return false;
        } else if (!servers.equals(other.servers))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

}
