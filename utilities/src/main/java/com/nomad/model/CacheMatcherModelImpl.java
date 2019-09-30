package com.nomad.model;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="cacheMatcher")
@XmlAccessorType (XmlAccessType.PROPERTY)
public class CacheMatcherModelImpl implements CacheMatcherModel {

  private String clazz;
  @XmlElement(name="properties")
  private final Map<String, String> properties= new HashMap<String, String>();
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
  public Map<String, String> getProperties() {
    return properties;
  }

  @Override
  public String toString() {
    return "CacheMatcherModel [clazz=" + clazz + ", properties=" + properties + "]";
  }
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
    result = prime * result + ((properties == null) ? 0 : properties.hashCode());
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
    final CacheMatcherModelImpl other = (CacheMatcherModelImpl) obj;
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
    return true;
  }


}
