package com.nomad.model;

import java.util.Collection;

import com.nomad.exception.SystemException;
import com.nomad.exception.UnsupportedModelException;

public interface DataStore {

    Collection<Model> get(Collection<Identifier> id) throws UnsupportedModelException, SystemException;

    Collection<Model> put(Collection<Model> data, String sessionId) throws UnsupportedModelException, SystemException;

    void remove(Collection< Identifier> id, String sessionId) throws UnsupportedModelException, SystemException;

    Collection<Identifier> contains(Collection<Identifier> ids) throws UnsupportedModelException, SystemException;

    Collection<Model> getFromCache(Collection<Identifier> ids) throws UnsupportedModelException, SystemException;

}
