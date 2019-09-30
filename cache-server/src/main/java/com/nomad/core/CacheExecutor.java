package com.nomad.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.InternalTransactDataStore;
import com.nomad.exception.BlockException;
import com.nomad.exception.LogicalException;
import com.nomad.exception.SystemException;
import com.nomad.exception.UnsupportedModelException;
import com.nomad.model.Criteria;
import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.model.core.SessionContainer;
import com.nomad.model.criteria.StatisticResult;
import com.nomad.model.update.UpdateRequest;
import com.nomad.server.BlockService;
import com.nomad.server.processing.ObjectProcessing;
import com.nomad.utility.ModelUtil;

public class CacheExecutor implements ObjectProcessing{
    protected final static Logger LOGGER = LoggerFactory.getLogger(ObjectProcessingImpl.class);
    protected volatile InternalTransactDataStore store;

    @Override
    public Collection<Model> get(Collection<Identifier> identifiers, SessionContainer sessions) throws LogicalException, SystemException {
        return store.get(identifiers, sessions);
    }

    @Override
    public Collection<Model> put(Collection<Model> identifiers, SessionContainer sessions) throws LogicalException, SystemException {
        return store.put(identifiers, sessions);
    }

    @Override
    public Collection<Identifier> delete(Collection<Identifier> identifiers,  SessionContainer sessions)
            throws LogicalException, SystemException {
        return store.remove(identifiers, sessions);
    }

    @Override
    public void commit(SessionContainer sessions) throws SystemException {
        store.commit(sessions);
    }

    @Override
    public void rollback(SessionContainer sessions) {
        store.rollback(sessions);
    }

    @Override
    public void closeSession(SessionContainer sessions) throws SystemException {
        store.closeSession(sessions);
    }

    @Override
    public void update(Collection<UpdateRequest> updateRequests, Collection<Identifier> identifiers, SessionContainer sessions)
            throws UnsupportedModelException, BlockException, SystemException, LogicalException {
        store.update(updateRequests, identifiers, sessions);
    }

    @Override
    public Collection<Identifier> inCache(Collection<Identifier> identifiers, Collection<Model> models)  throws SystemException, UnsupportedModelException, LogicalException {
        Collection<Identifier> result = null;
        if (identifiers != null && identifiers.size() > 0) {
            result = identifiers;
        }
        if (models != null) {
            result = new ArrayList<>(models.size());
            for (final Model model : models) {
                result.add(model.getIdentifier());
            }
        }
        if (result != null) {
            result = store.contains(result);
        }
        return result;
    }

    @Override
    public StatisticResult<?> getIdentifiers(Criteria<? extends Model> criteria) throws UnsupportedModelException, SystemException {
        return store.getIdentifiers(criteria);
    }

    @Override
    public void unblock(SessionContainer sessions) throws SystemException {
        store.unblock(sessions);
    }

    @Override
    public void commitPhase2(SessionContainer sessions) throws SystemException {
        store.commitPhase2(sessions);
    }

    @Override
    public void commitPhase1(SessionContainer sessions) throws SystemException {
        store.commit(sessions);
    }

    @Override
    public Collection<Identifier> block(Collection<Identifier> identifiers, Collection<Model> models, SessionContainer sessions) throws SystemException {
        if (identifiers == null) {
            identifiers = ModelUtil.getIdentifiers(models);
        }
        if (identifiers == null) {
            LOGGER.warn("nothing to do");
            return identifiers;
        }
        return store.block(identifiers, sessions, BlockService.BlockLevel.READ_LEVEL);

    }

    @Override
    public Collection<Identifier> deleteFromCache(Collection<Identifier> identifiers, Collection<Model> models, SessionContainer sessions)
            throws LogicalException, SystemException {
        if (identifiers != null) {
            return store.remove(identifiers, sessions);
        } else if (models != null) {
            identifiers = ModelUtil.getIdentifiers(models);
            return store.remove(identifiers, sessions);
        }
        return Collections.emptyList();
    }

    @Override
    public Collection<Model> putIntoCache(Collection<Model> models, SessionContainer sessions) throws LogicalException, SystemException {
        return store.put(models, sessions);
    }

    @Override
    public Collection<Model> getFromCache(Collection<Identifier> identifiers, Collection<Model> models, SessionContainer sessions)
            throws LogicalException, SystemException {
        if (identifiers == null) {
            identifiers = new ArrayList<>(models.size());
        }
        if (models != null) {
            identifiers.addAll(ModelUtil.getIdentifiers(models));
        }
        return store.getFromCache(identifiers, sessions);
    }

}
