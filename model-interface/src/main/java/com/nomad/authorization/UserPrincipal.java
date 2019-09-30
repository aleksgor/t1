package com.nomad.authorization;

import java.util.List;

public interface UserPrincipal {

    String getUser();

    List<String> getRoles();

}
