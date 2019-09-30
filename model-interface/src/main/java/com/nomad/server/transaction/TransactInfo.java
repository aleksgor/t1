package com.nomad.server.transaction;

import java.util.Collection;

import com.nomad.model.Identifier;

public interface TransactInfo {
    void updateTime(String sessionId);

    void clear();

    Collection<Identifier> getContainsIds(Collection<Identifier> ids);

    Collection<TransactionMarker> getTransactionMarker(Collection<String> sessionId);

    TransactionMarker getTransactionMarker(String sessionId);

    Collection<TransactionMarker> getAndRemoveTransactionMarker(Collection<String> sessionIds);

    void cleanTransactionMarker(Collection<String> sessionId);

    void setTransactionMarker(TransactionMarker marker, String sessionId);

    TransactionMarker removeTransactionMarker(String sessionId);


}
