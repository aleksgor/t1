package com.nomad.model;

import java.util.Properties;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name="commandPluginModel")
public class CommandPluginModelImpl implements CommandPluginModel {

  private String clazz;
  private int poolSize;
  private final Properties properties= new Properties();
  private long checkDelay;
  private int timeout;


  @Override
  public long getCheckDelay() {
    return checkDelay;
  }

  @Override
  public void setCheckDelay(final long checkDelay) {
    this.checkDelay = checkDelay;
  }

  @Override
  public String getClazz() {
    return clazz;
  }

  @Override
  @XmlElement(name="class")
  public void setClazz(final String clazz) {
    this.clazz = clazz;
  }

  @Override
  public int getPoolSize() {
    return poolSize;
  }

  @Override
  public void setPoolSize(final int poolSize) {
    this.poolSize = poolSize;
  }

  @Override
  public Properties getProperties() {
    return properties;
  }

  @Override
  public int getTimeout() {
    return timeout;
  }

  @Override
  public void setTimeout(final int timeout) {
    this.timeout = timeout;
  }

  @Override
  public String toString() {
    return "ServerPlugin [clazz=" + clazz + ", poolSize=" + poolSize + ", properties=" + properties + ", checkDelay=" + checkDelay + ", timeout=" + timeout
        + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (checkDelay ^ (checkDelay >>> 32));
    result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
    result = prime * result + poolSize;
    result = prime * result + ((properties == null) ? 0 : properties.hashCode());
    result = prime * result + (timeout ^ (timeout >>> 32));
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
    final CommandPluginModelImpl other = (CommandPluginModelImpl) obj;
    if (checkDelay != other.checkDelay) {
      return false;
    }
    if (clazz == null) {
      if (other.clazz != null) {
        return false;
      }
    } else if (!clazz.equals(other.clazz)) {
      return false;
    }
    if (poolSize != other.poolSize) {
      return false;
    }
    if (properties == null) {
      if (other.properties != null) {
        return false;
      }
    } else if (!properties.equals(other.properties)) {
      return false;
    }
    if (timeout != other.timeout) {
      return false;
    }
    return true;
  }

}
