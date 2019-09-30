package com.nomad.store;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.InternalDataStore;
import com.nomad.exception.ErrorCodes;
import com.nomad.exception.SystemException;
import com.nomad.exception.UnsupportedModelException;
import com.nomad.model.Criteria;
import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.model.StoreModel;
import com.nomad.model.criteria.StatisticResult;
import com.nomad.server.DateIdentifier;
import com.nomad.server.ModelStore;
import com.nomad.server.SaveService;
import com.nomad.server.ServerContext;
import com.nomad.utility.DataInvokerPool;

public class Store implements InternalDataStore{
    private static Logger LOGGER = LoggerFactory.getLogger(Store.class);

    private final Map<String, ModelStore<?>> store = new ConcurrentHashMap<>();

    public Store(final ServerContext context) {
    }

    @Override
    public Collection<Model> get(final Collection<Identifier> ids) throws UnsupportedModelException, SystemException {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        final ModelStore<?> mStore = store.get(ids.iterator().next().getModelName());
        if (mStore == null) {
            throw new UnsupportedModelException(ErrorCodes.Cache.ERROR_CACHE_MODEL_UNSUPPORTED, ids.iterator().next().getModelName() );
        }
        return mStore.get( ids);
    }

    @Override
    public Collection<Model> put(final Collection<Model> data, final String sessionId) throws UnsupportedModelException, SystemException {
        if (data == null || data.isEmpty()) {
            return new ArrayList<>();
        }
        final ModelStore<?> mStore = store.get(data.iterator().next().getModelName());
        if (mStore == null) {
            throw new UnsupportedModelException(ErrorCodes.Cache.ERROR_CACHE_MODEL_UNSUPPORTED, data.iterator().next().getModelName());
        }
        return mStore.put(data, sessionId);
    }

    @Override
    public void remove(final Collection<Identifier> ids, final String sessionId) throws UnsupportedModelException, SystemException {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        final ModelStore<?> mStore = store.get(ids.iterator().next().getModelName());
        if (mStore == null) {
            throw new UnsupportedModelException(ErrorCodes.Cache.ERROR_CACHE_MODEL_UNSUPPORTED,  ids.iterator().next().getModelName() );
        }
        mStore.remove(ids, sessionId);

    }

    @Override
    public void registerModel(final StoreModel model, final DataInvokerPool dataInvoker, final SaveService saveService, final ServerContext context)
            throws SystemException {
        if (model.isCache()) {
            ModelStore<?> modeStore = null;
            switch (model.getStoreType()) {
            case BYTES:
                modeStore = new ModelBytesStore(model.isReadThrough(), model.isWriteThrough(), dataInvoker, saveService, context, model);
                break;
            case ZIPPED_BYTES:
                modeStore = new ZippedModelBytesStore(model.isReadThrough(), model.isWriteThrough(), dataInvoker, saveService, context, model);
                break;
            case OBJECT:
                modeStore = new ModelStoreImpl(model.isReadThrough(), model.isWriteThrough(), dataInvoker, saveService, context, model);
                break;
            }

            store.put(model.getModel(), modeStore);
        }

    }

    @Override
    public ModelStore<?> getModelStore(final String modelName) {
        return store.get(modelName);
    }

    @Override
    public void cleanSession(final int timeout) {

    }

    @Override
    public boolean checkSession(final String sessionId) {

        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection< Identifier> contains(final Collection< Identifier> ids) throws UnsupportedModelException, SystemException {
        if (ids == null || ids.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        final Identifier id = ids.iterator().next();
        final ModelStore<?> mStore = store.get(id.getModelName());
        if (mStore == null) {
            throw new UnsupportedModelException(ErrorCodes.Cache.ERROR_CACHE_MODEL_UNSUPPORTED, id.getModelName() );
        }
        return mStore.contains(ids);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<Model> getFromCache(final Collection<Identifier> ids) throws UnsupportedModelException, SystemException {
        if (ids == null || ids.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        final Identifier id = ids.iterator().next();
        final ModelStore<?> mStore = store.get(id.getModelName());
        if (mStore == null) {
            throw new UnsupportedModelException(ErrorCodes.Cache.ERROR_CACHE_MODEL_UNSUPPORTED,  id.getModelName() );
        }
        return mStore.getFromCache(ids);
    }

    @Override
    public int getDataCount() {
        int result = 0;
        for (final ModelStore<?> models : store.values()) {
            result += models.getDataCount();
        }
        return result;
    }

    @Override
    public void clear() {
        store.clear();
    }

    @Override
    public Map<String, Integer> getObjectsCount() {
        final Map<String, Integer> result = new HashMap<>(store.size());
        for (final String key : store.keySet()) {
            result.put(key, store.get(key).getDataCount());
        }
        return result;
    }

    @Override
    public Set<Identifier> getIdentifiers(final String modelName) {
        final ModelStore<?> mStore = store.get(modelName);
        if (mStore == null) {
            return new HashSet<>();
        }
        return mStore.getIdentifiers();
    }

    @Override
    public <T extends Model> Collection<T> getList(final Criteria<T> criteria) throws UnsupportedModelException, SystemException {
        final ModelStore<?> mStore = store.get(criteria.getModelName());
        if (mStore == null) {
            throw new UnsupportedModelException(ErrorCodes.Cache.ERROR_CACHE_MODEL_UNSUPPORTED, criteria.getModelName() );
        }
        return mStore.getList(criteria);
    }

    @Override
    public <T extends Model>  StatisticResult<T> getIdentifiers(final Criteria<T> criteria) throws UnsupportedModelException, SystemException {
        final ModelStore<?> mStore = store.get(criteria.getModelName());

        if (mStore == null) {
            LOGGER.warn("model" + criteria.getModelName() + "not in list:" + store.keySet());
            throw new UnsupportedModelException(ErrorCodes.Cache.ERROR_CACHE_MODEL_UNSUPPORTED, criteria.getModelName() );
        }
        return mStore.getIdentifiers(criteria);
    }

    @Override
    public int removeOutstandingModels(final int removeCount) {
        final int removed = 0;
        while (removed < removeCount) {
            final Identifier id = getOldestData();
            if (id == null) {
                return removed;
            } else {
                store.get(id.getModelName()).removeFromCache(id);
            }
        }
        return removed;
    }

    private Identifier getOldestData() {

        DateIdentifier result = null;
        for (final Map.Entry<String, ModelStore<?>> entry : store.entrySet()) {
            final DateIdentifier key = entry.getValue().getOldestKey();
            if (key != null) {
                if (result == null) {
                    result = key;
                } else {
                    if (result.getTime() > key.getTime()) {
                        result = key;
                    }
                }
            }
        }
        return result.getIdentifier();
    }
}
