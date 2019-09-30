package com.nomad.server.service.childserver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.nomad.model.ConnectStatus;
import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.server.CacheMatcher;
import com.nomad.server.service.childserver.BestServerCalculator.ConnectPoolWithCacheMatcher;
import com.nomad.utility.RandomCollection;

public class RandomCollectionStoreConnectionPools extends RandomCollection<ConnectPoolWithCacheMatcher> {

    public Collection<StoreConnectionPool> getRandomList(final int length, final Model model) {

        final List<StoreConnectionPool> pools = new ArrayList<>();
        final Collection<ConnectPoolWithCacheMatcher> randomCollection = getRandomList();
        for (final ConnectPoolWithCacheMatcher connectPoolWithCacheMatcher : randomCollection) {
            if (ConnectStatus.OK.equals(connectPoolWithCacheMatcher.storeConnectionPool.getStatus())) {
                if (match(model, connectPoolWithCacheMatcher.cacheMatcher)) {
                    pools.add(connectPoolWithCacheMatcher.storeConnectionPool);
                }
            }
        }
        if (pools.size() > length) {
            return pools.subList(0, length);
        }
        return pools;
    }

    public Collection<StoreConnectionPool> getRandomList(final int length, final Identifier id) {
        final List<StoreConnectionPool> pools = new ArrayList<>();
        final Collection<ConnectPoolWithCacheMatcher> randomCollection = getRandomList();
        for (final ConnectPoolWithCacheMatcher connectPoolWithCacheMatcher : randomCollection) {
            if (ConnectStatus.OK.equals(connectPoolWithCacheMatcher.storeConnectionPool.getStatus())) {
                if (match(id, connectPoolWithCacheMatcher.cacheMatcher)) {
                    pools.add(connectPoolWithCacheMatcher.storeConnectionPool);
                }
            }
        }
        if (pools.size() > length) {
            return pools.subList(0, length);
        }
        return pools;

    }

    public Collection<String> getFullList(final Identifier id) {
        final List<String> poolsIds = new ArrayList<>();
        final Collection<ConnectPoolWithCacheMatcher> randomCollection = getAllElements();
        for (final ConnectPoolWithCacheMatcher connectPoolWithCacheMatcher : randomCollection) {
            if (ConnectStatus.OK.equals(connectPoolWithCacheMatcher.storeConnectionPool.getStatus())) {
                if (match(id, connectPoolWithCacheMatcher.cacheMatcher)) {
                    poolsIds.add(connectPoolWithCacheMatcher.storeConnectionPool.getPoolId());
                }
            }
        }
        return poolsIds;
    }

    public ConnectPoolWithCacheMatcher getConnectPoolWithCacheMatcherByStoreConnectionPool(final StoreConnectionPool pool) {
        final Double weight = counterMap.get(new ConnectPoolWithCacheMatcher(pool, null));
        if (weight != null) {
            return get(weight);
        }
        return null;
    }

    public boolean match(final Model model, final CacheMatcher cacheMatcher) {
        if (cacheMatcher == null) {
            return true;
        }
        synchronized (cacheMatcher) {
            return cacheMatcher.matchModel(model);
        }
    }

    public boolean match(final Identifier id, final CacheMatcher cacheMatcher) {
        if (cacheMatcher == null) {
            return true;
        }
        synchronized (cacheMatcher) {
            return cacheMatcher.matchIdentifier(id);
        }
    }

}
