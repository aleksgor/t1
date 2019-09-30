package com.nomad.server.idgenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.cache.commonclientserver.idgenerator.IdGeneratorMessageImpl;
import com.nomad.communication.MessageExecutor;
import com.nomad.communication.NetworkServer;
import com.nomad.model.idgenerator.IdGeneratorCommand;
import com.nomad.model.idgenerator.IdGeneratorMessage;
import com.nomad.model.idgenerator.IdGeneratorService;
import com.nomad.server.ServerContext;

public class IdGeneratorWorker implements MessageExecutor<IdGeneratorMessage, IdGeneratorMessage> {
    private static Logger LOGGER = LoggerFactory.getLogger(IdGeneratorWorker.class);

    private final IdGeneratorService generatorService;

    public IdGeneratorWorker(final ServerContext context, final NetworkServer server, final int threadId, IdGeneratorService generatorService) {
        LOGGER.debug("New IdGenerator thread. IdGenerator Service:{}");
        this.generatorService = generatorService;

    }


    @Override
    public IdGeneratorMessage execute(final IdGeneratorMessage messageIn) {
        IdGeneratorMessage messageOut = new IdGeneratorMessageImpl();
        messageOut.setCommand(messageIn.getCommand());
        messageOut.setModelName(messageIn.getModelName());

        final IdGeneratorCommand command = messageIn.getCommand();
        LOGGER.debug("idGenerator server command:{} data:{}", command, messageIn);
        // GET_NEXT_ID(1), GET_NEXT_IDENTIFIER(2);
        switch (command.getCode()) {
        case 1:// GET_NEXT_ID
            messageOut.getValue().addAll(generatorService.nextId(messageIn.getModelName(), messageIn.getCount()));
            messageOut.setResultCode(0);
            break;
        case 3:// GET_STATUS:
            messageOut.setResultCode(0);
            break;
        }
        LOGGER.debug("Answer:{}", messageOut);
        return messageOut;
    }

    @Override
    public void stop() {
        if (generatorService != null) {
            generatorService.stop();
        }
    }
}