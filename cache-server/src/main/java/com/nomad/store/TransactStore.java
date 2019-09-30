package com.nomad.store;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.InternalDataStore;
import com.nomad.InternalTransactDataStore;
import com.nomad.exception.BlockException;
import com.nomad.exception.ErrorCodes;
import com.nomad.exception.LogicalException;
import com.nomad.exception.SystemException;
import com.nomad.exception.UnsupportedModelException;
import com.nomad.io.serializer.SerializerFactory;
import com.nomad.message.MessageSenderReceiver;
import com.nomad.message.MessageSenderReceiverImpl;
import com.nomad.model.Criteria;
import com.nomad.model.DataSourceModel;
import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.model.StoreModel;
import com.nomad.model.core.SessionContainer;
import com.nomad.model.criteria.StatisticResult;
import com.nomad.model.update.UpdateItem;
import com.nomad.model.update.UpdateRequest;
import com.nomad.server.BlockService;
import com.nomad.server.DataDefinitionService;
import com.nomad.server.ModelStore;
import com.nomad.server.SaveService;
import com.nomad.server.ServerContext;
import com.nomad.server.BlockService.BlockLevel;
import com.nomad.server.ServerContext.ServiceName;
import com.nomad.server.service.storemodelservice.StoreModelServiceImpl;
import com.nomad.server.transaction.TransactElement;
import com.nomad.server.transaction.TransactElement.Operation;
import com.nomad.server.transaction.TransactInfo;
import com.nomad.server.transaction.TransactionMarker;
import com.nomad.store.transaction.TransactElementImpl;
import com.nomad.store.transaction.TransactElementSerializer;
import com.nomad.util.DataInvokerPoolImpl;
import com.nomad.utility.ModelUtil;
import com.nomad.utility.DataInvokerPool;
import com.nomad.utility.MessageUtil;

public class TransactStore implements InternalTransactDataStore {
    private static Logger LOGGER = LoggerFactory.getLogger(TransactStore.class);

    private volatile InternalDataStore cachedStores;

    private volatile TransactInfo transactInfo;
    private final ServerContext context;
    private final String serverName;
    private final DataDefinitionService dataDefinition;
    private volatile BlockService blockService;


    public TransactStore(final InternalDataStore store, final ServerContext context) throws SystemException  {
        cachedStores = store;
        final StoreModelServiceImpl server = (StoreModelServiceImpl) context.get(ServiceName.STORE_MODEL_SERVICE);
        serverName = server.getServerModel().getServerName();
        this.context = context;
        dataDefinition = context.getDataDefinitionService(null);
        blockService = (BlockService) context.get(ServiceName.BLOCK_SERVICE);
        SerializerFactory.registerSerializer(TransactElementImpl.class.getName(), TransactElementSerializer.class.getName());
        MessageSenderReceiver msr = new MessageSenderReceiverImpl(dataDefinition);
        transactInfo = new TransactInfoImpl(msr);
    }

    @Override
    public Collection<Model> put(final Collection<Model> data, final SessionContainer sessions)
            throws LogicalException, SystemException {
        LOGGER.debug("Put server:{} session:{}  models: {}", new Object[] { serverName, sessions,  data });
        if (sessions != null && !sessions.isEmpty() ) {
            final Collection< Identifier> ids = ModelUtil.getIdentifiers(data);

            final Collection<Identifier> blockIds = blockService.block(ids,sessions, BlockService.BlockLevel.READ_LEVEL);
            if (!blockIds.isEmpty()) {
                throw new BlockException(ErrorCodes.Block.ERROR_CACHE_SOFT_BLOCK,blockIds);
            }

            final Collection<Model> oldModels = cachedStores.get(ids);

            final TransactionMarker marker = transactInfo.getTransactionMarker(sessions.getMainSessionId());

            final Map<Identifier, Model> modelMap = ModelUtil.convertToMap(oldModels);
            for (final Model model : data) {
                final Model oldModel = modelMap.get(model.getIdentifier());
                if (oldModel != null) {
                    final TransactElement transactElement = marker.addOperation(model, Operation.UPDATE_MODEL, sessions.getSessionId());
                    transactElement.setOldModel(oldModel);
                } else {
                    marker.addOperation(model, Operation.ADD_MODEL, sessions.getSessionId());
                }
            }
        } else {
            return cachedStores.put(data, null);
        }

        return data;
    }

    @Override
    public List<Model> get(final Collection<Identifier> ids, SessionContainer sessions) throws LogicalException,  SystemException {
        final Object[] parameters = { serverName, sessions, ids };
        LOGGER.debug("get server:{} sessions:{} mses:{}", parameters);
        // check ReadBlock
        if (blockService.checkBlockLevel(ids, BlockService.BlockLevel.READ_LEVEL, sessions)) {
            throw new BlockException(ErrorCodes.Block.ERROR_CACHE_SOFT_BLOCK,(List<? extends Identifier>) ids);
        }

        return getModelsFromCache((List<Identifier>) ids, sessions);
    }

    private List<Model> getModelsFromCache(final List<Identifier> ids, final SessionContainer sessions) throws LogicalException, SystemException {
        final TransactionMarker marker = transactInfo.getTransactionMarker(sessions.getMainSessionId());
        final Map<Identifier, Model> models = ModelUtil.convertToMap(cachedStores.get(ids));
        if (marker != null) {
            final Map<Identifier, TransactElement> transactElements = marker.getLastTransactVersion(ids);

            for (final Identifier identifier : ids) {
                final TransactElement transactElement = transactElements.get(identifier);
                if (transactElement != null) {
                    switch (transactElement.getOperation()) {
                    case DELETE_MODEL:
                        models.remove(identifier);
                        break;
                    case UPDATE_MODEL:
                    case ADD_MODEL:
                        final Model model = transactElement.getNewModel();
                        models.put(model.getIdentifier(), model);
                        break;
                    case EMPTY_OPERATION:
                        break;
                    }
                }
            }
        }
        return new ArrayList<>(models.values());
    }

    @Override
    public Collection<Identifier> remove(final Collection<Identifier> ids, final SessionContainer sessions)
            throws LogicalException, SystemException {

        LOGGER.debug("Remove in server: {} session:{} ids: {}", new Object[] { serverName, sessions, ids });
        final Collection<? extends Identifier> blockIds = blockService.block(ids, sessions, BlockService.BlockLevel.UPDATE_LEVEL);
        LOGGER.debug("Remove in server : {}  session:{} blocks: {}", new Object[] { serverName, sessions, blockIds });
        if (!blockIds.isEmpty()) {
            throw new BlockException(ErrorCodes.Block.ERROR_CACHE_SOFT_BLOCK, blockIds);
        }

        if (sessions == null ) { // without
            // transactions....
            return ids;
        } else {

            final Collection<Model> models = cachedStores.get((Collection<Identifier>) ids);
            final Map<Identifier, Model> modelMap = ModelUtil.convertToMap(models);

            synchronized (transactInfo) {
                final TransactionMarker marker = transactInfo.getTransactionMarker(sessions.getMainSessionId());
                if (marker != null) {
                    for (final Identifier id : ids) {
                        final Model oldModel = modelMap.get(id);
                        if (oldModel != null) {
                            marker.addOperation(oldModel, Operation.DELETE_MODEL, sessions.getSessionId());
                        }

                    }
                }
            }
        }
        return ids;
    }

    @Override
    public ModelStore<?> getModelStore(final String modelName) {
        return cachedStores.getModelStore(modelName);
    }

    @Override
    public void commit(SessionContainer sessions) throws SystemException {
        LOGGER.debug("Commit server: {}  sessionId: {} ", new Object[] { serverName, sessions });
        if (sessions == null || sessions.getSessions()== null || sessions.getSessions().isEmpty()) {
            return;
        }

        final TransactionMarker marker = transactInfo.getTransactionMarker(sessions.getMainSessionId());
        commitTransactionMarker(marker, sessions.getSessions());
        LOGGER.debug("result commit {} ", sessions.getSessions());

    }

    private void commitTransactionMarker(final TransactionMarker marker, final Collection<String> sessionIds) throws SystemException {
        LOGGER.debug("commitTransactionMarker: {}", marker);
        final List<TransactElement> elementsForRollBack = new ArrayList<>();
        try {
            if (marker != null) {

                final Collection<TransactElement> elements = marker.getLastTransactVersionForSessions(sessionIds);
                for (final TransactElement transactElement : elements) {
                    if (!transactElement.isPhase2()) {
                        transactElement.setPhase2(true);
                        final Identifier id = transactElement.getIdentifier();
                        LOGGER.debug("commit TransactElement! {}", transactElement);
                        switch (transactElement.getOperation()) {
                        case UPDATE_MODEL:
                            try {
                                final Model newModel = transactElement.getNewModel();
                                cachedStores.put(Collections.singletonList(newModel), transactElement.getSessionId());
                                transactElement.setPhase2(true);
                                elementsForRollBack.add(transactElement); // for
                                // rollback

                            } catch (final UnsupportedModelException e) {
                                LOGGER.warn(e.getMessage(), e);
                            }
                            break;
                        case ADD_MODEL:
                            cachedStores.put(Collections.singletonList(transactElement.getNewModel()), transactElement.getSessionId());
                            transactElement.setPhase2(true);
                            elementsForRollBack.add(transactElement); // for
                            // rollback
                            break;
                        case DELETE_MODEL:
                            cachedStores.remove(Collections.singletonList(id), transactElement.getSessionId());
                            transactElement.setPhase2(true);
                            elementsForRollBack.add(transactElement);
                            break;
                        case EMPTY_OPERATION:

                            break;
                        default:
                            break;

                        }
                    }
                }

            }

        } catch (final Throwable e) {
            LOGGER.warn(e.getMessage(), e);
            rollbackPH2TransactionMarker(elementsForRollBack);
        }
    }

    @Override
    public void rollback(final SessionContainer sessions) {
        LOGGER.debug("rollback {} mainSession:{}", new Object[] { serverName, sessions });
        final TransactionMarker marker = transactInfo.getTransactionMarker(sessions.getMainSessionId());
        if (marker == null) {
            return;
        }
        final Collection<TransactElement> elements = marker.getAllTransactVersionForSessions(sessions.getSessions());

        rollbackPH2TransactionMarker(elements);
        marker.removeTransactElements(sessions.getSessions());
        blockService.unblock(sessions);
    }

    @Override
    public void closeSession(final SessionContainer sessions) {
        LOGGER.debug("cleanSession server:{} sessions:{} ", new Object[] { serverName, sessions });
        cleanSession(sessions);
    }

    private void cleanSession(final SessionContainer sessions) {
        blockService.unblock(sessions);
        final TransactionMarker marker = transactInfo.getTransactionMarker(sessions.getMainSessionId());
        if (marker != null) {
            marker.removeTransactElements(sessions.getSessions());
        }

    }

    /**
     * get list all ids from cache
     */
    @Override
    public Collection< Identifier> contains(final Collection< Identifier> ids) throws UnsupportedModelException, SystemException {
        LOGGER.debug("Contains server:{}  ids:{}", new Object[] { serverName, ids });
        final Set<Identifier> result = new HashSet<>(ids.size(), 1);
        result.addAll(cachedStores.contains(ids));
        LOGGER.debug("Contains in store server:{} session:{} ids:{}", serverName, result);
        result.addAll(transactInfo.getContainsIds(ids));
        LOGGER.debug("Contains result server:{} session:{} ids:{}", serverName, result);
        return new ArrayList<>(result);
    }

    @Override
    public Collection<Model> getFromCache(final Collection<Identifier> ids, SessionContainer sessions)
            throws UnsupportedModelException, SystemException {
        LOGGER.debug("getFromCache: {} ,ids:{}, session:{} ", new Object[] { serverName, ids, sessions });
        TransactionMarker marker;
        final Map<Identifier, Model> models = ModelUtil.convertToMap(cachedStores.getFromCache(ids));
        if (sessions.getSessions() != null) {
            marker = transactInfo.getTransactionMarker(sessions.getMainSessionId());
            LOGGER.debug("transactInfo: {} ,{},{}", new Object[] { serverName, transactInfo, sessions });

            if (marker != null) {
                final Map<Identifier, TransactElement> transactElements = marker.getLastTransactVersion(ids);
                for (final Identifier identifier : ids) {
                    final TransactElement transactElement = transactElements.get(identifier);
                    Model model;
                    if (transactElement != null) {
                        switch (transactElement.getOperation()) {
                        case DELETE_MODEL:
                            models.remove(identifier);
                            break;
                        case UPDATE_MODEL:
                            models.put(transactElement.getOldModel().getIdentifier(), transactElement.getOldModel());
                        case ADD_MODEL:
                            model = transactElement.getNewModel();
                            models.put(model.getIdentifier(), model);
                            break;
                        case EMPTY_OPERATION:
                            break;
                        }
                    }
                }
            }
        }

        return new ArrayList<>(models.values());
    }

    @Override
    public long cleanOldData(final int percent) throws SystemException {
        if (percent <= 0 || percent > 100) {
            throw new SystemException("parameter percent must be between 1 and 100");
        }
        final int allData = cachedStores.getDataCount();
        final int needClean = allData * (percent / 100);

        final int cleaned = cachedStores.removeOutstandingModels(needClean);

        LOGGER.info("size before:" + allData + "cs:" + cachedStores.getDataCount() + " isz:" + cachedStores.getDataCount() + " removed:" + cleaned);
        return cleaned;
    }

    @Override
    public Map<String, Integer> getObjectsCount() {

        return cachedStores.getObjectsCount();
    }

    @Override
    public Set<Identifier> getIdentifiers(final String modelName) {

        return cachedStores.getIdentifiers(modelName);
    }

    private void rollbackPH2TransactionMarker(final Collection<TransactElement> transactElements) {
        if (transactElements != null) {
            for (final TransactElement transactElement : transactElements) {
                LOGGER.debug("rollback! {}", transactElement);
                if (transactElement.isPhase2()) {
                    switch (transactElement.getOperation()) {
                    case UPDATE_MODEL:
                        try {
                            cachedStores.put(Collections.singletonList(transactElement.getOldModel()), transactElement.getSessionId());
                            transactElement.setPhase2(false);
                        } catch (final Exception e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                        break;
                    case ADD_MODEL:
                        try {
                            cachedStores.remove(Collections.singletonList(transactElement.getNewModel().getIdentifier()), transactElement.getSessionId());
                            transactElement.setPhase2(false);
                        } catch (final Exception e) {
                            LOGGER.error(e.getMessage(), e);
                        }

                        break;
                    case DELETE_MODEL:
                        try {
                            cachedStores.put(Collections.singletonList(transactElement.getNewModel()), transactElement.getSessionId());
                            transactElement.setPhase2(false);
                        } catch (final Exception e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                        break;
                    default:
                        break;

                    }
                }
            }
        }
    }

    // -------------------------------

    @Override
    public void commitPhase2(final SessionContainer sessions) throws SystemException {
        LOGGER.debug("commitPH2 server:{}, session:{}", serverName, sessions);

        // shift data
        final TransactionMarker marker = transactInfo.getTransactionMarker(sessions.getMainSessionId());
        marker.commitPhase2(sessions.getSessions());
        cleanSession(sessions);

    }

    @Override
    public void registerModel(final StoreModel model, final DataSourceModel dataSource) throws SystemException  {
        LOGGER.info("Register model:{}", model);
        final SaveService saveService = (SaveService) context.get(ServiceName.SAVE_SERVICE);
        DataInvokerPool dataInvoker = null;
        if (dataSource != null) {
            dataInvoker = (DataInvokerPool) context.getDataInvoker(model.getDataSource());
            if (dataInvoker == null) {
                dataInvoker = getDataInvoker(dataSource);
                context.saveDataInvoker(dataSource.getName(), dataInvoker);
            }
            dataInvoker.incrementPoolSize(dataSource.getThreads());
        }
        LOGGER.info("Data invoker model:{}", model);

        cachedStores.registerModel(model, dataInvoker, saveService, context);
    }

    private DataInvokerPool getDataInvoker(final DataSourceModel dataSource) throws SystemException {

        final DataInvokerPoolImpl result = new DataInvokerPoolImpl(dataSource.getThreads(), dataSource.getTimeOut(), dataSource.getClazz(),
                dataSource.getProperties(), context, dataSource.getName());
        return result;
    }

    @Override
    public <T extends Model> StatisticResult<T> getIdentifiers(final Criteria<T> criteria) throws UnsupportedModelException, SystemException {
        return cachedStores.getIdentifiers(criteria);
    }

    @Override
    public Collection< Identifier> block(final Collection<Identifier> ids, SessionContainer sessions, BlockLevel blockLevel)
            throws SystemException {
        return blockService.block(ids, sessions, blockLevel);
    }

    @Override
    public void unblock(final SessionContainer sessions) throws SystemException {
        blockService.unblock(sessions);
    }

    @Override
    public void start() throws SystemException {

    }

    @Override
    public void stop() {
        cachedStores.clear();
        transactInfo.clear();

    }

    @Override
    public void update(Collection<UpdateRequest> updateRequests, Collection<Identifier> ids, SessionContainer sessions) throws UnsupportedModelException, SystemException, BlockException, LogicalException {
        MessageSenderReceiver msr = new MessageSenderReceiverImpl(dataDefinition);
            List<Model> models = get(ids, sessions);
            for (Model t : models) {
                for (UpdateRequest updateRequest :  updateRequests) {
                    updateModel(t, updateRequest, msr,sessions);
                    
                }
            }
    }

    private Model updateModel(Model t, UpdateRequest updateRequest, MessageSenderReceiver msr, SessionContainer sessions) throws LogicalException, SystemException {
        
        final TransactionMarker marker = transactInfo.getTransactionMarker(sessions.getMainSessionId());

        Model result = MessageUtil.clone(t, msr);
        for (UpdateItem updateItem : updateRequest.getUpdateItems()) {
            updateField(result, updateItem);
            final TransactElement transactElement = marker.addOperation(result, Operation.UPDATE_MODEL, sessions.getSessionId());
            transactElement.setOldModel(t);
        }
        return result;
    }

    private void updateField(Model model, UpdateItem updateItem) throws LogicalException {
        String soperation = updateItem.getValue();
        if (soperation == null) {
            soperation = "0";
        }
        double opernad = Double.parseDouble(soperation);

        Object data;
        try {
            Method getter = new PropertyDescriptor(updateItem.getFieldName(), model.getClass()).getReadMethod();
            data = getter.invoke(model);
            if (data == null) {
                data = 0.0;
            }
        } catch (IntrospectionException e) {
            LOGGER.debug(e.getMessage(), e);
            throw new LogicalException(ErrorCodes.Update.ERROR_UPDATE_INVALID_PROPERTY_NAME, model.getModelName(), updateItem.getFieldName());
        } catch (IllegalAccessException e) {
            LOGGER.debug(e.getMessage(), e);
            throw new LogicalException(ErrorCodes.Update.ERROR_UPDATE_INVALID_PROPERTY_ACCESS, model.getModelName(), updateItem.getFieldName());
        } catch (Exception e) {
            LOGGER.debug(e.getMessage(), e);
            throw new LogicalException(ErrorCodes.Update.ERROR_UPDATE_INVALID_PROPRTY, model.getModelName(), updateItem.getFieldName());
        }
        if (data instanceof Number) {
            Number number = (Number) data;
            switch (updateItem.getOperation()) {
            case DECREMENT:
                number = number.doubleValue() - opernad;
                break;
            case DECREMENT_BY:
                number = opernad - number.doubleValue();
                break;
            case DIVIDE:
                if (opernad == 0) {
                    throw new LogicalException(ErrorCodes.Update.ERROR_UPDATE_INVALID_OPERNAD, "" + opernad);
                }
                number = number.doubleValue() / opernad;
                break;
            case DIVIDE_BY:
                if (number.doubleValue() == 0) {
                    throw new LogicalException(ErrorCodes.Update.ERROR_UPDATE_INVALID_OPERNAD, "" + number.doubleValue());
                }
                number = opernad / number.doubleValue();
                break;
            case INCREMENT:
                number = number.doubleValue() + opernad;
                break;
            case MULTIPLY:
                number = number.doubleValue() * opernad;
                break;
            }
            try {
                Method setter = new PropertyDescriptor(updateItem.getFieldName(), model.getClass()).getWriteMethod();
                setter.invoke(model, number);
            } catch (IntrospectionException e) {
                LOGGER.debug(e.getMessage(), e);
                throw new LogicalException(ErrorCodes.Update.ERROR_UPDATE_INVALID_PROPERTY_NAME, model.getModelName(), updateItem.getFieldName());
            } catch (IllegalAccessException e) {
                LOGGER.debug(e.getMessage(), e);
                throw new LogicalException(ErrorCodes.Update.ERROR_UPDATE_INVALID_PROPERTY_ACCESS, model.getModelName(), updateItem.getFieldName());
            } catch (Exception e) {
                LOGGER.debug(e.getMessage(), e);
                throw new LogicalException(ErrorCodes.Update.ERROR_UPDATE_INVALID_PROPRTY, model.getModelName(), updateItem.getFieldName());
            }
        }

    }
}
