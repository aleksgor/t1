package com.nomad.authorization;

import java.util.List;
import java.util.Map;

import com.nomad.server.ServiceInterface;

public interface AuthorizationService extends ServiceInterface {

    boolean login(String userName, String password);

    List<String> getRoles(String userName);

    void setProperties(Map<String, String> properties);

}
