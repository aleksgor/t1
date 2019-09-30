package com.nomad.server.service.idgenerator;

import com.nomad.model.ServerModel;
import com.nomad.model.idgenerator.IdGeneratorService;
import com.nomad.server.ServerContext;

public class IdGeneratorServiceFactory {

    public static IdGeneratorService getIdGeneratorService(ServerModel model, ServerContext context) {
        if (model == null) {
            return null;
        }
        if (model.getIdGeneratorServerModel() != null) {
            if (model.getIdGeneratorServerModel().getPort() <= 0) {
                return new IdGeneratorServiceImpl(context, model.getIdGeneratorServerModel());
            }
        }
        if (model.getIdGeneratorClientModels().size() > 0) {
            return new NetIdGeneratorService(model.getIdGeneratorClientModels(), context);
        }
        return null;
    }
}
