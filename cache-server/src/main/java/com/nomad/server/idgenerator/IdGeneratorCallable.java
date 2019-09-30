package com.nomad.server.idgenerator;

import java.util.concurrent.Callable;

import com.nomad.cache.commonclientserver.idgenerator.IdGeneratorMessageImpl;
import com.nomad.model.idgenerator.IdGeneratorMessage;
import com.nomad.server.MessageCallable;
import com.nomad.server.service.idgenerator.IdGeneratorConnectionPool;

public class IdGeneratorCallable extends MessageCallable<IdGeneratorMessage, IdGeneratorMessage> implements Callable<IdGeneratorMessage> {

    public IdGeneratorCallable(final IdGeneratorConnectionPool connectPool, final IdGeneratorMessage message) {
        super(connectPool, message);
    }

    public IdGeneratorCallable(final IdGeneratorConnectionPool connectPool) {
        super(connectPool);

    }

    @Override
    public String toString() {
        return "SessionCallable [connectPool=" + connectPool.getPoolId() + ", message=" + message + "]";
    }

    @Override
    protected IdGeneratorMessage getErrorAnswer(IdGeneratorMessage inputMessage) {
        IdGeneratorMessage result = new IdGeneratorMessageImpl();
        result.setResultCode(-1);
        return result;
    }

}
