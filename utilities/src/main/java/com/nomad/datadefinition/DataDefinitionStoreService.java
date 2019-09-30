package com.nomad.datadefinition;

import java.util.HashMap;
import java.util.Map;

import com.nomad.exception.SystemException;
import com.nomad.server.DataDefinitionService;
import com.nomad.server.ServiceInterface;

public class DataDefinitionStoreService implements ServiceInterface {

    private final Map<String, DataDefinitionService> dataDefinitionServices = new HashMap<String, DataDefinitionService>();

    public DataDefinitionService getDataDefinitionService(String name) {
        return dataDefinitionServices.get(name);
    }

    public void putDataDefinitionService(String name, DataDefinitionService dataDefinitionService) {
        dataDefinitionServices.put(name, dataDefinitionService);
    }

    @Override
    public void start() throws SystemException {
        for (DataDefinitionService data : dataDefinitionServices.values()) {
            data.start();
        }
    }

    @Override
    public void stop() {
        for (DataDefinitionService data : dataDefinitionServices.values()) {
            data.stop();
        }

    }

}
