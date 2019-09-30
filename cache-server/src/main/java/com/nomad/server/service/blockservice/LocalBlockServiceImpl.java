package com.nomad.server.service.blockservice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.exception.SystemException;
import com.nomad.model.Identifier;
import com.nomad.model.ServerModel;
import com.nomad.model.core.SessionContainer;
import com.nomad.server.BlockService;
import com.nomad.server.ServerContext;
import com.nomad.server.statistic.StatisticBlockMBean;

public class LocalBlockServiceImpl implements BlockService {

    private volatile Map<Identifier, Sessions> blocks = new ConcurrentHashMap<>();
    // mainSession-> session->id
    private volatile Map<String, Map<String, Set<Identifier>>> sessions = new ConcurrentHashMap<>();
    private static Logger LOGGER = LoggerFactory.getLogger(BlockService.class);
    private final StatisticBlockMBean statisticBean;
    private final ServerModel serverModel;

    /**
     * Return no blocked Ids.
     */
    public LocalBlockServiceImpl(final ServerModel serverModel, ServerContext context) {
        LOGGER.info("start block service!");
        this.serverModel = serverModel;
        statisticBean = new StatisticBlockMBean();
        statisticBean.setServiceInterface(this);
        context.getInformationPublisherService().publicData(statisticBean, serverModel.getServerName(), "BlockService", null);
        statisticBean.setBlockCount(blocks.size());
        statisticBean.setSessionCount(sessions.size());
    }

    /**
     * Return list of already blocked ids. List is empty if block successfully
     */
    @Override
    public Collection< Identifier> block(final Collection<Identifier> ids, SessionContainer sessions, BlockLevel blockLevel) {
        LOGGER.debug("Try block ids:{} session:{} server:{}", new Object[] { ids, sessions, serverModel.getServerName() });
        if (sessions == null || ids == null || sessions.getMainSessionId()==null || sessions.getSessionId()==null) {
            LOGGER.warn(" Try block session Id : " + sessions + " ids:" + ids);
            return Collections.emptyList();
        }

        final List<Identifier> blocked = new ArrayList<>(ids.size());
        final List<Identifier> rejected = new ArrayList<>(ids.size());
        final Sessions sessionData = new Sessions(sessions.getSessionId(), sessions.getMainSessionId(), blockLevel);
        for (final Identifier identifier : ids) {
            synchronized (blocks) {
                Sessions bSession = blocks.get(identifier);
                if (bSession == null) {
                    blocks.put(identifier, sessionData);
                    blocked.add(identifier);
                } else if (checkBlocker(bSession, sessions)) {
                    bSession.sessionIds.add(sessions.getSessionId());
                } else {
                    LOGGER.warn(" id:{} cannot be blocked. It blocked by session:{}. Server:{}",
                            new Object[] { identifier, bSession, serverModel.getServerName() });
                    rejected.add(identifier);
                    break;
                }
            }
        }
        // rollback blocking
        if (!rejected.isEmpty()) {
            LOGGER.debug("result  ids:{} unsucсessfully blocked for the session:{}", ids, sessions);
            for (final Identifier id : blocked) {
                Sessions blockSessions = blocks.get(id);
                if (blockSessions != null) {
                    blockSessions.remove(sessions.getSessionId());
                    if (blockSessions.sessionIds.isEmpty()) {
                        blocks.remove(id);
                    }
                }
            }
        } else {
            LOGGER.debug("result  ids:{} sucсessfully blocked for the session:{}", ids, sessions);
            Map<String, Set<Identifier>> inMainSession = this.sessions.get(sessions.getMainSessionId());
            if (inMainSession == null) {
                inMainSession = new HashMap<>();
                this.sessions.put(sessions.getMainSessionId(), inMainSession);
            }
            Set<Identifier> blockedIds = inMainSession.get(sessions.getSessionId());
            if (blockedIds == null) {
                blockedIds = new HashSet<>();
                inMainSession.put(sessions.getSessionId(), blockedIds);
            }
            blockedIds.addAll(ids);

        }
        statisticBean.setBlockCount(blocks.size());
        statisticBean.setSessionCount(this.sessions.size());
        return rejected;
    }

    @Override
    public void unblock(SessionContainer sessions) {
        LOGGER.debug("unBlock:{}, server:{}, sessions:{}", new Object[] { sessions, serverModel.getServerName(), sessions });
        if (sessions == null) {
            LOGGER.debug(" try unBlock : session Null");
            return;
        }
        final Map<String, Set<Identifier>> childSessions = this.sessions.get(sessions.getMainSessionId());
        if (childSessions != null) {

            for (final String ses : sessions.getSessions()) {
                final Set<Identifier> ids = childSessions.remove(ses);
                if (ids != null) {
                    LOGGER.debug("unBlock id:{},  server:{}", sessions.getSessions(), serverModel.getServerName());
                    for (final Identifier identifier : ids) {
                        blocks.remove(identifier);
                    }
                    ids.removeAll(sessions.getSessions());
                }
                this.sessions.remove(ses);
            }

            LOGGER.debug("unBlock id:{}, server:{}", blocks, serverModel.getServerName());
        }

        statisticBean.setBlockCount(blocks.size());
        statisticBean.setSessionCount(this.sessions.size());
    }

    @Override
    public void start() throws SystemException {

    }

    @Override
    public void stop() {
        blocks.clear();
        sessions.clear();
        statisticBean.setBlockCount(blocks.size());
        statisticBean.setSessionCount(sessions.size());
    }

    
    @Override
    public boolean checkBlockLevel(Iterable<Identifier> ids, BlockLevel blockLevel, SessionContainer sessions){
        for (Identifier identifier : ids) {
            Sessions blockSessions=blocks.get(identifier);
            if(blockSessions!= null){
                if(sessions != null && blockSessions.getMainId().equals(sessions.getMainSessionId()) && blockSessions.blockLevel.getLevel() > blockLevel.getLevel()){
                    // mey be add conditions 
                        return true;
                }
            }
        }
        return false;
    } 
    
    /**
     * 
     * @param blockSessions
     * @param sessionContainer
     * @return true if block ok. or false in field block
     */
    private boolean checkBlocker(final Sessions blockSessions,SessionContainer sessionContainer) {
        if(!blockSessions.mainId.equals(sessionContainer.getMainSessionId())){
            return false;
        }
        int index = blockSessions.sessionIds.indexOf(sessionContainer.getSessionId());
        if(index < 0 || index==blockSessions.sessionIds.size() - 1){
            return true;
        }
        return false;
    }

    private static class Sessions {
        private final List<String> sessionIds= new ArrayList<>();
        private final String mainId;
        BlockLevel blockLevel;

        public Sessions(final String sessionId, final String mainId,  BlockLevel blockLevel) {
            super();
            this.blockLevel = blockLevel;
            this.sessionIds.add(sessionId);
            this.mainId = mainId;
        }

        public void remove(String sessionId) {
            int lastindex=sessionIds.size()-1;
            if(lastindex>=0){
                if(sessionId.equals(sessionIds.get(lastindex))){
                    sessionIds.remove(lastindex);
                }
            }
        }

        public String getMainId() {
            return mainId;
        }

        @Override
        public String toString() {
            return "Sessions [sessionIds=" + sessionIds + ", mainId=" + mainId + ", blockLevel=" + blockLevel + "]";
        }



    }
/*
    private static class BlockedSessions {
        private final List<Sessions> sessions= new ArrayList();
        int stableLevel = BlockService.UPDATE_STABLE;

        private BlockedSessions(Sessions sessions, int stableLevel) {
            this.sessions = new HashSet<>(1,1);
            this.sessions.add(sessions);
            this.stableLevel=stableLevel;
        }
        
        private void remove(Sessions sessions){
            this.sessions.remove(sessions);
        }
        private BlockedSessions(Set<Sessions> sessions) {
            this.sessions = sessions;
        }
        private boolean isEmpty(){
            return sessions.isEmpty();
        }
    }
*/
}
