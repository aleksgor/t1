package com.nomad.store.transaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nomad.exception.SystemException;
import com.nomad.message.MessageSenderReceiver;
import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.server.transaction.TransactElement;
import com.nomad.server.transaction.TransactElement.Operation;
import com.nomad.server.transaction.TransactionMarker;
import com.nomad.utility.MessageUtil;

public class TransactionMarkerImpl implements TransactionMarker {

    private final List<TransactElement> index = new ArrayList<>();
    private volatile long lastTime;
    private final MessageSenderReceiver msr;

    public TransactionMarkerImpl(final MessageSenderReceiver msr) {
        this.msr = msr;
    }

    public TransactionMarkerImpl(final Identifier id, final String sessionId, final MessageSenderReceiver msr) {
        this(msr);
        if (id != null) {
            index.add(new TransactElementImpl(id, TransactElementImpl.Operation.EMPTY_OPERATION, sessionId));
        }
    }

    @Override
    public TransactElement addOperation(final Model newModel, final TransactElementImpl.Operation operation, final String sessionId) throws SystemException {
        lastTime = System.currentTimeMillis();
        final Model mNew = MessageUtil.clone(newModel, msr); 
        final TransactElementImpl element = new TransactElementImpl(mNew, operation, sessionId);
        index.add(element);
        return element;
    }

    @Override
    public Map<Identifier, TransactElement> getLastTransactVersion(final Collection<Identifier> ids) {
        lastTime = System.currentTimeMillis();
        final Set<Identifier> identifiers = new HashSet<>(ids);
        final Map<Identifier, TransactElement> result = new HashMap<>(ids.size(), 1);
        lastTime = System.currentTimeMillis();

        for (int i = index.size(); i > 0; i--) {
            final TransactElement element = index.get(i - 1);
            if (identifiers.contains(element.getIdentifier())) {
                result.put(element.getIdentifier(), element);
                identifiers.remove(element.getIdentifier());
            }
        }
        return result;
    }

    @Override
    public Collection<TransactElement> getLastTransactVersionForSessions(final Collection<String> sessionIds) {
        lastTime = System.currentTimeMillis();
        final Set<String> sessions = new HashSet<>(sessionIds);
        final Map<Identifier, TransactElement> result = new HashMap<>();
        for (int i = index.size(); i > 0; i--) {
            final TransactElement element = index.get(i - 1);
            if (sessions.contains(element.getSessionId())) {
                if (!result.containsKey(element.getIdentifier())) {
                    result.put(element.getIdentifier(), element);
                }
                sessions.remove(element.getIdentifier());
            }
        }
        return result.values();
    }

    @Override
    public Collection<TransactElement> getAllTransactVersionForSessions(final Collection<String> sessionIds) {
        lastTime = System.currentTimeMillis();
        final Set<String> sessions = new HashSet<>(sessionIds);
        final Collection<TransactElement> result = new ArrayList<>();
        for (final TransactElement te : index) {
            if (sessions.contains(te.getSessionId())) {
                result.add(te);
            }
        }
        return result;
    }

    /**
     * remove all elements for sessions and return all free identifiers
     */
    @Override
    public void removeTransactElements(final Collection<String> sessionIds) {
        lastTime = System.currentTimeMillis();
        final Set<String> sessions = new HashSet<>(sessionIds);
        final List<Identifier> result = new ArrayList<>();
        for (int i = 0; i < index.size(); i++) {
            final TransactElement element = index.get(i);
            if (sessions.contains(element.getSessionId())) {
                result.add(element.getIdentifier());
                index.remove(i);
                i--;
            }

        }
    }

    @Override
    public Collection<Identifier> getContainsIds(final List<? extends Identifier> ids) {
        lastTime = System.currentTimeMillis();
        final Set<Identifier> result = new HashSet<>(ids.size());
        final Set<Identifier> resultIds = new HashSet<>(ids);
        for (final TransactElement element : index) {
            if (resultIds.contains(element.getIdentifier())) {
                result.add(element.getIdentifier());
                resultIds.remove(element.getIdentifier());
            }

        }
        return result;
    }

    @Override
    public void commitPhase2(final Collection<String> sessionIds) {
        lastTime = System.currentTimeMillis();
        final Set<String> sessions = new HashSet<>(sessionIds);
        for (int i = index.size(); i > 0; i--) {
            final TransactElement element = index.get(i - 1);
            if (element.isPhase2() && sessions.contains(element.getSessionId())) {
                index.remove(i-1);
                i--;
            }
        }
        sessions.clear();
    }

    @Override
    public boolean contains(final Identifier identifier) {
        lastTime = System.currentTimeMillis();
        return containsQuietly(identifier);
    }

    @Override
    public boolean containsQuietly(final Identifier identifier) {
        for (final TransactElement element : index) {
            if (identifier.equals(element.getIdentifier())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Collection<? extends Identifier> getIdentifiersQuietly() {
        final Set<Identifier> result = new HashSet<>();
        for (final TransactElement element : index) {
            result.add(element.getIdentifier());
        }
        return result;
    }

    @Override
    public Collection<? extends Identifier> getNoDeleteIdentifiersQuietly() {
        final Set<Identifier> result = new HashSet<>();
        for (final TransactElement element : index) {
            if(!Operation.DELETE_MODEL.equals(element.getOperation())){
                result.add(element.getIdentifier());
            }
        }
        return result;
    }

    @Override
    public long getLastTime() {
        return lastTime;
    }

    @Override
    public String toString() {
        return "TransactionMarkerImpl [index=" + index + ", lastTime=" + lastTime + ", msr=" + msr + "]";
    }

}
