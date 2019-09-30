package com.nomad.server.sessionserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.cache.commonclientserver.session.SessionAnswerImpl;
import com.nomad.communication.MessageExecutor;
import com.nomad.communication.NetworkServer;
import com.nomad.exception.SystemException;
import com.nomad.model.SessionData;
import com.nomad.model.session.SessionAnswer;
import com.nomad.model.session.SessionClientModel;
import com.nomad.model.session.SessionClientModelImpl;
import com.nomad.model.session.SessionCommand;
import com.nomad.model.session.SessionDataImpl;
import com.nomad.model.session.SessionMessage;
import com.nomad.server.ServerContext;
import com.nomad.server.ServerContext.ServiceName;
import com.nomad.server.SessionResult;
import com.nomad.server.SessionState;
import com.nomad.server.service.session.server.ServerLocalSessionService;
import com.nomad.server.service.session.server.SessionStateData;

public class SessionServerWorker implements MessageExecutor<SessionMessage, SessionAnswer> {
    private static Logger LOGGER = LoggerFactory.getLogger(SessionServerWorker.class);

    private volatile ServerLocalSessionService sessionService;
    private final SessionServerCallBackClient sessionServerCallBackClient;

    public SessionServerWorker(final ServerContext context, final NetworkServer server, final int threadId) {
        LOGGER.debug("New session thread. Session Service:{}", sessionService);
        sessionService = (ServerLocalSessionService) context.get(ServiceName.SESSION_SERVICE);
        sessionServerCallBackClient = (SessionServerCallBackClient) context.get(ServiceName.SESSION_CALLBACK_CLIENT);

    }

    private SessionAnswer getSessionAnswer(SessionState state) {
        final SessionAnswer messageOut = new SessionAnswerImpl();
        messageOut.setParentSessionId(state.getMainSession());
        messageOut.setResultCode(state.getResult().getCode());
        messageOut.setSessionId(state.getSessionId());
        messageOut.setUserName(state.getUser());
        messageOut.getRoles().addAll(state.getRoles());
        messageOut.getChildSessions().addAll(state.getChildrenSessions());

        return messageOut;
    }
    @Override
    public SessionAnswer execute (final SessionMessage messageIn) throws SystemException {
        SessionAnswer messageOut = new SessionAnswerImpl();

        final SessionCommand command = messageIn.getSessionCommand();
        LOGGER.debug("session server command:{} data:{}", command, messageIn);
        // CreateSession(1), KillSession(2), CheckSession(3), 4 register CM
        switch (command.getCode()) {
        case 1:// CreateSession:

            final SessionState state = sessionService.startNewSession(messageIn.getSessionId(), messageIn.getUserName(), messageIn.getPassword());
            if (SessionResult.OK.equals(state.getResult())) {
                messageOut=getSessionAnswer(state);
                messageOut.setResultCode(SessionResult.OK.getCode());

            } else {
                messageOut=getSessionAnswer(state);
                messageOut.setResultCode(state.getResult().getCode());
            }
            break;
        case 2:// KillSession:
            sessionService.removeSession(messageIn.getSessionId());
            messageOut.setResultCode(SessionResult.OK.getCode());
            break;
        case 3: // session State
            messageOut = getSessionAnswer(sessionService.getSessionState(messageIn.getSessionId(), messageIn.getModelName(), messageIn.getOperation()));
            break;
        case 4:// register CM
            final String data = messageIn.getOperation(); // should be
            // count:host:port:timeout
            final String[] rows = data.split(":");
            if (rows.length == 4) {

                final SessionClientModel sessionClient = new SessionClientModelImpl();
                sessionClient.setThreads(Integer.parseInt(rows[0]));
                sessionClient.setHost(rows[1]);
                sessionClient.setPort(Integer.parseInt(rows[2]));
                sessionClient.setTimeout(Integer.parseInt(rows[3]));

                sessionServerCallBackClient.addCacheManager(sessionClient);
                messageOut.setResultCode(SessionResult.OK.getCode());
                break;
            } else {
                LOGGER.error("Incorrect  data:" + data);
            }
            messageOut.setResultCode(SessionResult.ERROR.getCode());
            break;
        case 6:// rollback
            sessionService.rollback(messageIn.getSessionId());
            messageOut.setResultCode(SessionResult.OK.getCode());
            break;
        case 12:// get status
            messageOut.setResultCode(SessionResult.OK.getCode());
            break;
        case 14:// start Child Session
            final SessionState sessionState = sessionService.startChildSession(messageIn.getMainSession(), messageIn.getSessionId());
            messageOut = getSessionAnswer(sessionState);
            break;
        case 15:// commit

            if (sessionService.commit(messageIn.getSessionId())) {
                messageOut.setResultCode(SessionResult.OK.getCode());
            } else {
                messageOut.setResultCode(SessionResult.ERROR.getCode());
            }
            break;
            // SYNC
        case 20:// SYNC_REMOVE_SESSION
            sessionService.removeSessionSynchronization(messageIn.getSessionId());
            break;
        case 21:// SYNC_STRAT_NEW_SESSION
            sessionService.startNewSessionSynchronization(messageIn.getSessionId(), messageIn.getUserName(), messageIn.getPassword());
            break;
        case 22:// SYNC_STRAT_NEW_CHILD_SESSION
            sessionService.startChildSessionSync(messageIn.getMainSession(), messageIn.getSessionId());
            break;
        case 23:// SYNC_GET_SESSION_STATE
            final SessionStateData sessionStateData = sessionService.getSessionStateSync(messageIn.getSessionId(), messageIn.getModelName(), messageIn.getOperation());
            if (sessionStateData.getSessionData() != null) {
                messageOut.setSyncData(sessionStateData.getSessionData());
            }
            if (sessionStateData.getSessionState() != null) {
                messageOut.setParentSessionId(sessionStateData.getSessionState().getMainSession());
                messageOut.setResultCode(sessionStateData.getSessionState().getResult().getCode());
                messageOut.setSessionId(sessionStateData.getSessionState().getSessionId());
                messageOut.getChildSessions().addAll(sessionStateData.getSessionState().getChildrenSessions());
                messageOut.setUserName(sessionStateData.getSessionState().getUser());
                messageOut.getRoles().addAll(sessionStateData.getSessionState().getRoles());
            }
            break;
        case 24:// TEST
            messageOut.setResultCode(0);
            break;
        case 25:// SYNC_GET_ALL_SESSIONS
            messageOut.setResultCode(0);
            SessionData syncData = new SessionDataImpl("");
            syncData.getChildSessions().putAll(sessionService.getAllSessions());
            messageOut.setSyncData(syncData);
            break;
        }
        LOGGER.debug("Answer:{}", messageOut);
        return messageOut;
    }

    @Override
    public void stop() {
        if (sessionService != null) {
            sessionService.stop();
        }
        if (sessionServerCallBackClient != null) {
            sessionServerCallBackClient.stop();
        }
    }
}