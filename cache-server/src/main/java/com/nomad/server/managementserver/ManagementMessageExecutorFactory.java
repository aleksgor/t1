package com.nomad.server.managementserver;

import com.nomad.communication.MessageExecutor;
import com.nomad.communication.MessageExecutorFactory;
import com.nomad.communication.NetworkServer;
import com.nomad.message.ManagementMessage;
import com.nomad.model.CommonServerModel;
import com.nomad.model.management.ManagementServerModel;
import com.nomad.server.ServerContext;

public class ManagementMessageExecutorFactory implements MessageExecutorFactory<ManagementMessage, ManagementMessage> {

    CommonServerModel serverModel;
    @Override
    public MessageExecutor<ManagementMessage, ManagementMessage> getMessageExecutor(final ServerContext context, final int workerId,  final NetworkServer server) {
        return new ManagementServerWorker( context, server, workerId, (ManagementServerModel) serverModel);
    }

}
