package com.nomad.model.session;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;

import com.nomad.model.athorization.AuthorizationServiceModel;

public class AuthorizationServiceModelImpl implements AuthorizationServiceModel {

    private String clazz;

    @XmlElement(name = "properties")
    private final Map<String, String> properties = new HashMap<String, String>();

    @Override
    public String getClazz() {
        return clazz;
    }

    @Override
    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        return "AuthorizationServiceModelImpl [clazz=" + clazz + ", properties=" + properties + "]";
    }

}
