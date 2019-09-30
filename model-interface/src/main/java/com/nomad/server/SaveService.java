package com.nomad.server;

import java.util.Collection;

import com.nomad.exception.LogicalException;
import com.nomad.exception.SystemException;
import com.nomad.model.Identifier;

public interface SaveService extends ServiceInterface {

    Collection<Identifier> isReadyToSave(Collection<Identifier> ids, String sessionId) throws SystemException, LogicalException;

    void cleanSession(Collection<String> sessionId) throws  SystemException, LogicalException;

    boolean isAvailable();

    Collection<Identifier> internalCheck(Collection<Identifier> ids, String sessionId, long clientId) throws SystemException, LogicalException;

}
