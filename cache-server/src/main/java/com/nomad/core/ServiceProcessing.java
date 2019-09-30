package com.nomad.core;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import com.nomad.cache.commonclientserver.RawMessageImpl;
import com.nomad.cache.commonclientserver.ResultImpl;
import com.nomad.exception.EOMException;
import com.nomad.exception.SystemException;
import com.nomad.message.MessageHeader;
import com.nomad.message.MessageSenderReceiver;
import com.nomad.message.OperationStatus;
import com.nomad.message.RawMessage;
import com.nomad.server.ExecutorServiceProvider;
import com.nomad.server.ServerContext;
import com.nomad.server.service.childserver.MessageAnswer;
import com.nomad.server.service.childserver.MessageRequest;
import com.nomad.server.service.childserver.StoreConnectionPool;
import com.nomad.utility.MessageUtil;

public class ServiceProcessing extends CommonProxyProcessing {

    public ServiceProcessing(final ServerContext context, final ExecutorServiceProvider executorServiceProvider) throws SystemException{
        super(context, executorServiceProvider);
    }

    RawMessage sendBroadcastMessage(final MessageHeader header, final InputStream input, final MessageSenderReceiver msr, final boolean local) throws SystemException, EOMException {
        final byte[] message =  MessageUtil.readByteBody(input);
        return sendBroadcastMessageForEachServer(header, message, msr, local);
    }

    RawMessage askСhildrenSequentially(final MessageHeader header, final byte[] message, final MessageSenderReceiver msr, final boolean local) {

        final Collection<StoreConnectionPool> servers = childrenServerService.getCacheConnectionsPools(null);
        if (servers == null) {
            LOGGER.error("server:{} no child servers !", server.getServerModel().getServerName(), header);
            return new RawMessageImpl(header, message, new ResultImpl(OperationStatus.EMPTY_ANSWER));

        }

        new ArrayList<>(servers.size());

        MessageAnswer answer = null;
        for (final StoreConnectionPool srv : servers) {
            if (!srv.isLocal() || local) { // not local
                LOGGER.debug("sendBradcastMessageForEachServer: client:{}, server:{}", server.getServerModel().getServerName(),  "local" );
                final MessageRequest request = new MessageRequest(new RawMessageImpl(header, message), srv);
                try {
                    MessageAnswer thisAnswer = null;
                    thisAnswer = request.call();
                    if (answer == null) {
                        answer = thisAnswer;
                    }
                    if (OperationStatus.OK.equals(thisAnswer.getMessage().getResult().getOperationStatus())) {

                        return thisAnswer.getMessage();
                    }
                } catch (final Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }

        }
        if (answer != null) {
            return answer.getMessage();
        }
        return new RawMessageImpl(header, message, new ResultImpl(OperationStatus.EMPTY_ANSWER));

    }
}
