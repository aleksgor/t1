package com.nomad.server.processing;


import java.util.Collection;

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

public interface ObjectProcessing {

    Collection<Model> get(Collection<Identifier> identifiers, SessionContainer sessions) throws LogicalException, SystemException;

    Collection<Model> put(Collection<Model> models, SessionContainer sessions) throws LogicalException, SystemException;

    Collection<Identifier> delete(Collection<Identifier> identifiers, SessionContainer sessions)
            throws LogicalException, SystemException;

    void commit(SessionContainer sessions) throws SystemException;

    void rollback(SessionContainer sessions);

    void closeSession(SessionContainer sessions) throws SystemException;

    void update(Collection<UpdateRequest> updateRequests, Collection<Identifier> identifiers, SessionContainer sessions)
            throws UnsupportedModelException, BlockException, SystemException, LogicalException;

    Collection<Identifier> inCache(Collection<Identifier> identifiers, Collection<Model> models)
            throws SystemException, UnsupportedModelException, LogicalException;

    StatisticResult<?> getIdentifiers(Criteria<? extends Model> criteria) throws UnsupportedModelException, SystemException;

    void unblock(SessionContainer sessions) throws SystemException;

    void commitPhase2(SessionContainer sessions) throws SystemException;

    void commitPhase1(SessionContainer sessions) throws SystemException;

    Collection<Identifier> block(Collection<Identifier> identifiers, Collection<Model> models, SessionContainer sessions) throws SystemException;

    Collection<Identifier> deleteFromCache(Collection<Identifier> identifiers, Collection<Model> models, SessionContainer sessions)
            throws LogicalException, SystemException;

    Collection<Model> putIntoCache(Collection<Model> models, SessionContainer sessions) throws LogicalException, SystemException;

    Collection<Model> getFromCache(Collection<Identifier> identifiers, Collection<Model> models, SessionContainer sessions)
            throws LogicalException, SystemException;


}
