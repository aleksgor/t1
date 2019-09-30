package com.nomad.authorization;

import java.util.ArrayList;
import java.util.List;

public class UserPrincipalImpl implements UserPrincipal {

    private String user;
    private final List<String> roles = new ArrayList<String>();

    public UserPrincipalImpl(String user) {
        this.user = user;
    }
    @Override
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public List<String> getRoles() {
        return roles;
    }


}
