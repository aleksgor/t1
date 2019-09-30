package com.nomad.server;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.client.ClientPooledInterface;
import com.nomad.message.CommonAnswer;
import com.nomad.message.CommonMessage;
import com.nomad.model.ConnectStatus;
import com.nomad.utility.NetworkConnectionPool;

public abstract class MessageCallable<T extends CommonMessage, K extends CommonAnswer> implements Callable<K> {
    protected final NetworkConnectionPool<K, T> connectPool;
    protected volatile T message;
    private static Logger LOGGER = LoggerFactory.getLogger(MessageCallable.class);

    public MessageCallable(final NetworkConnectionPool<K, T> connectPool, final T message) {
        super();
        this.connectPool = connectPool;
        this.message = message;
    }

    public MessageCallable(final NetworkConnectionPool<K, T> connectPool) {
        this.connectPool = connectPool;
    }

    @SuppressWarnings("unchecked")
    @Override
    public K call() throws Exception {
        ClientPooledInterface<T, K> connection = null;

        try {
            connection = (ClientPooledInterface<T, K>) connectPool.getObject();
            if (connection != null) {
                final K answer = connection.sendMessage(message);
                return answer;
            }
            return getErrorAnswer(message);
        } catch (final Throwable e) {
            LOGGER.error(" Cannot connect to:" + connectPool.getPoolId() + ":" + e.getMessage());
            connectPool.setStatus(ConnectStatus.ERROR);
            return getErrorAnswer(message);
        } finally {
            if (connection != null) {
                connection.freeObject();
            }
        }
    }

    protected abstract K getErrorAnswer(T inputMessage);

    public void setMessage(final T message) {
        this.message = message;
    }


    public NetworkConnectionPool<K, T> getConnectPool() {
        return connectPool;
    }

    @Override
    public String toString() {
        return "SessionCallable [connectPool=" + connectPool.getPoolId() + ", message=" + message + "]";
    }

}
