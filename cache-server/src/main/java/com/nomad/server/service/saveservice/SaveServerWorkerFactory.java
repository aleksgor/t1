package com.nomad.server.service.saveservice;

import com.nomad.communication.MessageExecutor;
import com.nomad.communication.MessageExecutorFactory;
import com.nomad.communication.NetworkServer;
import com.nomad.message.SaveRequest;
import com.nomad.message.SaveResult;
import com.nomad.server.ServerContext;

public class SaveServerWorkerFactory  implements MessageExecutorFactory <SaveRequest, SaveResult >{
    private final SaveServerStore store;
    public SaveServerWorkerFactory(final SaveServerStore store ){
        this.store=store;
    }

    @Override
    public MessageExecutor<SaveRequest, SaveResult> getMessageExecutor(final ServerContext context, final int workerId, final NetworkServer server) {
        return new SaveServiceWorker(store, context);
    }


}
