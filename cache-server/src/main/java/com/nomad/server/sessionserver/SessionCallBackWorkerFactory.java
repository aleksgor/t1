package com.nomad.server.sessionserver;

import com.nomad.communication.MessageExecutor;
import com.nomad.communication.MessageExecutorFactory;
import com.nomad.communication.NetworkServer;
import com.nomad.exception.SystemException;
import com.nomad.model.session.SessionAnswer;
import com.nomad.model.session.SessionMessage;
import com.nomad.server.ExecutorServiceProvider;
import com.nomad.server.ServerContext;
import com.nomad.server.sessionserver.sessionclient.CallBackServerWorker;


public class SessionCallBackWorkerFactory<T extends SessionMessage, K extends SessionAnswer> implements MessageExecutorFactory <T, K >{

    final ExecutorServiceProvider executorProvider;
    public SessionCallBackWorkerFactory(final ExecutorServiceProvider executorProvider){
        this.executorProvider=executorProvider;

    }
    @Override
    public MessageExecutor<T, K> getMessageExecutor(final ServerContext context, final int workerId, final NetworkServer server) throws SystemException{
        return new CallBackServerWorker<T, K>(context , executorProvider);
    }


}
