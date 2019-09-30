package com.nomad.server.service.childserver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.model.CacheMatcherModel;
import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.model.StoreModel;
import com.nomad.server.CacheMatcher;

public class BestServerCalculator {
    private static Logger LOGGER = LoggerFactory.getLogger(BestServerCalculator.class);
    private volatile StoreModel storeModel;

    private volatile RandomCollectionStoreConnectionPools cacheServers;

    public StoreModel getStoreModel() {
        return storeModel;
    }

    public BestServerCalculator(final StoreModel model) {
        super();
        cacheServers = new RandomCollectionStoreConnectionPools();
        storeModel = model;
    }

    public Collection<StoreConnectionPool> getReadyCachePools() {
        final Collection<ConnectPoolWithCacheMatcher> ls = cacheServers.getRandomList();
        final List<StoreConnectionPool> result = new ArrayList<>(ls.size());
        for (final ConnectPoolWithCacheMatcher connectPoolWithCacheMatcher : ls) {
            result.add(connectPoolWithCacheMatcher.storeConnectionPool);
        }
        return result;
    }


    public Collection<StoreConnectionPool> getUniqueDataSourcesCachePools() {
        final Collection<ConnectPoolWithCacheMatcher> ls = cacheServers.getAllElements();

        final Map<String, StoreConnectionPool> result = new HashMap<>(ls.size(), 1);
        for (final ConnectPoolWithCacheMatcher connectPoolWithCacheMatcher : ls) {
            List<String> dataSoutces = connectPoolWithCacheMatcher.storeConnectionPool.getDataSources();
            if (dataSoutces != null) {
                for (String ds : dataSoutces) {
                    result.put(ds, connectPoolWithCacheMatcher.storeConnectionPool);
                }
            }
        }
        return result.values();
    }

    public Collection<StoreConnectionPool> getCachePools() {

        final Collection<ConnectPoolWithCacheMatcher> ls = cacheServers.getAllElements();
        final List<StoreConnectionPool> result = new ArrayList<>(ls.size());
        for (final ConnectPoolWithCacheMatcher connectPoolWithCacheMatcher : ls) {
            result.add(connectPoolWithCacheMatcher.storeConnectionPool);
        }
        return result;
    }

    public List<String> getPoolIdsForCacheManager(final Identifier id) {
        final List<String> result = new ArrayList<>(storeModel.getCopyCount());
        final Collection<StoreConnectionPool> list = cacheServers.getRandomList(storeModel.getCopyCount(), id);
        for (final StoreConnectionPool storeConnectionPool : list) {
            result.add(storeConnectionPool.getPoolId());
        }
        return result;
    }


    public Collection<String> getFullPoolIdsForCacheManager(final Identifier id) {
        return cacheServers.getFullList(id);
    }

    public void addConnectPool(final StoreConnectionPool server, final StoreModel storeModel) {
        LOGGER.info("Add server:" + server.getPoolId() + " For:" + storeModel);
        final ConnectPoolWithCacheMatcher poolWithMatcher = new ConnectPoolWithCacheMatcher(server,getCacheMatcher(storeModel.getCacheMatcherModel()));
        cacheServers.add(10d, poolWithMatcher);
    }

    public void updateConnectPool(final StoreConnectionPool pool,  final double rating) {
        LOGGER.info("Update client information:" + pool.getPoolId() +" Rating:"+rating);
        final ConnectPoolWithCacheMatcher cpwcm= cacheServers.getConnectPoolWithCacheMatcherByStoreConnectionPool(pool);
        if(cpwcm!=null){
            cacheServers.update(rating, cpwcm);
        }
    }

    public StoreConnectionPool getRandomPoolForCache() {
        return cacheServers.next().storeConnectionPool;
    }

    @Override
    public String toString() {
        return "BestServerCalculator [model=" + storeModel +  ", Cache Servers=" + cacheServers +"]";
    }

    @SuppressWarnings("unchecked")
    private CacheMatcher getCacheMatcher(final CacheMatcherModel cmModel) {
        CacheMatcher result = null;
        if (cmModel != null && cmModel.getClazz() != null) {
            try {
                final Class<? extends CacheMatcher> clazz = (Class<? extends CacheMatcher>) Class.forName(cmModel.getClazz());
                result = clazz.newInstance();
                result.setProperties(cmModel.getProperties());
                result.init();
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return result;
    }

    static class ConnectPoolWithCacheMatcher {
        final StoreConnectionPool storeConnectionPool;
        final CacheMatcher cacheMatcher;

        public ConnectPoolWithCacheMatcher(final StoreConnectionPool storeConnectionPool, final CacheMatcher cacheMatcher) {
            super();
            this.storeConnectionPool = storeConnectionPool;
            this.cacheMatcher = cacheMatcher;
        }

        public boolean match(final Model m){
            if(cacheMatcher==null || m == null){
                return true;
            }
            return cacheMatcher.matchModel(m);
        }
        public boolean match(final Identifier id){
            if(cacheMatcher==null || id == null){
                return true;
            }
            return cacheMatcher.matchIdentifier(id);
        }
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((storeConnectionPool == null) ? 0 : storeConnectionPool.hashCode());
            return result;
        }
        @Override
        public boolean equals(final Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final ConnectPoolWithCacheMatcher other = (ConnectPoolWithCacheMatcher) obj;
            if (storeConnectionPool == null) {
                if (other.storeConnectionPool != null)
                    return false;
            } else if (!storeConnectionPool.equals(other.storeConnectionPool))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "ConnectPoolWithCacheMatcher [storeConnectionPool=" + storeConnectionPool.getPoolId() + "]";
        }

    }
}
