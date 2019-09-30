package com.nomad.server.service.common;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.exception.SystemException;
import com.nomad.message.CommonAnswer;
import com.nomad.message.CommonMessage;
import com.nomad.model.ConnectStatus;
import com.nomad.utility.AbstractConnection;
import com.nomad.utility.NetworkConnectionPool;

public abstract class AbstractCallable<T extends CommonAnswer, K extends CommonMessage> implements Callable<T> {
    private final NetworkConnectionPool<T, K> connectPool;
    private volatile K message;
    private static Logger LOGGER = LoggerFactory.getLogger(AbstractCallable.class);

    public AbstractCallable(final NetworkConnectionPool<T, K> connectPool, final K message) {
        super();
        this.connectPool = connectPool;
        this.message = message;
    }

    @Override
    public T call() {
        AbstractConnection<T, K> connection = null;
        try {
            connection = connectPool.getObject();
            if (connection != null) {
                final T answer = connection.sendMessage(message);
                return answer;
            }
            LOGGER.error(" connect pool is empty !");
            return getErrorMessage(message);
        } catch (final SystemException e) {
            LOGGER.error(" Cannot connect to:" + connectPool.getPoolId() + ":" + e.getMessage());
            connectPool.setConnectStatus(ConnectStatus.INACCESSIBLE);
            return getErrorMessage(message);
        } finally {
            if (connection != null) {
                connection.freeObject();
            }
        }
    }

    public void setMessage(final K message) {
        this.message = message;
    }

    public NetworkConnectionPool<T, K> getConnectPool() {
        return connectPool;
    }

    @Override
    public String toString() {
        return "SessionCallable [connectPool=" + connectPool.getPoolId() + ", message=" + message + "]";
    }

    public abstract T getErrorMessage(K message);
}
