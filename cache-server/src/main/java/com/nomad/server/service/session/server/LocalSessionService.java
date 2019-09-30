package com.nomad.server.service.session.server;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.InternalTransactDataStore;
import com.nomad.authorization.AuthorizationService;
import com.nomad.core.SessionContainerImpl;
import com.nomad.exception.SystemException;
import com.nomad.model.SessionCallBackServerModel;
import com.nomad.model.SessionData;
import com.nomad.model.athorization.AuthorizationServiceModel;
import com.nomad.model.core.SessionContainer;
import com.nomad.model.session.SessionDataImpl;
import com.nomad.model.session.SessionServerModel;
import com.nomad.server.ServerContext;
import com.nomad.server.SessionResult;
import com.nomad.server.SessionService;
import com.nomad.server.SessionState;
import com.nomad.server.service.session.SessionCleanerTimer;
import com.nomad.session.SessionStateImpl;
import com.nomad.utility.SynchronizedLinkedHashMap;

public class LocalSessionService implements SessionService {

    // some sessions to one big object
    protected volatile SynchronizedLinkedHashMap<String, SessionData> sessions = new SynchronizedLinkedHashMap<>();;
    protected static final Logger LOGGER = LoggerFactory.getLogger(LocalSessionService.class);
    private final ServerContext context;
    private InternalTransactDataStore store;
    protected volatile long sessionTimeLive;
    private AuthorizationService authorizationService;
    final SessionServerModel sessionModel;

    private SessionCleanerTimer cleaner;
    private ScheduledFuture<?> future;

    public LocalSessionService(final SessionServerModel serverModel, final ServerContext context) {
        LOGGER.debug("Local session service:" + serverModel);
        this.sessionTimeLive = serverModel.getSessionTimeLive();
        this.context = context;
        this.sessionModel = serverModel;
    }

    @Override
    public boolean removeSession(final String sessionId) {
        final SessionData parent = sessions.get(sessionId);
        if (parent == null) {
            return false;
        }
        final SessionData current = parent.searchSession(sessionId);
        Set<String> allSessions = current.getAllSessions();
        InternalTransactDataStore dataStore = getDataSource();
        // unblock !!!
        for (final String ses : allSessions) {
            sessions.remove(ses);
        }
        parent.remove(sessionId);
        if (dataStore != null) {
            dataStore.rollback(new SessionContainerImpl(current, parent.getSessionId()));
        }
        return true;
    }

    @Override
    public SessionState startNewSession(String sessionId, String userName, String password) {
        LOGGER.debug("Start new session:{},timeout:{},session:{}, user:{}password{}", new Object[] { sessionId, sessionTimeLive, sessions, userName, password });

        List<String> roles = null;
        if (authorizationService != null) {
            if (authorizationService.login(userName, password)) {
                roles = authorizationService.getRoles(userName);
            } else {
                return new SessionStateImpl(SessionResult.ACCESS_DENIED);
            }
        }

        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString();
        }

        final SessionData sessionData = new SessionDataImpl(sessionId);
        sessionData.setUserName(userName);
        if (roles != null) {
            sessionData.getRoles().addAll(roles);
        }
        sessions.put(sessionId, sessionData);
        final SessionState state = new SessionStateImpl(SessionResult.OK);
        state.setUser(userName);
        if (roles != null) {
            state.getRoles().addAll(roles);
        }
        state.setSessionId(sessionId);
        state.setMainSession(sessionId);
        state.getChildrenSessions().add(sessionId);
        state.setResult(SessionResult.OK);
        LOGGER.debug("Start new session result:{},{}", sessionId, state);
        return state;
    }

    @Override
    public SessionState startChildSession(final String parentSessionId, String childSessionId) {
        LOGGER.debug("Start new child session:{},timeout:{},parentSession: {},{}", new Object[] { childSessionId, sessionTimeLive, parentSessionId, sessions });
        if (childSessionId == null) {
            childSessionId = UUID.randomUUID().toString();
        }
        final SessionData parentSession = sessions.get(parentSessionId);
        if (parentSession == null) {
            return new SessionStateImpl(SessionResult.ACCESS_DENIED);
        }

        parentSession.updateDate();

        final SessionData childSession = parentSession.searchSession(parentSessionId);
        if (childSession == null) {
            LOGGER.error("parentSessionId:" + parentSessionId + " not found in:" + parentSession);
            return new SessionStateImpl(SessionResult.ERROR);

        }
        final SessionData sessionData = new SessionDataImpl(childSessionId);
        childSession.getChildSessions().put(childSessionId, sessionData);
        sessions.put(childSessionId, parentSession);

        final SessionState result = new SessionStateImpl(SessionResult.OK);

        result.setMainSession(parentSession.getSessionId());
        result.getChildrenSessions().addAll(sessionData.getAllSessions());
        result.setSessionId(childSessionId);
        LOGGER.debug("Start new child session result:{},{}", childSessionId, result);

        return result;
    }

    @Override
    public SessionState getSessionState(final String sessionId, final String modelName, final String operation) {
        LOGGER.debug("getSessionState sessionId:{},{}", sessionId);

        final SessionData sessionData = sessions.get(sessionId);
        if (sessionData == null) {
            LOGGER.warn("No session :{}", sessionId);
            return new SessionStateImpl(SessionResult.NO_SESSION);
        }
        final long time = sessionData.getLastDate();
        if ((time + sessionTimeLive) < System.currentTimeMillis()) {
            sessionData.getAllSessions();
            for (final String ses : sessionData.getAllSessions()) {
                sessions.remove(ses);
            }
            LOGGER.warn("Session timeout:{} time:{}", sessionId, sessionTimeLive);
            return new SessionStateImpl(SessionResult.TIME_OUT);
        }
        sessionData.updateDate();

        final SessionData childData = sessionData.searchSession(sessionId);
        final SessionState result = new SessionStateImpl(SessionResult.OK);
        result.setMainSession(sessionData.getSessionId());
        result.setSessionId(sessionId);
        result.getChildrenSessions().addAll(childData.getAllSessions());
        result.setUser(sessionData.getUserName());
        result.getRoles().addAll(sessionData.getRoles());

        return result;
    }

    @Override
    public void killOldSessions() throws SystemException {
        InternalTransactDataStore dataStore = getDataSource();

        LOGGER.debug("killOldSessions!");
        for (final String session : sessions.keySet()) {
            final SessionData value = sessions.getQuietly(session);
            if (value != null && (System.currentTimeMillis() - value.getLastDate()) > sessionTimeLive) {
                LOGGER.warn("Local old session killed!:{}", session);
                final SessionData sessionData = sessions.remove(session);
                for (final String sessionId : sessionData.getAllSessions()) {
                    sessions.remove(sessionId);
                }
                if (dataStore != null) {
                    SessionContainer sessions = new SessionContainerImpl(session, session,sessionData.getAllSessions() );
                    dataStore.rollback(sessions);
                }
            } else {
                return;
            }
        }
    }

    @Override
    public boolean serverRegistering(final SessionCallBackServerModel sessionServerModel) {
        return true;
    }

    @Override
    public boolean commit(final String sessionId) {
        LOGGER.debug("local commit {}!", sessionId);

        final SessionData mainSessionData = sessions.get(sessionId);
        if (mainSessionData == null) {
            return false;
        }
        SessionContainer sessions= new SessionContainerImpl(mainSessionData.searchSession(sessionId), mainSessionData.getSessionId()) ;
        final InternalTransactDataStore store = getDataSource();
        if (store != null) {
            try {
                store.commit(sessions);
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
                try {
                    store.rollback(sessions);
                } catch (final Exception e1) {
                    LOGGER.error(e.getMessage(), e);
                }
                return false;
            }
            try {
                store.commitPhase2(sessions);
            } catch (final SystemException e) {
                LOGGER.error(e.getMessage(), e);
                try {
                    store.rollback(sessions);
                } catch (final Exception e1) {
                    LOGGER.error(e.getMessage(), e);
                }
                return false;
            }
        }
        return true;
    }

    @Override
    public void rollback(final String sessionId) throws SystemException {

        final SessionData mainSessionData = sessions.get(sessionId);
        final SessionData sessionData = mainSessionData.searchSession(sessionId);

        LOGGER.debug("Rollback:{}", sessionId);
        final InternalTransactDataStore store = getDataSource();
        if (store != null) {
            try {
                SessionContainer sessions= new SessionContainerImpl(sessionId,mainSessionData.getSessionId(),sessionData.getAllSessions());
                store.rollback(sessions);
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private InternalTransactDataStore getDataSource() {
        if (store == null) {
            store = (InternalTransactDataStore) context.get(ServerContext.ServiceName.INTERNAL_TRANSACT_DATA_STORE);
        }
        return store;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void start() throws SystemException {
        AuthorizationServiceModel authorizationServiceModel = sessionModel.getAuthorizationService();
        if (authorizationServiceModel != null && authorizationServiceModel.getClazz() != null) {
            try {
                Class<AuthorizationService> asClass = (Class<AuthorizationService>) Class.forName(authorizationServiceModel.getClazz());
                authorizationService = asClass.newInstance();
                authorizationService.setProperties(authorizationServiceModel.getProperties());
                authorizationService.start();
            } catch (Throwable e) {
                throw new SystemException(e);
            }
        }
        LOGGER.debug("start SessionCleanerTimer:" + sessionModel.getSessionTimeLive() + " ms");
        cleaner = new SessionCleanerTimer(this, "sessionCleaner: " + context.getServerName());

        future = context.getScheduledExecutorService().scheduleAtFixedRate(cleaner, sessionModel.getSessionTimeLive(), TimeUnit.MILLISECONDS);

    }

    @Override
    public void stop() {
        if (authorizationService != null) {
            authorizationService.stop();
        }
        if (cleaner != null) {
            cleaner.cancel();
        }
        sessions.clear();
        if (store != null) {
            store.stop();
        }
        if (future != null) {
            context.getScheduledExecutorService().stop(future);

        }

    }

    @Override
    public Map<String, SessionData> getAllSessions() {

        return sessions.getAllData();
    }

    @Override
    public boolean isTrustService() {
        return false;
    }

}
