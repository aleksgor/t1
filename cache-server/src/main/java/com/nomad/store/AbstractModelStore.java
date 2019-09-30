package com.nomad.store;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.exception.LogicalException;
import com.nomad.exception.ModelNotExistException;
import com.nomad.exception.SystemException;
import com.nomad.exception.UnsupportedModelException;
import com.nomad.model.Criteria;
import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.model.StoreModel;
import com.nomad.model.criteria.StatisticResult;
import com.nomad.server.DataDefinitionService;
import com.nomad.server.DateIdentifier;
import com.nomad.server.ModelStore;
import com.nomad.server.SaveService;
import com.nomad.server.ServerContext;
import com.nomad.server.ServerContext.ServiceName;
import com.nomad.server.statistic.StatisticStoreMBean;
import com.nomad.utility.ModelUtil;

import com.nomad.utility.DataInvokerPool;
import com.nomad.utility.PooledDataInvoker;
import com.nomad.utility.SimpleServerContext;
import com.nomad.utility.SynchronizedLinkedHashMap;

public abstract class AbstractModelStore<K> implements ModelStore<K> {
    protected static Logger LOGGER = LoggerFactory.getLogger(AbstractModelStore.class);
    private final SynchronizedLinkedHashMap<Identifier, TimeData<K>> store;
    private final boolean readThrough;
    private final boolean writeThrough;
    private DataInvokerPool dataInvokerPool = null;
    private volatile SaveService saveService;
    protected final DataDefinitionService dataDefinition;
    private volatile StatisticStoreMBean statistic;
    private final StoreModel modelStore;
    private final String serverName;
    private final ServerContext context;

    public AbstractModelStore(final boolean readThrough, final boolean writeThrough, final DataInvokerPool dataInvoker, final SaveService saveService, ServerContext context, final StoreModel modelStore) throws SystemException {
        this.readThrough = readThrough;
        this.writeThrough = writeThrough;
        this.dataInvokerPool = dataInvoker;
        this.saveService = saveService;
        this.modelStore = modelStore;
        this.store = new SynchronizedLinkedHashMap<>();

        if (context == null) {
            context = new SimpleServerContext();
        }
        this.context = context;
        if (context.getServerModel() != null) {
            serverName = context.getServerModel().getServerName();
        } else {
            serverName = "noname";
        }

        dataDefinition = context.getDataDefinitionService("");
        statistic = new StatisticStoreMBean();

        statistic.setModelStore(this);
        statistic.setModelName(modelStore == null ? "noName" : modelStore.getModel());
        context.getInformationPublisherService().publicData(statistic, context.getServerModel() == null ? "noName" : context.getServerModel().getServerName(), "Cache", modelStore == null ? "noName" : modelStore.getModel());

    }

    @Override
    public int getDataCount() {
        return store.size();
    }

    @Override
    public Collection<Model> get(final Collection<Identifier> ids) throws SystemException {

        final List<Model> result = new ArrayList<>(ids.size());
        final List<Identifier> notFound = new ArrayList<>(ids.size());
        for (final Identifier id : ids) {
            final TimeData<K> data = store.get(id);
            if (data != null) {
                result.add(getModelFromBytes(data.getData()));
            } else {
                notFound.add(id);
            }
        }
        if (!notFound.isEmpty() && readThrough && dataInvokerPool != null) {
            PooledDataInvoker invoker = null;
            try {
                invoker = dataInvokerPool.getObject();
                final Collection<Model> newModels = invoker.getModel(notFound);
                result.addAll(newModels);
                for (final Model model : newModels) {
                    final K data = getBytesFromModel(model);
                    store.put(model.getIdentifier(), new TimeData<K>(data));
                }
            } catch (final ModelNotExistException e) {
                ;
            } finally {
                if (invoker != null) {
                    invoker.freeObject();
                }
            }
        }

        return result;
    }

    private Collection<Model> putModels(final Collection<Model> models) throws SystemException {
        for (final Model model : models) {
            store.put(model.getIdentifier(), new TimeData<>(getBytesFromModel(model)));
        }
        statistic.setCount(store.size());
        return models;
    }

    @Override
    public Collection<Model> put(final Collection<Model> models, final String sessionId) throws SystemException {
        LOGGER.debug("put server:{}, models:{}", serverName, models);
        putModels(models);

        final List<Model> result = new ArrayList<>(models.size());
        LOGGER.debug("put server:{},writeThrough:{} models:{} ", new Object[] { serverName, writeThrough, models });
        if (writeThrough && dataInvokerPool != null) {
            final List<Model> filteredModels = new ArrayList<>(models);

            Set<Identifier> allowedIds = new HashSet<>(ModelUtil.getIdentifiers(models));
            LOGGER.debug("put server:{},allowedIds:{}  ", serverName, allowedIds);
/*            if (saveService != null && sessionId != null) {
                try {
                    allowedIds = new HashSet<>(saveService.isReadyToSave(new ArrayList<>(allowedIds), sessionId));
                } catch (final Exception ex) {
                    throw new SystemException(ex);
                }
                filteredModels.clear();
                for (final Model model : models) {
                    if (allowedIds.contains(model.getIdentifier())) {
                        filteredModels.add(model);
                    }
                }
            }
*/            PooledDataInvoker invoker = null;
            if (filteredModels.size() > 0) {
                SaveService saveService = getSaveService();
                try {
                    invoker = dataInvokerPool.getObject();
                    Collection<Identifier> filteredIds = ModelUtil.getIdentifiers(filteredModels);
                    Collection<Identifier> filteredIdsForSave = filteredIds;
                    if (saveService != null) {
                        filteredIdsForSave = saveService.isReadyToSave(filteredIds, sessionId);
                    }
                    if (filteredIdsForSave.size() > 0) {
                        final Collection<Model> existModel = invoker.getModel(filteredIdsForSave);
                        if (existModel.size() == filteredModels.size()) {
                            result.addAll(invoker.updateModel(filteredModels));
                            return result;
                        }

                        if (existModel.size() == 0) {
                            result.addAll(invoker.addModel(filteredModels));
                            return result;
                        }

                        final List<Model> newModels = new ArrayList<>();
                        final List<Model> updateModels = new ArrayList<>();

                        final Map<Identifier, Model> mapModels = ModelUtil.convertToMap(existModel);
                        for (final Model model : filteredModels) {
                            final Model foundedModel = mapModels.get(model.getIdentifier());
                            if (foundedModel == null) {
                                newModels.add(model);
                            }
                        }
                        if (!newModels.isEmpty()) {
                            result.addAll(invoker.addModel(newModels));
                        }
                        if (!updateModels.isEmpty()) {
                            result.addAll(invoker.updateModel(updateModels));
                        }
                    }

                } catch (LogicalException e) {
                    ;
                } finally {
                    if (invoker != null) {
                        invoker.freeObject();
                    }
                    if (saveService != null) {
                        try {
                            saveService.cleanSession(Collections.singletonList(sessionId));
                        } catch (LogicalException e) {
                            ;
                        }
                    }
                }
            }
        } else {
            result.addAll(models);
        }
        statistic.setCount(store.size());
        return result;
    }

    @Override
    public void remove(final Collection<? extends Identifier> ids, final String sessionId) throws SystemException {
        LOGGER.debug("remove:{} readThrough:{}", ids, (readThrough && dataInvokerPool != null));
        if (readThrough && dataInvokerPool != null) {
            Collection<Identifier> filteredIds = new ArrayList<>(ids);
/*            if (saveService != null && sessionId != null) {
                try {
                    filteredIds = saveService.isReadyToSave(filteredIds, sessionId);
                } catch (final Exception ex) {
                    throw new SystemException(ex);
                }
            }
*/            PooledDataInvoker invoker = null;
            try {

                if (!filteredIds.isEmpty()) {
                    invoker = dataInvokerPool.getObject();
                    invoker.eraseModel(filteredIds);
                }
            } catch (final ModelNotExistException e) {
                ;
            } finally {
                if (invoker != null) {
                    invoker.freeObject();
                }
            }
        }

        for (final Identifier identifier : ids) {
            store.remove(identifier);
        }
        statistic.setCount(store.size());

    }

    @Override
    public void clean() {
        store.clear();
        statistic.setCount(store.size());

    }

    @Override
    public Collection<Identifier> contains(final Collection<Identifier> ids) throws SystemException {
        LOGGER.debug("server:{}", store);
        final List<Identifier> result = new ArrayList<>(ids.size());
        for (final Identifier identifier : ids) {
            final TimeData<K> value = store.get(identifier);
            if (value != null) {
                value.getData();
                result.add(identifier);
            }
        }
        return result;
    }

    @Override
    public Collection<Model> getFromCache(final Collection<Identifier> ids) throws SystemException {
        final List<Model> result = new ArrayList<>(ids.size());
        for (final Identifier identifier : ids) {
            final TimeData<K> value = store.get(identifier);
            if (value != null) {
                final Model model = getModelFromBytes(value.getData());
                if (model != null) {
                    result.add(model);
                }
            }
        }

        return result;
    }

    @Override
    public Set<Identifier> getIdentifiers() {
        final Set<Identifier> result = new HashSet<>(store.size(), 1);
        for (final Identifier id : store.keySet()) {
            result.add(id);
        }
        return result;
    }

    @Override
    public <T extends Model> Collection<T> getList(final Criteria<T> criteria) throws UnsupportedModelException, SystemException {
        checkCriteria(criteria);

        return dataInvokerPool.getObject().getList(criteria).getResultList();
    }

    protected abstract Model getModelFromBytes(K input) throws SystemException;

    protected abstract K getBytesFromModel(Model input) throws SystemException;

    private void checkCriteria(final Criteria<? extends Model> criteria) {
        if (criteria.getPageSize() > modelStore.getMaxListSize()) {
            LOGGER.warn("pageSize:" + criteria.getPageSize() + " was corrected " + modelStore.getMaxListSize());
            criteria.setPageSize(modelStore.getMaxListSize());
        }
    }

    @Override
    public <T extends Model> StatisticResult<T> getIdentifiers(final Criteria<T> criteria) throws UnsupportedModelException, SystemException {
        PooledDataInvoker invoker = null;
        try {
            checkCriteria(criteria);
            invoker = dataInvokerPool.getObject();
            return invoker.getIds(criteria);
        } finally {
            if (invoker != null) {
                invoker.freeObject();
            }
        }
    }

    public K getQuietly(final Identifier id) {
        final TimeData<K> result = store.getQuietly(id);
        if (result == null) {
            return null;
        }
        return result.getData();

    }

    @Override
    public DateIdentifier getOldestKey() {
        if (!store.isEmpty()) {
            final Identifier id = store.keySet(1).iterator().next();
            final TimeData<K> data = store.getQuietly(id);
            return new DateIdentifierImpl(id, data.getTime());
        }
        return null;
    }

    @Override
    public void removeFromCache(final Identifier identifier) {
        store.remove(identifier);
        statistic.setCount(store.size());

    }

    private SaveService getSaveService() {
        if (context != null) {
            return (SaveService) context.get(ServiceName.SAVE_SERVICE);
        }
        return null;
    }

}
