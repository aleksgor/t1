package com.nomad.store;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.message.MessageSenderReceiver;
import com.nomad.model.Identifier;
import com.nomad.server.transaction.TransactInfo;
import com.nomad.server.transaction.TransactionMarker;
import com.nomad.store.transaction.TransactionMarkerImpl;
import com.nomad.utility.SynchronizedLinkedHashMap;

public class TransactInfoImpl implements TransactInfo {
    // sessionId-> Tm
    private volatile SynchronizedLinkedHashMap<String, TransactionMarker> transactions;
    @SuppressWarnings("unused")
    private static Logger LOGGER = LoggerFactory.getLogger(TransactInfoImpl.class);
    private final MessageSenderReceiver msr;

    public TransactInfoImpl(final MessageSenderReceiver msr) {
        transactions = new SynchronizedLinkedHashMap<>();
        this.msr=msr;
    }

    @Override
    public void updateTime(final String sessionId) {
        getTransactionMarker(sessionId);
    }

    /*
    @Override
    public void removeElements(final Collection<String> serssionIds,final String mainSession) {
        final TransactionMarker tm =getTransactionMarker(mainSession);
        if(tm!=null){
            tm.removeTransactElements(serssionIds);
        }
    }
     */
    @Override
    public void clear() {
        transactions.clear();
    }

    public List<? extends Identifier> getContainsIds(final List<? extends Identifier> ids, final String sessionId) {
        final List<Identifier> result = new ArrayList<>(ids.size());
        for (final TransactionMarker tm : transactions.values()) {
            tm.getContainsIds(ids);
        }
        final TransactionMarker marker = getTransactionMarker(sessionId);

        if (marker != null) {
            for (final Identifier identifier : ids) {
                if (marker.contains(identifier)) {
                    result.add(identifier);
                }
            }
        }
        return result;
    }

    @Override
    public Collection<Identifier> getContainsIds(final Collection< Identifier> ids) {
        final Collection<Identifier> result = new ArrayList<>(ids.size());
        final Set<Identifier> existedIds = new HashSet<>();
        for (final TransactionMarker transactionMarker : transactions.values()) {
            existedIds.addAll(transactionMarker.getNoDeleteIdentifiersQuietly());
        }
        for (final Identifier id : ids) {
            if (existedIds.contains(id)) {
                result.add(id);
            }
        }
        return result;
    }

    @Override
    public TransactionMarker getTransactionMarker(final String sessionId) {
        TransactionMarker result = transactions.get(sessionId);
        if (result == null) {
            result = new TransactionMarkerImpl(null, sessionId, msr);
            transactions.put(sessionId, result);
        }
        return result;
    }

    @Override
    public TransactionMarker removeTransactionMarker(final String sessionId) {
        return transactions.remove(sessionId);
    }


    @Override
    public void cleanTransactionMarker(final Collection<String> sessionIds) {
        getAndRemoveTransactionMarker(sessionIds);
    }

    @Override
    public void setTransactionMarker(final TransactionMarker marker, final String sessionId) {
        transactions.put(sessionId, marker);
    }




    @Override
    public String toString() {
        return "TransactInfoImpl [transactions=" + transactions + "]";
    }

    @Override
    public Collection<TransactionMarker> getTransactionMarker(final Collection<String> sessionIds) {
        final Collection<TransactionMarker> result = new ArrayList<>(sessionIds.size());
        for (final String sessionId : sessionIds) {
            TransactionMarker marker = transactions.get(sessionId);
            if (marker == null) {
                marker = new TransactionMarkerImpl(null, sessionId, msr);
                transactions.put(sessionId,marker);
            }
            result.add(marker);
        }
        return result;
    }

    @Override
    public Collection<TransactionMarker> getAndRemoveTransactionMarker(final Collection<String> sessionIds) {
        return null;
    }

}
