package com.nomad.server.transaction;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.nomad.exception.SystemException;
import com.nomad.model.Identifier;
import com.nomad.model.Model;


public interface TransactionMarker{

    TransactElement addOperation(final Model newModel, final TransactElement.Operation operation, final String sessionId) throws SystemException;

    Map<Identifier, TransactElement> getLastTransactVersion(final Collection<Identifier> ids);

    Collection<TransactElement> getLastTransactVersionForSessions(final Collection<String> sessionIds);

    Collection<TransactElement> getAllTransactVersionForSessions(final Collection<String> sessionIds);

    void removeTransactElements(final Collection<String> sessionIds);

    Collection<Identifier> getContainsIds(List<? extends Identifier> ids);

    void commitPhase2(Collection<String> sessionIds);

    boolean contains(Identifier identifier);

    boolean containsQuietly(Identifier identifier);

    Collection<? extends Identifier> getIdentifiersQuietly();

    long getLastTime();

    Collection<? extends Identifier> getNoDeleteIdentifiersQuietly();

}
