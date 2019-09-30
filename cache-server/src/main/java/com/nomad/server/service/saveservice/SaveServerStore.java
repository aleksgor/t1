package com.nomad.server.service.saveservice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.message.SaveRequest;
import com.nomad.message.SaveResult;
import com.nomad.model.Identifier;
import com.nomad.server.SaveService;
import com.nomad.server.service.saveservice.model.SaveResultImpl;
import com.nomad.utility.SynchronizedLinkedHashMap;

public class SaveServerStore {

    private static byte NEW = 1;
    private static byte FIXED = 2;

    // session-> identifier-> time
    private volatile Map<String, SaveData> sessions = new ConcurrentHashMap<>();
    private volatile SynchronizedLinkedHashMap<Identifier, SaveData> ids = new SynchronizedLinkedHashMap<Identifier, SaveData>();
    private static Logger LOGGER = LoggerFactory.getLogger(SaveService.class);
    private volatile SaveService client;
    private final long timeLive;

    public SaveServerStore(final SaveService client, final long timeLive) {
        this.client = client;
        this.timeLive = timeLive;

    }

    public void sessionClose(final String sessionId) {
        final SaveData data = sessions.remove(sessionId);
        if (data != null) {
            for (final Identifier id : data.getIdentifiers()) {
                synchronized (ids) {
                    ids.remove(id);
                }
            }
        }
    }

    // return only free ids
    public SaveResult check(final SaveRequest request) {
        LOGGER.debug("check  request: {} ", request);
        if (request.getSessionIds().size() != 1) {
            LOGGER.error("check  request: {} must contains only one session!", request);
            return new SaveResultImpl(request.getIdentifiers());
        }
        final String sessionId = request.getSessionIds().iterator().next();
        final List<Identifier> newIds = new ArrayList<>(request.getIdentifiers().size());
        final SaveData sd = new SaveData(newIds, sessionId, request.getClientId());
        sd.setStatus(NEW);
        synchronized (ids) {
            for (final Identifier id : request.getIdentifiers()) {
                if (!ids.containsKey(id)) {
                    sd.getIdentifiers().add(id);
                    ids.put(id, sd);
                    newIds.add(id);
                }
            }
        }
        Collection<Identifier> idsForLock = null;
        if (client.isAvailable()) {
            try {
                idsForLock = client.internalCheck(newIds, sessionId, request.getClientId());
            } catch (final Exception e) {
                synchronized (ids) {
                    for (final Identifier identifier : request.getIdentifiers()) {
                        ids.remove(identifier);
                    }
                }
                final SaveResultImpl result = new SaveResultImpl(new ArrayList<Identifier>());
                result.setResultCode(0);
                LOGGER.error("Error in " + client + ": " + e.getMessage(), e);
            }
        } else {
            idsForLock = newIds;
        }
        sd.setStatus(FIXED);

        synchronized (sessions) {
            final SaveData sd1 = sessions.get(sessionId);
            if (sd1 == null) {
                sessions.put(sessionId, sd);
            } else {
                sd1.getIdentifiers().addAll(sd.getIdentifiers());
                sd1.updateDate();
            }
        }
        final SaveResultImpl result = new SaveResultImpl(idsForLock);
        result.setResultCode(0);
        LOGGER.debug("check result: allowed {}", result);
        return result;
    }

    public void stop() {
    }

    public void cleanSession(final Collection<String> sessionIds) {
        LOGGER.debug("cleanSession:{}", sessionIds);
        for (final String sessionId : sessionIds) {
            removeSession(sessionId);
            sessions.remove(sessionId);
        }
        LOGGER.debug("cleanSession result ss:{}", sessions);
    }

    private void removeSession(final String sessionId) {
        final SaveData sd = sessions.get(sessionId);
        if (sd != null) {
            synchronized (ids) {
                for (final Identifier identifier : sd.getIdentifiers()) {
                    ids.remove(identifier);
                }
            }
        }
    }

    public void cleanOldSessions(final long timeout) {
        final long minTime = System.currentTimeMillis() - timeLive;
        synchronized (ids) {
            Set<Identifier> idskeys = ids.keySet();
            for (final Identifier id : idskeys) {
                final SaveData sd = ids.get(id);
                if (sd.getTime() < minTime) {
                    ids.remove(id);
                } else {
                    return;
                }
            }
        }
    }

    public SaveResult preCheck(final SaveRequest message) {
        String sessionId;
        if (message.getSessionIds().size() != 1) {
            final SaveResult result = new SaveResultImpl(new ArrayList<Identifier>(0));
            result.setResultCode(-2);
            return result;
        }
        sessionId = message.getSessionIds().iterator().next();

        final SaveData sdNew = new SaveData(new ArrayList<Identifier>(message.getIdentifiers().size()), sessionId, message.getClientId());
        final Collection<Identifier> result = new ArrayList<Identifier>();
        synchronized (ids) {
            for (final Identifier id : message.getIdentifiers()) {
                final SaveData sd = ids.get(id);
                if (sd != null) {
                    if (sd.getStatus() == NEW) {
                        if (!SaveCoordinator.compare(sd.getClientId(), message.getClientId())) {
                            result.add(id);
                        }
                    }
                } else {
                    sdNew.getIdentifiers().add(id);
                    ids.put(id, sdNew);
                }
            }
        }
        final SaveData sdSes = sessions.get(sessionId);
        if (sdSes == null) {
            sessions.put(sessionId, sdNew);
        } else {
            sdSes.getIdentifiers().addAll(sdNew.getIdentifiers());
        }

        final SaveResult resultSd = new SaveResultImpl(result);
        resultSd.setResultCode(0);
        return resultSd;
    }

    private static class SaveData {
        private final Set<Identifier> identifiers = new HashSet<>();
        private final String sessionId;
        private long time;
        private byte status;
        private final long clientId;

        private SaveData(final Collection<Identifier> identifiers, final String sessionId, final long clientId) {
            super();
            this.identifiers.addAll(identifiers);
            this.sessionId = sessionId;
            time = System.currentTimeMillis();
            this.clientId = clientId;
        }

        public Collection<Identifier> getIdentifiers() {
            return identifiers;
        }

        public long getTime() {
            return time;
        }

        public long getClientId() {
            return clientId;
        }

        public void setStatus(final byte status) {
            this.status = status;
        }

        public byte getStatus() {
            return status;
        }

        public void updateDate() {
            time = System.currentTimeMillis();
        }

        @Override
        public String toString() {
            return "SaveData [identifiers=" + identifiers + ", sessionId=" + sessionId + ", time=" + time + ", status=" + status + ", clientId=" + clientId + "]";
        }

    }

}
