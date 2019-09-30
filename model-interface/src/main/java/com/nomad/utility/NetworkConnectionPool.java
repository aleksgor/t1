package com.nomad.utility;

import com.nomad.message.CommonAnswer;
import com.nomad.message.CommonMessage;
import com.nomad.model.ConnectStatus;

public interface NetworkConnectionPool<T extends CommonAnswer, K extends CommonMessage> extends ObjectPool<AbstractConnection<T, K>> {

    ConnectStatus getConnectStatus();

    void setConnectStatus(ConnectStatus status);

    ConnectStatus getStatus();

    void setStatus(final ConnectStatus status);

}