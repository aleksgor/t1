package com.nomad.server.service.session.synchronizesession;

import com.nomad.cache.commonclientserver.session.SessionAnswerImpl;
import com.nomad.model.session.SessionAnswer;
import com.nomad.model.session.SessionMessage;
import com.nomad.server.service.common.AbstractCallable;
import com.nomad.utility.NetworkConnectionPool;

public class SynchronizationSessionCallable extends AbstractCallable<SessionAnswer, SessionMessage> {


    public SynchronizationSessionCallable(final NetworkConnectionPool<SessionAnswer, SessionMessage> connectPool, final SessionMessage message) {
        super(connectPool, message);
    }

    @Override
    public SessionAnswer getErrorMessage(final SessionMessage message) {
        final SessionAnswer result= new SessionAnswerImpl();
        result.setResultCode(-1);
        return result;
    }

}
