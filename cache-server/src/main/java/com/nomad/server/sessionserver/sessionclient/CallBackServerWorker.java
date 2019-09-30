package com.nomad.server.sessionserver.sessionclient;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.cache.commonclientserver.BodyImpl;
import com.nomad.cache.commonclientserver.session.SessionAnswerImpl;
import com.nomad.communication.MessageExecutor;
import com.nomad.core.ProxyCacheManagerProcessing;
import com.nomad.core.ProxyProcessing;
import com.nomad.core.SessionContainerImpl;
import com.nomad.exception.LogicalException;
import com.nomad.exception.SystemException;
import com.nomad.message.Body;
import com.nomad.message.MessageHeader;
import com.nomad.message.MessageSenderReceiver;
import com.nomad.message.MessageSenderReceiverImpl;
import com.nomad.message.OperationStatus;
import com.nomad.message.RawMessage;
import com.nomad.model.ServiceCommand;
import com.nomad.model.session.SessionAnswer;
import com.nomad.model.session.SessionCommand;
import com.nomad.model.session.SessionMessage;
import com.nomad.server.BlockService;
import com.nomad.server.ExecutorServiceProvider;
import com.nomad.server.ServerContext;
import com.nomad.server.SessionResult;
import com.nomad.server.StoreModelService;
import com.nomad.utility.SessionUtil;

public class CallBackServerWorker<T extends SessionMessage, K extends SessionAnswer> implements MessageExecutor<T, K> {
    private static Logger LOGGER = LoggerFactory.getLogger(CallBackServerWorker.class);
    private final ProxyProcessing proxyProcessing;

    private final ProxyCacheManagerProcessing cacheManagerProcessing;
    private final BlockService blockService;
    private final StoreModelService storeModel;

    public CallBackServerWorker(final ServerContext context, final ExecutorServiceProvider executorProvider) throws SystemException {

        proxyProcessing = new ProxyProcessing(context, executorProvider);
        // ----------
        cacheManagerProcessing = new ProxyCacheManagerProcessing(context, executorProvider, null);
        blockService = (BlockService) context.get(ServerContext.ServiceName.BLOCK_SERVICE);
        storeModel = (StoreModelService) context.get(ServerContext.ServiceName.STORE_MODEL_SERVICE);

    }

    private SessionAnswer send(final SessionMessage message, final ServiceCommand command) throws LogicalException, SystemException {
        final MessageHeader header = new MessageHeader();
        final SessionAnswer resultAnswer = new SessionAnswerImpl();
        header.setCommand(command.toString());
        header.setSessionId(message.getSessionId());
        final Body body = new BodyImpl();
        header.getSessions().addAll(message.getSessionIds());
        SessionUtil.fillSessions(header, message);

        final MessageSenderReceiver msr = new MessageSenderReceiverImpl(null);
        final RawMessage result = cacheManagerProcessing.sendBroadcastMessageForEachServer(header, msr.getByteFromBody(body), msr, true);
        if (OperationStatus.OK.equals(result.getResult().getOperationStatus())) {
            resultAnswer.setResultCode(SessionResult.OK.getCode());
        } else {
            throw new LogicalException();
        }
        return resultAnswer;
    }

    private void commitPhase1(final SessionMessage message) throws LogicalException, SystemException {
        LOGGER.debug("commitPh1 server:{} message:{}", storeModel.getServerModel().getServerName(), message);
        send(message, ServiceCommand.COMMIT_PHASE1);

    }

    private void commitPhase2(final SessionMessage message) throws LogicalException, SystemException {
        LOGGER.debug("commitPh2 server:{} message:{}", storeModel.getServerModel().getServerName(), message);
        send(message, ServiceCommand.COMMIT_PHASE2);
        blockService.unblock(new SessionContainerImpl(message));
    }

    private void rollback(final SessionMessage message) throws SystemException, LogicalException {
        LOGGER.debug("rollback server:{} message:{}", storeModel.getServerModel().getServerName(), message);
        send(message, ServiceCommand.ROLLBACK_IN_CACHE);
        blockService.unblock(new SessionContainerImpl(message));

    }

    @Override
    public void stop() {
        proxyProcessing.close();
    }

    @SuppressWarnings("unchecked")
    @Override
    public SessionAnswer execute(final SessionMessage messageIn) {
        final SessionAnswer messageOut = new SessionAnswerImpl();
        final SessionCommand command = messageIn.getSessionCommand();
        // CreateSession(1), KillSession(2), CheckSession(3), 4 register CM
        LOGGER.debug("server:{}, command:{} , params:{}", new Object[] { storeModel.getServerModel().getServerName(), command, messageIn });
        try {
            switch (command.getCode()) {
            case 6:// Rollback
                rollback(messageIn);
                messageOut.setResultCode(SessionResult.OK.getCode());
                break;
            case 7:// Commit ph1:
                commitPhase1(messageIn);
                messageOut.setResultCode(SessionResult.OK.getCode());
                break;
            case 8:// Commit ph2:
                commitPhase2(messageIn);
                messageOut.setResultCode(SessionResult.OK.getCode());
                break;
            case 2:// KillSession:
                final MessageHeader header = new MessageHeader();
                header.setCommand(ServiceCommand.ROLLBACK_IN_CACHE.toString());
                final Body body = new BodyImpl();
                header.setSessionId(messageIn.getSessionId());
                final MessageSenderReceiver msr = new MessageSenderReceiverImpl(null);
                final RawMessage result = cacheManagerProcessing.sendBroadcastMessageForAnyServer(header, msr.getByteFromBody(body), msr, true);
                if (OperationStatus.OK.equals(result.getResult().getOperationStatus())) {
                    messageOut.setResultCode(SessionResult.OK.getCode());
                } else {
                    messageOut.setResultCode(SessionResult.ERROR.getCode());
                }
                break;
            default:
                messageOut.setResultCode(SessionResult.ERROR.getCode());

            }
        } catch (LogicalException e) {
            messageOut.setResultCode(SessionResult.ERROR.getCode());
        } catch (SystemException e) {
            messageOut.setResultCode(SessionResult.ERROR.getCode());
        }
        return messageOut;
    }

}
