package com.nomad.server;

import java.util.Collection;
import java.util.Set;

import com.nomad.exception.SystemException;
import com.nomad.exception.UnsupportedModelException;
import com.nomad.model.Criteria;
import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.model.criteria.StatisticResult;

public interface ModelStore <K> {

    int getDataCount();

    Collection<Model> get(Collection<Identifier> id) throws SystemException;

    Collection<Model> put(Collection<Model> model, String sessionId) throws SystemException;

    void remove(Collection<? extends Identifier> id, String sessionId) throws SystemException;

    Set<Identifier> getIdentifiers();

    Collection<Identifier> contains(Collection<Identifier> ids) throws SystemException;

    Collection<Model> getFromCache(Collection<Identifier> ids) throws SystemException;

    <T extends Model> Collection<T> getList(Criteria< T> criteria) throws UnsupportedModelException, SystemException;

    <T extends Model> StatisticResult<T> getIdentifiers(Criteria<T> criteria) throws UnsupportedModelException, SystemException;

    DateIdentifier getOldestKey();

    void removeFromCache(Identifier identifier);

    void clean();

}
