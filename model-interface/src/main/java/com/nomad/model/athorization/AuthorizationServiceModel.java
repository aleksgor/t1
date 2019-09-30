package com.nomad.model.athorization;

import java.util.Map;

public interface AuthorizationServiceModel {

    public String getClazz();

    public void setClazz(String clazz);

    public Map<String, String> getProperties();

}
