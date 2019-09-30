package com.nomad.datadefinition;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.exception.SystemException;
import com.nomad.model.ModelDescription;
import com.nomad.server.DataDefinitionService;
import com.nomad.server.ServiceInterface;

public class DataDefinitionServiceImpl implements DataDefinitionService, ServiceInterface {
    protected static Logger LOGGER = LoggerFactory.getLogger(DataDefinitionService.class);
    private final String name;
    private String fileName = "pm.cfg.xml";
    private final Map<String, ModelDescription> models = new HashMap<>();

    public DataDefinitionServiceImpl(String name, String fileName, String empty) {
        if (fileName != null) {
            this.fileName = fileName;
        }
        this.name = name == null ? "" : name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public ModelDescription getModelDescription(final String modelName) {
        return models.get(modelName);
    }

    @Override
    public void start() throws SystemException {
        final DataDefinitionFileLoader loader = new DataDefinitionFileLoader();

        Map<String, ModelDescription> models = loader.loadFile(fileName);
        this.models.putAll(models);
    }

    @Override
    public void stop() {
        models.clear();
    }

    @Override
    public String toString() {
        return "DataDefinitionServiceImpl [name=" + name + ", fileName=" + fileName + ", models=" + models + "]";
    }

}
