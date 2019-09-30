package com.nomad.authorization;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.exception.SystemException;

public class FileAuthorizationServiceImpl implements AuthorizationService {
    protected static final Logger LOGGER = LoggerFactory.getLogger(FileAuthorizationServiceImpl.class);
    private Map<String, UserRoles> data = new ConcurrentHashMap<>();
    private Map<String, String> properties;

    @Override
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public void start() throws SystemException {
        try {
            if (properties != null) {
                String f = properties.get("file");
                if (f != null) {
                    File propFile = new File(f);
                    if (propFile.exists() && propFile.isFile()) {
                        InputStreamReader in = new InputStreamReader(new FileInputStream(propFile));
                        try (BufferedReader br = new BufferedReader(in);) {
                            String stringData;
                            while ((stringData = br.readLine()) != null) {
                                String[] userPasswordRoles = stringData.split(" ");
                                if (userPasswordRoles.length == 1) {
                                    UserRoles roles = new UserRoles();
                                    data.put(userPasswordRoles[0], roles);
                                } else if (userPasswordRoles.length == 2) {
                                    UserRoles roles = new UserRoles();
                                    roles.password = userPasswordRoles[1];
                                    data.put(userPasswordRoles[0], roles);
                                } else if (userPasswordRoles.length >= 3) {
                                    UserRoles role = new UserRoles();
                                    role.password = userPasswordRoles[1];
                                    String[] roleNames = userPasswordRoles[2].split(",");
                                    role.roles = Arrays.asList(roleNames);
                                    data.put(userPasswordRoles[0], role);
                                }
                            }
                        }
                    } else {
                        LOGGER.error(propFile + " does not exist! " + propFile.getAbsolutePath());
                    }
                } else {
                    LOGGER.error("for FileAuthorizationServiceImpl service propery file must be set !");
                }
            }
        } catch (IOException e) {
            throw new SystemException(e);
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean login(String userName, String password) {
        if (userName == null) {
            return false;
        }
        UserRoles roles = data.get(userName);
        if (roles == null) {
            return false;
        }
        if (password != null && password.length() == 0) {
            password = null;
        }
        if (password == null && roles.password == null) {
            return true;
        }
        if (roles.password != null) {
            return roles.password.equals(password);
        }
        return false;
    }

    @Override
    public List<String> getRoles(String userName) {
        if (userName == null) {
            return Collections.emptyList();
        }

        UserRoles roles = data.get(userName);
        if (roles == null) {
            return Collections.emptyList();
        }
        return roles.roles;
    }

    private static class UserRoles {
        private String password;
        private List<String> roles;
    }
}
