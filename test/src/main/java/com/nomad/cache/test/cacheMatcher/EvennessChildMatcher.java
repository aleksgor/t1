package com.nomad.cache.test.cacheMatcher;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.cache.test.model.ChildId;
import com.nomad.cache.test.model.MainTestModelId;
import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.server.CacheMatcher;

public class EvennessChildMatcher implements CacheMatcher {
    protected static Logger LOGGER = LoggerFactory.getLogger(EvennessChildMatcher.class);

    private Map<String, String> properties;
    private boolean even;

    @Override
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;

    }

    @Override
    public boolean matchModel(Model m) {
        MainTestModelId id = (MainTestModelId) m.getIdentifier();
        return matchIdentifier(id);
    }

    @Override
    public boolean matchIdentifier(Identifier id) {
        ChildId identifier = (ChildId) id;
        boolean result = ((identifier.getId() % 2) == 0) == even;
        return result;
    }

    @Override
    public void init() {
        String property = properties.get("even");
        if (property == null) {
            LOGGER.error("must be 'enen' peoperty!");
        } else {
            even = Boolean.parseBoolean(property);
        }
    }

}
