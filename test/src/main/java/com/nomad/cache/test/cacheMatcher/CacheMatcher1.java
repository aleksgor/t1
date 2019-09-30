package com.nomad.cache.test.cacheMatcher;

import java.util.Map;

import com.nomad.cache.test.model.MainTestModelId;
import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.server.CacheMatcher;

public class CacheMatcher1 implements CacheMatcher {

    private Map<String, String> properties;
    private int remainder;
    private int total;

    @Override
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public boolean matchModel(Model m) {

        MainTestModelId id = (MainTestModelId) m.getIdentifier();
        return id.getId() % total == remainder;
    }

    @Override
    public boolean matchIdentifier(Identifier id) {
        MainTestModelId identifier = (MainTestModelId) id;
        return identifier.getId() % total == remainder;
    }

    @Override
    public void init() {
        String property = properties.get("remainder");
        if (property != null && property.length() > 0) {
            remainder = Integer.parseInt(property);
        }
        property = properties.get("total");
        if (property != null && property.length() > 0) {
            total = Integer.parseInt(property);
        }
    }

}
