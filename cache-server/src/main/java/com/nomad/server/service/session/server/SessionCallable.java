package com.nomad.server.service.session.server;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.cache.commonclientserver.session.SessionAnswerImpl;
import com.nomad.cache.commonclientserver.session.SessionMessageImpl;
import com.nomad.client.ClientPooledInterface;
import com.nomad.model.session.SessionAnswer;
import com.nomad.model.session.SessionMessage;
import com.nomad.server.ServerStatus;
import com.nomad.server.service.session.SessionConnectionPool;

public class SessionCallable implements  Callable<SessionAnswer>{
    private final SessionConnectionPool connectPool;
    private volatile SessionMessageImpl message;
    private static Logger LOGGER = LoggerFactory.getLogger(SessionCallable.class);

    public SessionCallable(final SessionConnectionPool connectPool, final SessionMessageImpl message) {
        super();
        this.connectPool = connectPool;
        this.message = message;
    }

    public SessionCallable(final SessionConnectionPool connectPool) {
        this.connectPool = connectPool;
    }

    @Override
    public SessionAnswer call() {
        ClientPooledInterface<SessionMessage, SessionAnswer> connection = null;
        try {
            connection = connectPool.getObject();
            if (connection != null) {
                final SessionAnswer answer = connection.sendMessage(message);
                return answer;
            }
            return new SessionAnswerImpl(-1, null);
        } catch (final Throwable e) {
            LOGGER.error(" Cannot connect to:" + connectPool.getPoolId() + ":" + e.getMessage());
            connectPool.setStatus(ServerStatus.ERROR);
            return new SessionAnswerImpl(-1,message.getSessionId());
        } finally {
            if (connection != null) {
                connection.freeObject();
            }
        }
    }

    public void setMessage(final SessionMessageImpl message) {
        this.message = message;
    }


    public SessionConnectionPool getConnectPool() {
        return connectPool;
    }

    @Override
    public String toString() {
        return "SessionCallable [connectPool=" + connectPool.getPoolId() + ", message=" + message + "]";
    }

}
