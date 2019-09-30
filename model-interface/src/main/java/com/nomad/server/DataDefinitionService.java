package com.nomad.server;

import com.nomad.model.ModelDescription;

public interface DataDefinitionService extends ServiceInterface {

    ModelDescription getModelDescription(String modelName);

    String getName();

    String getFileName();

}
