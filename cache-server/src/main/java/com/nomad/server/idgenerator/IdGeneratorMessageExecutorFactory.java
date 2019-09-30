package com.nomad.server.idgenerator;

import com.nomad.communication.MessageExecutor;
import com.nomad.communication.MessageExecutorFactory;
import com.nomad.communication.NetworkServer;
import com.nomad.model.idgenerator.IdGeneratorMessage;
import com.nomad.model.idgenerator.IdGeneratorService;
import com.nomad.server.ServerContext;

public class IdGeneratorMessageExecutorFactory implements MessageExecutorFactory<IdGeneratorMessage, IdGeneratorMessage> {
    private IdGeneratorService generatorService;

    public IdGeneratorMessageExecutorFactory(IdGeneratorService generatorService) {
        this.generatorService = generatorService;
    }

    @Override
    public MessageExecutor<IdGeneratorMessage, IdGeneratorMessage> getMessageExecutor(final ServerContext context, final int workerId, final NetworkServer server) {
        return new IdGeneratorWorker(context, server, workerId, generatorService);
    }

}
