package com.nomad.communication;

import com.nomad.exception.SystemException;
import com.nomad.message.CommonAnswer;
import com.nomad.message.CommonMessage;
import com.nomad.server.ServerContext;

public interface MessageExecutorFactory <K extends CommonMessage, T extends CommonAnswer> {

    MessageExecutor<K , T >  getMessageExecutor(final ServerContext context, int workerId, NetworkServer server) throws SystemException;

}
