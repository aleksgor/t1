package com.nomad.model;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "dataSource")
@XmlAccessorType (XmlAccessType.FIELD)
public class DataSourceModelImpl implements DataSourceModel {

    private String name;
    @XmlElement( name = "class")
    private String clazz;
    private int threads;
    private int timeout;
    
    private final  Map<String,String> properties = new HashMap<>();

    @Override
    public int getThreads() {
        return threads;
    }

    @Override
    public void setThreads(final int threads) {
        this.threads = threads;
    }

    @Override
    public void addProperty(final String propertyName, final String propertyValue) {
        properties.put(propertyName, propertyValue);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getClazz() {
        return clazz;
    }

    @Override
    public void setClazz(final String clazz) {
        this.clazz = clazz;
    }

    @Override
    public  Map<String,String> getProperties() {
        return properties;
    }

    @Override
    public int getTimeOut() {
        return timeout;
    }

    @Override
    public void setTimeOut(final int timeout) {
        this.timeout = timeout;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
        result = prime * result + ((properties == null) ? 0 : properties.hashCode());
        result = prime * result + threads;
        result = prime * result + timeout;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DataSourceModelImpl other = (DataSourceModelImpl) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (clazz == null) {
            if (other.clazz != null) {
                return false;
            }
        } else if (!clazz.equals(other.clazz)) {
            return false;
        }
        if (properties == null) {
            if (other.properties != null) {
                return false;
            }
        } else if (!properties.equals(other.properties)) {
            return false;
        }
        if (threads != other.threads) {
            return false;
        }
        if (timeout != other.timeout) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DataSourceModel [Name=" + name + ", clazz=" + clazz + ", threads=" + threads + ", timeout=" + timeout + ", properties=" + properties + "]";
    }

}
