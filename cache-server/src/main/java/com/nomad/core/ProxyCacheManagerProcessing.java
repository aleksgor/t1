package com.nomad.core;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.nomad.cache.commonclientserver.BodyImpl;
import com.nomad.cache.commonclientserver.RawMessageImpl;
import com.nomad.cache.commonclientserver.RequestImpl;
import com.nomad.cache.commonclientserver.ResultImpl;
import com.nomad.exception.ErrorCodes;
import com.nomad.exception.LogicalException;
import com.nomad.exception.ModelNotExistException;
import com.nomad.exception.SystemException;
import com.nomad.message.Body;
import com.nomad.message.MessageHeader;
import com.nomad.message.MessageSenderReceiver;
import com.nomad.message.MessageSenderReceiverImpl;
import com.nomad.message.OperationStatus;
import com.nomad.message.RawMessage;
import com.nomad.message.Result;
import com.nomad.model.BaseCommand;
import com.nomad.model.Condition;
import com.nomad.model.Criteria;
import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.model.ModelDescription;
import com.nomad.model.Relation;
import com.nomad.model.ServiceCommand;
import com.nomad.model.criteria.StatisticResult;
import com.nomad.model.criteria.StatisticResultImpl;
import com.nomad.model.idgenerator.IdGeneratorService;
import com.nomad.model.update.UpdateRequest;
import com.nomad.server.ExecutorServiceProvider;
import com.nomad.server.ServerContext;
import com.nomad.server.ServerContext.ServiceName;
import com.nomad.server.SessionResult;
import com.nomad.server.SessionState;
import com.nomad.server.service.childserver.MessageAnswer;
import com.nomad.server.service.childserver.MessageRequest;
import com.nomad.server.service.childserver.StoreConnectionPool;
import com.nomad.utility.ModelUtil;
import com.nomad.util.UniSorting;
import com.nomad.utility.MessageUtil;
import com.nomad.utility.SessionUtil;
import com.nomad.utility.SimpleCriteria;

public class ProxyCacheManagerProcessing extends CommonProxyProcessing {
    private final ProxyProcessing proxyProcessing;

    public ProxyCacheManagerProcessing(final ServerContext context, final ExecutorServiceProvider executorServiceProvider, ProxyProcessing proxyProcessing) throws SystemException {

        super(context, executorServiceProvider);
        this.proxyProcessing = proxyProcessing;
    }

    Collection<Model> getModelList(final MessageHeader header, final Collection<Identifier> identifiers, final MessageSenderReceiver msr) throws SystemException {
        if (identifiers == null || identifiers.size() == 0) {
            return Collections.emptyList();
        }
        final Collection<StoreConnectionPool> servers = childrenServerService.getCacheConnectionsPools(header.getModelName());
        if (servers == null) {
            LOGGER.error("Model: {} does not supported", header);
            return null;
        }

        final Collection<MessageRequest> requests = new ArrayList<>(servers.size());

        final MessageHeader newHeader = MessageUtil.getHeaderCopy(header);
        SessionUtil.fillSessions(header, newHeader);
        newHeader.setCommand(ServiceCommand.GET_FROM_CACHE.toString());

        final Body body = new BodyImpl(null, identifiers, null);
        final byte[] message = msr.getByteFromBody(body);
        for (final StoreConnectionPool srv : servers) {
            requests.add(new MessageRequest(new RawMessageImpl(newHeader, message), srv));
        }
        final Set<Model> answers = new HashSet<>(identifiers.size());
        List<Future<MessageAnswer>> resultList = Collections.emptyList();
        try {
            resultList = executorServiceProvider.getExecutorService().invokeAll(requests, requestTimeout, TimeUnit.SECONDS);

            for (final Future<MessageAnswer> fmsg : resultList) {
                try {
                    final MessageAnswer messageAnswer = fmsg.get();
                    if (OperationStatus.OK.equals(messageAnswer.getMessage().getResult().getOperationStatus())) {
                        final Body answerBody = msr.getBodyFromByte(messageAnswer.getMessage().getMessage());
                        if (answerBody.getResponse().getResultList() != null) {
                            answers.addAll(answerBody.getResponse().getResultList());
                        }
                    }
                } catch (final ExecutionException e) {
                    LOGGER.error("error exec getModelList:" + servers + ":" + fmsg.get().getPool().getPoolId() + ":" + e);
                } catch (final Exception e) {
                    LOGGER.error("error exec getModelList:" + servers + ":" + fmsg.get().getPool().getPoolId() + ":" + e, e);
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new SystemException(e);
        }
        // check all data for read through
        final Set<Identifier> lostIds = new HashSet<>(identifiers);
        for (final Model model : answers) {
            lostIds.remove(model.getIdentifier());
        }
        if (lostIds.size() > 0) {
            final Map<String, List<Identifier>> serversNames = getSimpleNormalMapForCacheManager(header, lostIds);
            newHeader.setCommand(BaseCommand.GET.toString());
            requests.clear();
            for (final Entry<String, List<Identifier>> element : serversNames.entrySet()) {
                final StoreConnectionPool pool = childrenServerService.getStoreConnectionPool(element.getKey());
                final byte[] getMessage = msr.getByteFromBody(new BodyImpl(null, element.getValue(), null));
                requests.add(new MessageRequest(new RawMessageImpl(newHeader, getMessage), pool));
            }
            try {
                resultList = executorServiceProvider.getExecutorService().invokeAll(requests, requestTimeout, TimeUnit.SECONDS);

                for (final Future<MessageAnswer> fmsg : resultList) {
                    try {
                        final MessageAnswer messageAnswer = fmsg.get();
                        if (OperationStatus.OK.equals(messageAnswer.getMessage().getResult().getOperationStatus())) {
                            final MessageSenderReceiver msr1 = new MessageSenderReceiverImpl(messageAnswer.getMessage().getHeader().getVersion(), dataDefinition);
                            final Body answerBody = msr1.getBodyFromByte(messageAnswer.getMessage().getMessage());
                            if (answerBody.getResponse().getResultList() != null) {
                                answers.addAll(answerBody.getResponse().getResultList());
                            }
                        }
                    } catch (final ExecutionException e) {
                        LOGGER.error("error exec getModelList:" + servers + ":" + fmsg.get().getPool().getPoolId() + ":" + e);
                    } catch (final Exception e) {
                        LOGGER.error("error exec getModelList:" + servers + ":" + fmsg.get().getPool().getPoolId() + ":" + e, e);
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new SystemException(e);
            }
        }
        return new ArrayList<>(answers);
    }

    /**
     * Input message may be ids or models
     * 
     * @throws LogicalException
     */
    RawMessage delete(final MessageHeader header, final byte[] message, final MessageSenderReceiver msr) throws SystemException, LogicalException {

        final Body body = msr.getBodyFromByte(message);
        if (body.getRequest().getIdentifiers() == null && body.getRequest().getModels() == null && body.getRequest().getCriteria() != null) {
            return deleteByCriteria(header, message, msr);
        }
        final Collection<StoreConnectionPool> servers = childrenServerService.getCacheConnectionsPools(header.getModelName());
        LOGGER.info("delete from servers:{}", servers);
        if (servers == null) {
            LOGGER.error("Model: {} does not supported", header);
            return new RawMessageImpl(header, message, new ResultImpl(OperationStatus.UNSUPPORTED_MODEL_NAME, "model does not supported" + header));
        }

        final Map<String, StoreConnectionPool> mapService = new HashMap<>(servers.size(), 1);
        for (final StoreConnectionPool storeConnectionPool : servers) {
            mapService.put(storeConnectionPool.getPoolId(), storeConnectionPool);
        }

        List<Identifier> ids = (List<Identifier>) body.getRequest().getIdentifiers();

        if (body.getRequest().getIdentifiers() == null || body.getRequest().getIdentifiers().size() == 0) {
            if (body.getRequest().getModels() != null && body.getRequest().getModels().size() > 0) {
                if (ids == null) {
                    ids = new ArrayList<>();
                }
                ids.addAll(ModelUtil.getIdentifiers(body.getRequest().getModels()));
                body.getRequest().setIdentifiers(ids);
            }
        }

        if (body.getRequest().getIdentifiers().size() == 0) {
            LOGGER.warn("nothink to do header: {} body:{}", header, body);
            return new RawMessageImpl(header, msr.getByteFromBody(body), new ResultImpl(OperationStatus.OK));
        }

        SessionState state = null;
        if (sessionService == null) {
            LOGGER.error("server:" + serverName + " sessionserver must be defined!");
            return new RawMessageImpl(header, message, new ResultImpl(OperationStatus.ERROR));
        }
        if (header.getSessionId() == null) {
            state = sessionService.startNewSession(null, header.getUserName(), header.getPassword());
        } else {
            state = sessionService.startChildSession(header.getSessionId(), null);
        }
        try {
            final MessageHeader deleteFromCacheHeader = MessageUtil.getHeaderCopy(header);
            SessionUtil.fillSessions(deleteFromCacheHeader, state);
            deleteFromCacheHeader.setCommand(ServiceCommand.DELETE_FROM_CACHE.name());

            final List<MessageRequest> requests = new ArrayList<>(servers.size());
            final byte[] correctedBody = msr.getByteFromBody(body);
            for (final StoreConnectionPool srv : servers) {
                requests.add(new MessageRequest(new RawMessageImpl(deleteFromCacheHeader, correctedBody), srv));
            }

            final List<Future<MessageAnswer>> resultList = executorServiceProvider.getExecutorService().invokeAll(requests, requestTimeout, TimeUnit.SECONDS);

            final Set<Identifier> resultIds = new HashSet<>(body.getRequest().getIdentifiers().size());

            Result res = new ResultImpl(OperationStatus.OK);
            for (final Future<MessageAnswer> fmsg : resultList) {
                try {
                    final MessageAnswer messageAnswer = fmsg.get();
                    if (OperationStatus.OK.equals(messageAnswer.getMessage().getResult().getOperationStatus())) {
                        final MessageSenderReceiver msr1 = new MessageSenderReceiverImpl(messageAnswer.getMessage().getHeader().getVersion(), dataDefinition);
                        final Body answerBody = msr1.getBodyFromByte(messageAnswer.getMessage().getMessage());
                        resultIds.addAll(answerBody.getResponse().getIdentifiers());
                    } else {
                        res = messageAnswer.getMessage().getResult();
                    }
                } catch (final ExecutionException e) {
                    LOGGER.error("error exec delete:" + servers + ":" + fmsg.get().getPool().getPoolId() + ":" + e);
                } catch (final Exception e) {
                    LOGGER.error("error exec delete:" + servers + ":" + fmsg.get().getPool().getPoolId() + ":" + e, e);
                }
            }
            // commit!!!
            if (header.getSessionId() == null) {
                if (OperationStatus.OK.equals(res.getOperationStatus())) {
                    OperationStatus status = commitPhase1AndPhase2(deleteFromCacheHeader, msr, message);
                    sessionService.removeSession(state.getSessionId());
                    StatisticResult<Model> result = new StatisticResultImpl<>();
                    result.setIdentifiers(ids);
                    return new RawMessageImpl(header, msr.getByteFromBody(new BodyImpl(result)), new ResultImpl(status));
                } else {
                    sessionService.rollback(state.getSessionId());
                    sessionService.removeSession(state.getSessionId());
                    return new RawMessageImpl(header, message, new ResultImpl(res.getOperationStatus()));

                }
            } else {
                if (!OperationStatus.OK.equals(res.getOperationStatus())) {
                    sessionService.rollback(state.getSessionId());
                    return new RawMessageImpl(header, message, new ResultImpl(res.getOperationStatus()));
                }
            }
            StatisticResult<Model> result = new StatisticResultImpl<>();
            result.setIdentifiers(ids);
            return new RawMessageImpl(header, msr.getByteFromBody(new BodyImpl(result)), new ResultImpl(OperationStatus.OK));
        } catch (InterruptedException | ExecutionException e) {
            throw new SystemException(e);
        }
    }

    /**
     * this server know about Model
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    RawMessage put(final MessageHeader header, final byte[] message, final MessageSenderReceiver msr) throws LogicalException, SystemException {
        final Collection<StoreConnectionPool> servers = childrenServerService.getCacheConnectionsPools(header.getModelName());

        if (servers == null) {
            LOGGER.error("Model: {} does not supported", header);
            return new RawMessageImpl(header, message, new ResultImpl(OperationStatus.UNSUPPORTED_MODEL_NAME, "model does not supported :" + header));
        }

        final Map<String, StoreConnectionPool> mapService = new HashMap<>(servers.size(), 1);
        for (final StoreConnectionPool storeConnectionPool : servers) {
            mapService.put(storeConnectionPool.getPoolId(), storeConnectionPool);
        }

        final Body body = msr.getBodyFromByte(message);
        if (body.getRequest().getModels() == null || body.getRequest().getModels().size() == 0) {
            LOGGER.error("nothink to do header: {} body:{}", header, body);
            return new RawMessageImpl(header, msr.getByteFromBody(body), new ResultImpl(OperationStatus.OK));
        }

        final Collection<Model> models = body.getRequest().getModels();
        List<Model> modelWithoutIdentifier = new ArrayList<>();
        for (Model model : models) {
            if (model.getIdentifier() == null) {
                modelWithoutIdentifier.add(model);
            }
        }
        if (modelWithoutIdentifier.size() > 0) {
            IdGeneratorService idGeneratorService = (IdGeneratorService) context.get(ServiceName.ID_GENERATOR_SERVICE);
            List<Identifier> ids = idGeneratorService.nextIdentifier(modelWithoutIdentifier.get(0).getModelName(), modelWithoutIdentifier.size());
            for (int i = 0; i < modelWithoutIdentifier.size(); i++) {
                modelWithoutIdentifier.get(i).setIdentifier(ids.get(i));
            }
        }
        // check ID
        final Map<Identifier, Model> idModel = ModelUtil.convertToMap(models);

        MessageHeader headerWithSession = checkSession(header);
        final Map<String, List<Identifier>> normalRequests = getNormalMapForCacheManager(headerWithSession, ModelUtil.getIdentifiers(models), msr);

        final MessageHeader newHeader = MessageUtil.getHeaderCopy(headerWithSession);
        newHeader.setCommand(ServiceCommand.PUT_INTO_CACHE.toString());

        final List<MessageRequest> requests = new ArrayList<>(servers.size());
        for (final StoreConnectionPool srv : servers) {
            List<Identifier> idForInsert = normalRequests.get(srv.getPoolId());
            if (idForInsert == null) {
                idForInsert = new ArrayList<>();
            }
            final List<Model> modelsForRequest = new ArrayList<>();
            for (final Identifier identifier : idForInsert) {
                modelsForRequest.add(idModel.get(identifier));
            }

            if (!modelsForRequest.isEmpty()) {
                final Body newBody = new BodyImpl(modelsForRequest, null, null);
                requests.add(new MessageRequest(new RawMessageImpl(newHeader, msr.getByteFromBody(newBody)), srv));
            }
        }

        List<Future<MessageAnswer>> resultList;
        try {
            resultList = executorServiceProvider.getExecutorService().invokeAll(requests, requestTimeout, TimeUnit.SECONDS);
        } catch (InterruptedException e1) {
            throw new SystemException(e1);
        }

        final Set<Model> resultModel = new HashSet<>(models.size());

        Result res = new ResultImpl(OperationStatus.OK);
        for (final Future<MessageAnswer> fmsg : resultList) {
            try {
                final MessageAnswer messageAnswer = fmsg.get();
                if (OperationStatus.OK.equals(messageAnswer.getMessage().getResult().getOperationStatus())) {
                    final MessageSenderReceiver msr1 = new MessageSenderReceiverImpl(messageAnswer.getMessage().getHeader().getVersion(), dataDefinition);
                    final Body answerBody = msr1.getBodyFromByte(messageAnswer.getMessage().getMessage());
                    resultModel.addAll(answerBody.getResponse().getResultList());

                } else {
                    res = messageAnswer.getMessage().getResult();
                }
            } catch (final Exception e) {
                throw new SystemException(e);
            }
        }
        // commit!!!
        final Body answerBody = new BodyImpl(new StatisticResultImpl(new ArrayList(resultModel)));
        if (header.getSessionId() == null) {

            if (OperationStatus.OK.equals(res.getOperationStatus())) {
                OperationStatus commitStatus = commitPhase1AndPhase2(newHeader, msr, MessageUtil.getEmptyBody());
                if (OperationStatus.OK.equals(commitStatus)) {
                    sessionService.removeSession(headerWithSession.getSessionId());
                    return new RawMessageImpl(header, msr.getByteFromBody(answerBody), new ResultImpl(OperationStatus.OK));
                } else {
                    return new RawMessageImpl(header, msr.getByteFromBody(answerBody), new ResultImpl(commitStatus));
                }
            } else {
                sessionService.rollback(headerWithSession.getSessionId());
                sessionService.removeSession(headerWithSession.getSessionId());
                return new RawMessageImpl(header, message, new ResultImpl(res.getOperationStatus()));
            }

        } else {
            /*
             * if (!OperationStatus.OK.equals(res.getOperationStatus())) {
             * sessionService.rollback(sessionState.getSessionId());
             * sessionService.removeSession(sessionState.getSessionId()); return
             * new RawMessageImpl(header,message,new
             * ResultImpl(res.getOperationStatus())); }
             */
        }
        return new RawMessageImpl(header, msr.getByteFromBody(answerBody), res);
    }

    public RawMessage inCache(final MessageHeader header, final byte[] message, final MessageSenderReceiver msr) {
        try {
            return sendBroadcastMessageForEachServerAndSumResults(header, message, msr, true);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            return new RawMessageImpl(header, message, new ResultImpl(OperationStatus.ERROR, "system error :" + header));

        }
    }

    // cache manager
    public RawMessage commitPhase1(final MessageHeader header, final byte[] message, final MessageSenderReceiver msr) throws LogicalException, SystemException {
        try {
            MessageHeader commitHeader = MessageUtil.getHeaderCopy(header);
            commitHeader.setCommand(ServiceCommand.COMMIT_PHASE1.name());
            return sendBroadcastMessageForEachServerAndSumResults(commitHeader, message, msr, true);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            return new RawMessageImpl(header, message, new ResultImpl(OperationStatus.ERROR, "system error :" + header));

        }
    }

    // cache manager
    public RawMessage commitPhase2(final MessageHeader header, final byte[] message, final MessageSenderReceiver msr) throws LogicalException, SystemException {
        try {
            MessageHeader commitHeader = MessageUtil.getHeaderCopy(header);
            commitHeader.setCommand(ServiceCommand.COMMIT_PHASE2.name());
            final RawMessage result = sendBroadcastMessageForEachServerAndSumResults(commitHeader, message, msr, true);
            return result;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            return new RawMessageImpl(header, message, new ResultImpl(OperationStatus.ERROR, "system error :" + header));
        }
    }

    private Collection<Identifier> getIdentifierByCriteria(final Criteria<? extends Model> criteria, MessageHeader header, final MessageSenderReceiver msr) throws SystemException {
        final Collection<StoreConnectionPool> servers = childrenServerService.getUniqueStoreCacheConnectionPools(header.getModelName());
        if (servers == null || servers.size() == 0) {
            LOGGER.error("Model: {} does not supported", header.getModelName());
            return null;
        }

        final MessageHeader newHeader = MessageUtil.getHeaderCopy(header);
        newHeader.setCommand(ServiceCommand.GET_IDS_BY_CRITERIA.toString());

        List<MessageRequest> requests = new ArrayList<MessageRequest>(servers.size());

        byte[] message = msr.getByteFromBody(new BodyImpl(new RequestImpl(null, null, criteria)));
        for (StoreConnectionPool storeConnectionPool : servers) {
            requests.add(new MessageRequest(new RawMessageImpl(newHeader, message), storeConnectionPool));
        }

        Collection<Identifier> resultIdentifiers = new HashSet<>();
        try {
            List<Future<MessageAnswer>> resultList = executorServiceProvider.getExecutorService().invokeAll(requests, requestTimeout, TimeUnit.SECONDS);

            for (final Future<MessageAnswer> fmsg : resultList) {
                if (OperationStatus.OK.equals(fmsg.get().getMessage().getResult().getOperationStatus())) {
                    final byte[] messageWithIds = fmsg.get().getMessage().getMessage();
                    final Body bodyWithIds = msr.getBodyFromByte(messageWithIds);
                    final StatisticResult<? extends Model> result = bodyWithIds.getResponse();
                    resultIdentifiers.addAll(result.getIdentifiers());
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new SystemException(e);
        }
        return resultIdentifiers;
    }

    @SuppressWarnings("unchecked")
    protected StatisticResult<Model> getByCriteria(final MessageHeader header, final byte[] message, final MessageSenderReceiver msr) throws SystemException, LogicalException {
        final Body body = msr.getBodyFromByte(message);
        Criteria<? extends Model> criteria = body.getRequest().getCriteria();
        final Collection<StoreConnectionPool> servers = childrenServerService.getUniqueStoreCacheConnectionPools(header.getModelName());
        if (servers == null || servers.size() == 0) {
            LOGGER.error("Model: {} does not supported", header);
            return null;
        }

        List<MessageRequest> requests = new ArrayList<MessageRequest>(servers.size());

        for (StoreConnectionPool storeConnectionPool : servers) {
            requests.add(new MessageRequest(new RawMessageImpl(header, message), storeConnectionPool));
        }
        StatisticResult<Model> statisticResult = null;
        try {
            List<Future<MessageAnswer>> resultList = executorServiceProvider.getExecutorService().invokeAll(requests, requestTimeout, TimeUnit.SECONDS);
            for (final Future<MessageAnswer> fmsg : resultList) {
                if (OperationStatus.OK.equals(fmsg.get().getMessage().getResult().getOperationStatus())) {
                    final byte[] messageWithIds = fmsg.get().getMessage().getMessage();
                    final Body bodyWithIds = msr.getBodyFromByte(messageWithIds);
                    final StatisticResult<Model> result = (StatisticResult<Model>) bodyWithIds.getResponse();
                    statisticResult = addStatistic(statisticResult, result);
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new SystemException(e);
        }
        final MessageHeader newHeader = MessageUtil.getHeaderCopy(header, ServiceCommand.GET_FROM_CACHE.toString());

        // relation
        List<Model> models = new ArrayList<>();
        if (statisticResult != null) {
            models = new ArrayList<>(statisticResult.getResultList());
            models.addAll(getModelList(newHeader, statisticResult.getIdentifiers(), msr));
            statisticResult.setIdentifiers(null);
        }
        final Set<String> relations = criteria.getRelationsLoad();
        if (relations != null && relations.size() > 0) {
            fillModels(models, relations, header, msr);
        }

        sort(criteria, models);
        // paging
        if (criteria.getPageSize() > 0 && criteria.getPageSize() < models.size()) {
            models = models.subList(0, criteria.getPageSize());
        }
        if (statisticResult != null) {
            statisticResult.setResultList(models);
        }
        return statisticResult;

    }

    private void fillModels(final Collection<Model> models, final Collection<String> relations, final MessageHeader header, final MessageSenderReceiver msr) throws SystemException, LogicalException {
        if (models != null && !models.isEmpty()) {
            Model model = models.iterator().next();
            for (final String relation : relations) {
                ModelDescription description = dataDefinition.getModelDescription(model.getModelName());
                if (description == null) {
                    throw new ModelNotExistException(ErrorCodes.Model.MODEL_NOT_SUPPORTED, model.getModelName());
                }
                Relation currentRelation = description.getRelationByName(relation);
                switch (currentRelation.getJoin()) {
                case INNER:

                    break;
                case LEFT_OUTER:
                    fillLeftOuterRelations(models, description, currentRelation, header, msr);
                    break;
                case COLLECTION:
                    fillCollectionRelations(models, description, currentRelation, header, msr);
                    break;
                case FULL_OUTER:
                    break;
                case RIGHT_OUTER:
                    break;
                default:
                    break;

                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void fillCollectionRelations(Collection<Model> models, ModelDescription description, Relation currentRelation, MessageHeader header, MessageSenderReceiver msr) throws SystemException, LogicalException {
        ModelDescription childDescription = dataDefinition.getModelDescription(currentRelation.getChildrenModel());
        ModelDescription parentDescription = dataDefinition.getModelDescription(currentRelation.getParentModel());
        List<Condition> conditions = currentRelation.getConditions();
        if (childDescription == null) {
            throw new ModelNotExistException(ErrorCodes.Model.MODEL_NOT_SUPPORTED, currentRelation.getChildrenModel(), currentRelation.getName(), parentDescription.getModelName());
        }
        Class<?> classChildModel = getClassIdByName(childDescription.getClazz());
        Class<?> classModel = getClassIdByName(parentDescription.getClazz());
        int criteriaPage = 10;

        try {
            SimpleCriteria criteria = new SimpleCriteria();
            criteria.setModelName(childDescription.getModelName());
            if (conditions.size() == 1) {
                Condition cr = conditions.iterator().next();

                // criteria.addCriterion(cr.getChildFieldName(),
                // com.nomad.model.Criteria.Condition.IN, Collections.emptyList());

                Method getMethodGetFromParent = new PropertyDescriptor(cr.getParentFieldName(), classModel).getReadMethod();
                Method getMethodGetFromChild = new PropertyDescriptor(cr.getParentFieldName(), classChildModel).getReadMethod();
                Method getMethodSetResultToParent = new PropertyDescriptor(currentRelation.getFieldName(), classModel).getWriteMethod();
                Method getMethodGetResultToParent = new PropertyDescriptor(currentRelation.getFieldName(), classModel).getReadMethod();

                List<Object> parameters = new ArrayList<>(criteriaPage);
                MessageHeader headerGet = MessageUtil.getHeaderCopy(header, BaseCommand.GET_LIST_ID_BY_CRITERIA.toString());
                headerGet.setModelName(childDescription.getModelName());
                Collection<Model> page = new ArrayList<>();
                for (Model model : models) {
                    parameters.add(getMethodGetFromParent.invoke(model));
                    page.add(model);
                    if (page.size() >= criteriaPage) {
                        criteria.cleanCriterion();
                        criteria.addCriterion(cr.getChildFieldName(), com.nomad.model.Criteria.Condition.IN, parameters);
                        byte[] message = msr.getByteFromBody(new BodyImpl(null, null, criteria));
                        RawMessage msg = proxyProcessing.getModelsByCriteria(header, message, msr);
                        if (OperationStatus.OK.equals(msg.getResult().getOperationStatus())) {
                            Body body = msr.getBodyFromByte(msg.getMessage());
                            Collection<? extends Model> childModel = body.getResponse().getResultList();
                            Map<Object, List<Model>> normalizedChild = new HashMap<>();
                            for (Model model2 : childModel) {
                                Object key = getMethodGetFromChild.invoke(model2);
                                List<Model> list = normalizedChild.get(key);
                                if (list == null) {
                                    list = new ArrayList<>();
                                    normalizedChild.put(key, list);
                                }
                                list.add(model2);
                            }

                            for (Model model2 : childModel) {
                                Object key = getMethodGetFromParent.invoke(model2);
                                if (key != null) {
                                    List<Model> list = normalizedChild.get(key);
                                    if (getMethodSetResultToParent != null) {
                                        getMethodSetResultToParent.invoke(model2, list);
                                    } else if (getMethodGetResultToParent != null) {
                                        List<Object> getResult = (List<Object>) getMethodGetResultToParent.invoke(model2);
                                        getResult.addAll(list);
                                    } else {
                                        LOGGER.warn("no getter or setter for" + currentRelation.getFieldName() + " model:" + parentDescription.getModelName());
                                    }

                                }
                            }

                        } else {
                            throw new LogicalException();
                        }
                        parameters.clear();
                    }
                }
            } else {
                // TODO
            }

        } catch (IntrospectionException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            throw new SystemException(e);
        }
    }

    private Class<?> getClassIdByName(String className) throws SystemException {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new SystemException(e.getMessage());
        }
    }

    private void fillLeftOuterRelations(final Collection<Model> models, ModelDescription description, Relation currentRelation, final MessageHeader header, final MessageSenderReceiver msr) throws SystemException, LogicalException {
        Map<Identifier, List<Model>> result = new HashMap<>();
        ModelDescription parentDescription = dataDefinition.getModelDescription(currentRelation.getParentModel());
        ModelDescription childDescription = dataDefinition.getModelDescription(currentRelation.getChildrenModel());
        List<Condition> conditions = currentRelation.getConditions();
        if (childDescription == null) {
            throw new ModelNotExistException(ErrorCodes.Model.MODEL_NOT_SUPPORTED, currentRelation.getChildrenModel(), currentRelation.getName(), parentDescription.getModelName());
        }
        Class<?> classChild = getClassIdByName(childDescription.getClazz());
        Class<?> classModel = getClassIdByName(parentDescription.getClazz());
        try {
            List<Method[]> idMetodsSet = new ArrayList<>(conditions.size());
            for (Condition condition : conditions) {
                Method[] methods = new Method[2];
                methods[0] = new PropertyDescriptor(condition.getChildFieldName(), classChild).getWriteMethod();
                methods[1] = new PropertyDescriptor(condition.getParentFieldName(), classModel).getReadMethod();
                idMetodsSet.add(methods);
            }

            for (Model model : models) {
                Model childModel = (Model) classChild.newInstance();
                for (Method[] method : idMetodsSet) {
                    Object data = method[1].invoke(model);
                    method[0].invoke(childModel, data);
                }
                List<Model> modelsForUpdate = result.get(childModel.getIdentifier());
                if (modelsForUpdate == null) {
                    modelsForUpdate = new ArrayList<>();
                    result.put(childModel.getIdentifier(), modelsForUpdate);
                }
                modelsForUpdate.add(model);
            }

            Collection<Identifier> requestIds = result.keySet();
            Method methodForSet = new PropertyDescriptor(currentRelation.getFieldName(), classModel).getWriteMethod();
            MessageHeader childHeader = MessageUtil.getHeaderCopy(header, BaseCommand.GET.toString());
            childHeader.setModelName(currentRelation.getChildrenModel());
            Collection<Model> resultModels = proxyProcessing.get(childHeader, requestIds, msr);
            for (Model model : resultModels) {
                List<Model> listModels = result.get(model.getIdentifier());
                for (Model model2 : listModels) {
                    methodForSet.invoke(model2, model);
                }
            }
        } catch (IntrospectionException | IllegalArgumentException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new SystemException(e);
        }

    }

    public void sortTest(final Criteria<? extends Model> criteria, final List<? extends Model> input) {
        sort(criteria, input);
    }

    private void sort(final Criteria<? extends Model> criteria, final List<? extends Model> input) {
        final UniSorting sorter = new UniSorting(criteria.getOrder());
        Collections.sort(input, sorter);
    }

    protected RawMessage deleteByCriteria(final MessageHeader header, final byte[] message, final MessageSenderReceiver msr) throws SystemException, LogicalException {

        final Collection<StoreConnectionPool> servers = childrenServerService.getCacheConnectionsPools(header.getModelName());
        if (servers == null || servers.size() == 0) {
            LOGGER.error("Model: {} does not supported", header);
            return null;
        }

        final MessageHeader deleteHeader = MessageUtil.getHeaderCopy(header);

        SessionState sessionState = null;
        if (header.getSessionId() == null) {
            sessionState = sessionService.startNewSession(null, header.getUserName(), header.getPassword());
            SessionUtil.fillSessions(deleteHeader, sessionState);
        } else {
            sessionState = sessionService.startChildSession(header.getSessionId(), null);
            SessionUtil.fillSessions(deleteHeader, sessionState);
        }
        try {
            deleteHeader.setCommand(ServiceCommand.GET_IDS_BY_CRITERIA.name());
            final RawMessage getMessage = new RawMessageImpl(deleteHeader, message);

            final List<MessageRequest> requests = new ArrayList<>(servers.size());

            for (final StoreConnectionPool storeConnectionPool : servers) {
                requests.add(new MessageRequest(getMessage, storeConnectionPool));
            }
            List<Future<MessageAnswer>> resultList = executorServiceProvider.getExecutorService().invokeAll(requests, requestTimeout, TimeUnit.SECONDS);
            final Set<Identifier> idsForDelete = new HashSet<>();

            for (final Future<MessageAnswer> result : resultList) {
                final MessageAnswer answer = result.get();
                if (OperationStatus.OK.equals(answer.getMessage().getResult().getOperationStatus())) {
                    final Body answerBody = msr.getBodyFromByte(answer.getMessage().getMessage());
                    idsForDelete.addAll(answerBody.getResponse().getIdentifiers());
                } else {
                    sessionService.rollback(sessionState.getSessionId());
                    if (header.getSessionId() == null) {
                        sessionService.removeSession(sessionState.getSessionId());
                    }
                    return new RawMessageImpl(header, message, answer.getMessage().getResult());
                }
            }
            // delete

            SessionUtil.fillSessions(deleteHeader, sessionState);
            deleteHeader.setCommand(ServiceCommand.DELETE_FROM_CACHE.name());

            Body body = new BodyImpl(null, new ArrayList<>(idsForDelete), null);
            final RawMessage deleteMessage = new RawMessageImpl(deleteHeader, msr.getByteFromBody(body));

            requests.clear();
            for (final StoreConnectionPool storeConnectionPool : servers) {
                requests.add(new MessageRequest(deleteMessage, storeConnectionPool));
            }
            idsForDelete.clear();
            resultList = executorServiceProvider.getExecutorService().invokeAll(requests, requestTimeout, TimeUnit.SECONDS);

            for (final Future<MessageAnswer> result : resultList) {
                final MessageAnswer answer = result.get();
                if (OperationStatus.OK.equals(answer.getMessage().getResult().getOperationStatus())) {
                    final Body answerBody = msr.getBodyFromByte(answer.getMessage().getMessage());
                    idsForDelete.addAll(answerBody.getResponse().getIdentifiers());
                } else {
                    sessionService.rollback(sessionState.getSessionId());
                    if (header.getSessionId() == null) {
                        sessionService.removeSession(sessionState.getSessionId());
                    }
                    return new RawMessageImpl(header, message, answer.getMessage().getResult());
                }
            }

            if (header.getSessionId() == null) {
                if (sessionService.commit(sessionState.getSessionId())) {
                    if (header.getSessionId() == null) {
                        sessionService.removeSession(sessionState.getSessionId());
                    }
                    final Body bodyCount = new BodyImpl(MessageUtil.getStatisticResult(idsForDelete.size()));
                    return new RawMessageImpl(header, msr.getByteFromBody(bodyCount), new ResultImpl(OperationStatus.OK));
                }
                if (header.getSessionId() == null) {
                    sessionService.removeSession(sessionState.getSessionId());
                }

                final OperationStatus status = commitPhase1AndPhase2(deleteHeader, msr, MessageUtil.getEmptyBody());
                if (OperationStatus.OK.equals(status)) {
                    closeSession(sessionState.getSessionId());
                } else {
                    return new RawMessageImpl(header, msr.getByteFromBody(body), new ResultImpl(status));
                }

            }
            body = new BodyImpl(MessageUtil.getStatisticResult(idsForDelete.size()));
            idsForDelete.clear();
            return new RawMessageImpl(header, msr.getByteFromBody(body), new ResultImpl(OperationStatus.OK));
        } catch (InterruptedException | ExecutionException e) {
            throw new SystemException(e);
        }
    }

    private void closeSession(final String sessionId) {
        sessionService.removeSession(sessionId);
    }

    OperationStatus rollback(MessageHeader header, final MessageSenderReceiver msr) throws SystemException, LogicalException {
        return rollback(header.getSessionId(), msr, header.getMainSession(), header.getSessions());

    }

    OperationStatus rollback(final String session, final MessageSenderReceiver msr, final String mainSession, final Collection<String> sessions) throws SystemException, LogicalException {
        sessionService.rollback(session);
        return OperationStatus.OK;

    }

    OperationStatus commitPhase1AndPhase2(final MessageHeader header, final MessageSenderReceiver msr, byte[] message) throws LogicalException, SystemException {
        RawMessage rawMessagePhase1 = commitPhase1(header, message, msr);
        if (OperationStatus.OK.equals(rawMessagePhase1.getResult().getOperationStatus())) {
            commitPhase2(header, message, msr);
            return OperationStatus.OK;
        } else {
            rollback(header.getSessionId(), msr, header.getMainSession(), header.getSessions());
            return rawMessagePhase1.getResult().getOperationStatus();

        }

    }

    private MessageHeader checkSession(MessageHeader header) throws LogicalException {
        MessageHeader headerWithSession = MessageUtil.getHeaderCopy(header);

        SessionState sessionState = null;
        if (sessionService != null) {
            if (header.getSessionId() == null) {
                sessionState = sessionService.startNewSession(null, header.getUserName(), null);

                if (!SessionResult.OK.equals(sessionState.getResult())) {
                    throw new LogicalException(ErrorCodes.Session.ERROR_SESSION_CREATE_SESSION, sessionState.getResult().getCode());
                }
                SessionUtil.fillSessions(headerWithSession, sessionState);
            } else {
                sessionState = sessionService.startChildSession(header.getSessionId(), null);
                if (!SessionResult.OK.equals(sessionState.getResult())) {
                    throw new LogicalException(ErrorCodes.Session.ERROR_SESSION_CREATE_SESSION, sessionState.getResult().getCode());
                }
                SessionUtil.fillSessions(headerWithSession, sessionState);
            }
        }
        return headerWithSession;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Result update(MessageHeader header, byte[] message, MessageSenderReceiver msr) throws SystemException, LogicalException {
        final Body body = msr.getBodyFromByte(message);
        Collection<UpdateRequest> updateRequests = body.getRequest().getUpdateRequest();
        if (body.getRequest() == null || body.getRequest().getUpdateRequest() == null) {
            return new ResultImpl(OperationStatus.ERROR, "invalid update request");

        }
        try {
            if (body.getRequest() != null && body.getRequest().getCriteria() != null) {
                if (body.getRequest().getIdentifiers() == null) {
                    body.getRequest().setIdentifiers(new ArrayList<Identifier>());
                }
                body.getRequest().getIdentifiers().addAll((Collection) getIdentifierByCriteria(body.getRequest().getCriteria(), header, msr));
            }

        } catch (Exception e) {
            return new ResultImpl(OperationStatus.ERROR, e.getMessage());
        }

        final Collection<StoreConnectionPool> servers = childrenServerService.getCacheConnectionsPools(header.getModelName());

        if (servers == null) {
            LOGGER.error("Model: {} does not supported", header);
            throw new LogicalException(ErrorCodes.Cache.ERROR_CACHE_MODEL_UNSUPPORTED, header.getModelName());
        }

        try {
            final Map<String, List<Identifier>> normalIdetifiers = getNormalMapForCacheManager(header, body.getRequest().getIdentifiers(), msr);
            final List<MessageRequest> requests = new ArrayList<>(servers.size());
            MessageHeader updateHeader = MessageUtil.getHeaderCopy(header, BaseCommand.UPDATE.toString());
            updateHeader = checkSession(updateHeader);
            for (final StoreConnectionPool storeConnectionPool : servers) {
                List<Identifier> ids = normalIdetifiers.get(storeConnectionPool.getPoolId());
                if (ids != null) {
                    final Body newBody = new BodyImpl(null, ids, null);
                    newBody.getRequest().setUpdateRequest(updateRequests);
                    requests.add(new MessageRequest(new RawMessageImpl(updateHeader, msr.getByteFromBody(newBody)), storeConnectionPool));
                }
            }

            try {
                List<Future<MessageAnswer>> resultList = executorServiceProvider.getExecutorService().invokeAll(requests, requestTimeout, TimeUnit.SECONDS);
                Result result = new ResultImpl(OperationStatus.OK);
                for (Future<MessageAnswer> future : resultList) {

                    try {
                        Result currentResult = future.get().getMessage().getResult();
                        if (!OperationStatus.OK.equals(currentResult)) {
                            LOGGER.debug(future.get().getPool().getPoolId() + " result:" + currentResult);
                            result = currentResult;
                        }
                    } catch (ExecutionException e) {
                        LOGGER.error(e.getMessage(), e);
                        throw new SystemException(e);
                    }
                }
                if (header.getSessionId() == null) {
                    if (OperationStatus.OK.equals(result.getOperationStatus())) {
                        commitPhase1AndPhase2(updateHeader, msr, message);
                    } else {
                        rollback(updateHeader, msr);
                    }
                }
                return result;
            } catch (InterruptedException e1) {

                throw new SystemException(e1);
            }

        } catch (SystemException e) {
            LOGGER.error(e.getMessage(), e);
            rollback(header.getSessionId(), msr, header.getMainSession(), header.getSessions());
            return new ResultImpl(OperationStatus.ERROR, e.getMessage());

        }

    }

}
