package com.nomad.server;

import java.util.Map;

import com.nomad.model.Identifier;
import com.nomad.model.Model;

public interface CacheMatcher {

    void setProperties(Map<String, String> properties);

    boolean matchModel(Model m);

    boolean matchIdentifier(Identifier id);

    void init();

}
