package com.nomad.model;

import java.util.Collection;

import com.nomad.exception.LogicalException;
import com.nomad.exception.SystemException;
import com.nomad.model.core.SessionContainer;
import com.nomad.server.ServiceInterface;

public interface TransactDataStore extends ServiceInterface {
    Collection<Model> get(Collection< Identifier> id, SessionContainer sessions) throws  LogicalException, SystemException;

    Collection<Model> put(Collection<Model> data, SessionContainer sessions) throws LogicalException, SystemException;

    Collection<Identifier> remove(final Collection<Identifier> ids, final SessionContainer sessions) throws LogicalException, SystemException;

    void commit(SessionContainer sessions) throws SystemException;

    void rollback(SessionContainer sessions);

    void closeSession(SessionContainer sessions) throws SystemException;

    Collection<Identifier> contains(Collection<Identifier> id) throws LogicalException, SystemException;

    Collection<Model> getFromCache(Collection<Identifier> id, SessionContainer sessions) throws LogicalException, SystemException;

    void commitPhase2(SessionContainer sessions) throws SystemException;

}
