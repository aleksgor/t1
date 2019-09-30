package com.nomad.server.sessionserver;

import com.nomad.communication.MessageExecutor;
import com.nomad.communication.MessageExecutorFactory;
import com.nomad.communication.NetworkServer;
import com.nomad.model.session.SessionAnswer;
import com.nomad.model.session.SessionMessage;
import com.nomad.server.ServerContext;

public class SessionMessageExecutorFactory implements MessageExecutorFactory<SessionMessage, SessionAnswer> {

    @Override
    public MessageExecutor<SessionMessage, SessionAnswer> getMessageExecutor(final ServerContext context, final int workerId,  final NetworkServer server) {
        return new SessionServerWorker( context, server, workerId);
    }

}
