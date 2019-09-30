package com.nomad.core;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.cache.commonclientserver.BodyImpl;
import com.nomad.cache.commonclientserver.RawMessageImpl;
import com.nomad.cache.commonclientserver.ResultImpl;
import com.nomad.exception.EOMException;
import com.nomad.exception.ErrorCodes;
import com.nomad.exception.LogicalException;
import com.nomad.exception.SystemException;
import com.nomad.message.Body;
import com.nomad.message.MessageHeader;
import com.nomad.message.MessageSenderReceiver;
import com.nomad.message.OperationStatus;
import com.nomad.message.RawMessage;
import com.nomad.message.Result;
import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.model.ServiceCommand;
import com.nomad.model.criteria.StatisticElement;
import com.nomad.model.criteria.StatisticResult;
import com.nomad.model.criteria.StatisticResultImpl;
import com.nomad.server.DataDefinitionService;
import com.nomad.server.ExecutorServiceProvider;
import com.nomad.server.SaveService;
import com.nomad.server.ServerContext;
import com.nomad.server.ServerContext.ServiceName;
import com.nomad.server.SessionService;
import com.nomad.server.service.ChildrenServerService;
import com.nomad.server.service.childserver.MessageAnswer;
import com.nomad.server.service.childserver.MessageRequest;
import com.nomad.server.service.childserver.StoreConnectionPool;
import com.nomad.server.service.commandplugin.CommandPluginService;
import com.nomad.server.service.storemodelservice.StoreModelServiceImpl;
import com.nomad.utility.ModelUtil;
import com.nomad.utility.MessageUtil;

public class CommonProxyProcessing {

    protected static Logger LOGGER = LoggerFactory.getLogger(CommonProxyProcessing.class);
    protected volatile CommandPluginService commandPluginService;
    protected SessionService sessionService;
    protected ChildrenServerService childrenServerService;
    // private volatile ServerContext context;
    protected volatile StoreModelServiceImpl server;
    protected volatile ServerContext context;
    protected long requestTimeout = 60 * 1000;
    protected ExecutorServiceProvider executorServiceProvider;
    protected final DataDefinitionService dataDefinition;
    // protected final BlockService blockService;
    protected final String serverName;

    public CommonProxyProcessing(final ServerContext context, final ExecutorServiceProvider executorServiceProvider) throws SystemException {
        this.context = context;
        server = (StoreModelServiceImpl) context.get(ServerContext.ServiceName.STORE_MODEL_SERVICE);
        if (server != null) {
            serverName = server.getServerModel().getServerName();
        } else {
            serverName = "empty";
        }
        commandPluginService = (CommandPluginService) context.get(ServerContext.ServiceName.PROXY_PLUGIN);
        sessionService = (SessionService) context.get(ServerContext.ServiceName.SESSION_SERVICE);
        childrenServerService = (ChildrenServerService) context.get(ServerContext.ServiceName.CHILDREN_SERVICE);
        this.executorServiceProvider = executorServiceProvider;
        dataDefinition = context.getDataDefinitionService(null);
    }

    protected void closeSessionInSaveService(final Collection<String> sessionIds) throws LogicalException, SystemException {
        final SaveService saveService = (SaveService) context.get(ServiceName.SAVE_SERVICE);
        if (saveService != null) {
            saveService.cleanSession(sessionIds);
        }
    }
    /**
     * this server know about Model
     * 
     * @throws SystemException
     */
    protected RawMessage rollbackAnyServerForCacheManager(final MessageHeader header, final byte[] message, final MessageSenderReceiver msr)
            throws SystemException {

        final Collection<StoreConnectionPool> servers = childrenServerService.getCacheConnectionsPools(null);
        if (servers == null) {
            LOGGER.error("No child servers! ", header);
            return new RawMessageImpl(header, message, new ResultImpl(OperationStatus.UNSUPPORTED_MODEL_NAME, "model does not supported" + header));
        }

        final Collection<MessageRequest> requests = new ArrayList<>(servers.size());

        for (final StoreConnectionPool srv : servers) {
            requests.add(new MessageRequest(new RawMessageImpl(header, message), srv));
        }
        Result result = new ResultImpl(OperationStatus.OK);
        try {
            final List<Future<MessageAnswer>> resultList = executorServiceProvider.getExecutorService().invokeAll(requests, requestTimeout, TimeUnit.SECONDS);
            for (final Future<MessageAnswer> fmsg : resultList) {
                try {
                    final MessageAnswer answer = fmsg.get();
                    if (!OperationStatus.OK.equals(answer.getMessage().getResult().getOperationStatus())) {
                        result = answer.getMessage().getResult();
                    }
                } catch (final ExecutionException e) {
                    LOGGER.error("error exec rollbackAnyServerForCacheManager:" + servers + ":" + executorServiceProvider + ":" + e);
                } catch (final Exception e) {
                    LOGGER.error("error execrollbackAnyServerForCacheManager:" + servers + ":" + executorServiceProvider + ":" + e);
                }
            }
        } catch (InterruptedException e) {
            throw new SystemException(e);
        }
        return new RawMessageImpl(header, message, result);
    }

    /**
     * return map client-> list Identifier
     * 
     * @return
     * @throws SystemException
     */
    protected Map<String, List<Identifier>> getNormalMapForCacheManager(final MessageHeader header, final Collection<? extends Identifier> ids,
            final MessageSenderReceiver msr) throws SystemException {

        final Collection<StoreConnectionPool> servers = childrenServerService.getCacheConnectionsPools(header.getModelName());

        final Collection<MessageRequest> requests = new ArrayList<>(servers.size());

        final MessageHeader newHeader = MessageUtil.getHeaderCopy(header);
        newHeader.setCommand(ServiceCommand.IN_LOCAL_CACHE.toString());

        final Body newBody = new BodyImpl(null, new ArrayList<>(ids), null);
        final byte[] newMessage = msr.getByteFromBody(newBody);

        final Map<Identifier, Collection<String>> stores = new HashMap<>(ids.size(), 1);

        for (final StoreConnectionPool srv : servers) {
            requests.add(new MessageRequest(new RawMessageImpl(newHeader, newMessage), srv));
        }
        // check contains
        List<Future<MessageAnswer>> resultList;
        try {
            resultList = executorServiceProvider.getExecutorService().invokeAll(requests, requestTimeout, TimeUnit.SECONDS);
        } catch (InterruptedException e1) {
            throw new SystemException(e1);
        }

        for (final Future<MessageAnswer> fmsg : resultList) {
            try {
                final MessageAnswer answer = fmsg.get();
                RawMessage rawMessage = answer.getMessage();
                final Body answerBody = msr.getBodyFromByte(rawMessage.getMessage());
                if (OperationStatus.OK.equals(rawMessage.getResult().getOperationStatus()) && answerBody.getResponse().getIdentifiers().size() > 0) {
                    final Collection<Identifier> idsAnswer = answerBody.getResponse().getIdentifiers();
                    final String poolId = answer.getPool().getPoolId();

                    for (final Identifier identifier : idsAnswer) {
                        Collection<String> storesForId = stores.get(identifier);
                        if (storesForId == null) {
                            storesForId = new ArrayList<>();
                            stores.put(identifier, storesForId);
                        }
                        storesForId.add(poolId);
                    }

                }
            } catch (final ExecutionException e) {
                LOGGER.error("error exec getNormalMapForCacheManager:" + servers + ":" + executorServiceProvider + ":" + e);
            } catch (final Exception e) {
                LOGGER.error("error exec getNormalMapForCacheManager:" + servers + ":" + executorServiceProvider + ":" + e);
            }
        }

        // normalize stores
        for (final Identifier id : ids) {
            Collection<String> connectionNames = stores.get(id);
            if (connectionNames == null) {
                connectionNames = childrenServerService.getConnectionsPoolsIds(id);
            } else {
                final int count = childrenServerService.getStoriesCount(id);
                if (connectionNames.size() < count) {
                    connectionNames = childrenServerService.fillPoolsIds(id, connectionNames);
                }
            }
            stores.put(id, connectionNames);
        }
        // translate
        final Map<String, StoreConnectionPool> serversMap = new HashMap<>(servers.size(), 1);
        for (final StoreConnectionPool client : servers) {
            serversMap.put(client.getPoolId(), client);
        }

        final Map<String, List<Identifier>> mapPoolId = new HashMap<>(servers.size(), 1);

        for (final Identifier id : ids) {

            final Collection<String> connectionNames = stores.get(id);
            for (final String clientId : connectionNames) {
                List<Identifier> identifierList = mapPoolId.get(clientId);
                if (identifierList == null) {
                    identifierList = new ArrayList<>();
                    mapPoolId.put(clientId, identifierList);
                }
                identifierList.add(id);
            }
        }

        return mapPoolId;
    }

    /**
     * this server unknow about Model
     * 
     * @throws SystemException
     */
    public RawMessage sendBroadcastMessageForAnyServer(final MessageHeader header, final byte[] message, final MessageSenderReceiver msr, final boolean local)
            throws SystemException {

        final Collection<StoreConnectionPool> servers = childrenServerService.getCacheConnectionsPools(header.getModelName());
        if (servers == null) {
            LOGGER.error("server:{} Model: {} does not supported", server.getServerModel().getServerName(), header);
            return new RawMessageImpl(header, message, new ResultImpl(OperationStatus.ERROR, "model does nod supported: " + header));

        }
        final Collection<MessageRequest> requests = new ArrayList<>(servers.size());

        for (final StoreConnectionPool srv : servers) {
            if (!srv.isLocal() || local) { // not local
                requests.add(new MessageRequest(new RawMessageImpl(header, message), srv));
            }
        }
        OperationStatus status = OperationStatus.OK;
        try {
            final List<Future<MessageAnswer>> resultList = executorServiceProvider.getExecutorService().invokeAll(requests, requestTimeout, TimeUnit.SECONDS);

            for (final Future<MessageAnswer> fmsg : resultList) {
                try {
                    final MessageAnswer answer = fmsg.get();
                    if (OperationStatus.OK.equals(answer.getMessage().getResult().getOperationStatus())) {
                        return answer.getMessage();
                    } else {
                        status = answer.getMessage().getResult().getOperationStatus();
                    }
                } catch (final ExecutionException e) {
                    LOGGER.error("error exec sendBradcastMessageForAnyServer:" + servers + ":" + fmsg.get().getPool().getPoolId() + ":" + e);
                } catch (final Exception e) {
                    LOGGER.error("error exec sendBradcastMessageForAnyServer:" + servers + ":" + fmsg.get().getPool().getPoolId() + ":" + e, e);
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new SystemException(e);
        }
        return new RawMessageImpl(header, message, new ResultImpl(status));
    }

    /**
     * this server unknow about Model
     * 
     * @throws SystemException
     */
    public RawMessage sendBroadcastMessageForEachServer(final MessageHeader header, final byte[] message, final MessageSenderReceiver msr, final boolean local)
            throws SystemException {

        final Collection<StoreConnectionPool> servers = childrenServerService.getCacheConnectionsPools(header.getModelName());
        if (servers == null) {
            LOGGER.error("server:{} Model: {} does not supported", server.getServerModel().getServerName(), header);
            return new RawMessageImpl(header, message, new ResultImpl(OperationStatus.OK));

        }

        final Collection<MessageRequest> requests = new ArrayList<>(servers.size());

        for (final StoreConnectionPool srv : servers) {
            if (!srv.isLocal() || local) { // not local
                LOGGER.debug("sendBradcastMessageForEachServer: client:{}, server:{}", server.getServerModel().getServerName(), "local ");
                requests.add(new MessageRequest(new RawMessageImpl(header, message), srv));
            }

        }
        try {
            final List<Future<MessageAnswer>> resultList = executorServiceProvider.getExecutorService().invokeAll(requests, requestTimeout, TimeUnit.SECONDS);

            for (final Future<MessageAnswer> fmsg : resultList) {
                try {
                    final MessageAnswer answer = fmsg.get();
                    if (!OperationStatus.OK.equals(answer.getMessage().getResult().getOperationStatus())) {
                        return answer.getMessage();
                    }
                } catch (final ExecutionException e) {
                    LOGGER.error("error exec sendBradcastMessageForEachServer:" + servers + ":" + fmsg.get().getPool().getPoolId() + ":" + e);
                } catch (final Exception e) {
                    LOGGER.error("error exec sendBradcastMessageForEachServer:" + servers + ":" + fmsg.get().getPool().getPoolId() + ":" + e, e);
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new SystemException(e);
        }
        return new RawMessageImpl(header, message, new ResultImpl(OperationStatus.OK));
    }

    protected void block(final MessageHeader header, final InputStream input, final MessageSenderReceiver msr) throws LogicalException, SystemException {
        final byte[] message = MessageUtil.readByteBody(input);
        final Body bodyBlock = msr.getBodyFromByte(message);
        block(header, bodyBlock, msr, false);
    }

    protected void hardBlock(final MessageHeader header, final Body bodyBlock, final MessageSenderReceiver msr) throws LogicalException, SystemException {
        block(header, bodyBlock, msr, true);
    }

    private void block(final MessageHeader header, final Body bodyBlock, final MessageSenderReceiver msr, boolean hardBlock)
            throws LogicalException, SystemException {
        List<Identifier> ids = (List<Identifier>) bodyBlock.getRequest().getIdentifiers();

        if (bodyBlock.getRequest().getModels() != null && bodyBlock.getRequest().getModels().size() > 0) {
            if (ids == null) {
                ids = new ArrayList<>();
            }
            ids.addAll(ModelUtil.getIdentifiers(bodyBlock.getRequest().getModels()));
        }

        Map<String, List<Identifier>> normalizedIds = getNormalFullMapForCacheManager(header, ids, msr);
        final Collection<StoreConnectionPool> servers = childrenServerService.getCacheConnectionsPools(header.getModelName());

        final MessageHeader blockHeader = MessageUtil.getHeaderCopy(header);
        if (hardBlock) {
            blockHeader.setCommand(ServiceCommand.HARD_BLOCK.toString());
        } else {
            blockHeader.setCommand(ServiceCommand.BLOCK.toString());
        }

        final List<MessageRequest> requests = new ArrayList<>(servers.size());
        for (final StoreConnectionPool srv : servers) {
            final List<Identifier> modelsForRequest = normalizedIds.get(srv.getPoolId());
            if (!modelsForRequest.isEmpty()) {
                final Body newBody = new BodyImpl(null, modelsForRequest, null);
                requests.add(new MessageRequest(new RawMessageImpl(blockHeader, msr.getByteFromBody(newBody)), srv));
            }

        }
        boolean allOk = true;

        try {
            final List<Future<MessageAnswer>> resultList = executorServiceProvider.getExecutorService().invokeAll(requests, requestTimeout, TimeUnit.SECONDS);
            List<Identifier> errorIds = new ArrayList<>();
            for (final Future<MessageAnswer> fmsg : resultList) {
                try {
                    final MessageAnswer messageAnswer = fmsg.get();
                    if (!OperationStatus.OK.equals(messageAnswer.getMessage().getResult().getOperationStatus())) {
                        Body errorAnswer = msr.getBodyFromByte(messageAnswer.getMessage().getMessage());
                        if (errorAnswer.getResponse() != null && errorAnswer.getResponse().getIdentifiers() != null) {
                            errorIds.addAll(errorAnswer.getResponse().getIdentifiers());
                        }
                        allOk = false;
                    }
                } catch (final ExecutionException e) {
                    allOk = false;
                } catch (final Exception e) {
                    allOk = false;
                }
            }
        } catch (InterruptedException e) {
            allOk = false;
        }
        if (!allOk) {
            blockHeader.setCommand(ServiceCommand.UNBLOCK.toString());
            requests.clear();
            for (final StoreConnectionPool srv : servers) {
                final List<Identifier> modelsForRequest = normalizedIds.get(srv.getPoolId());
                if (!modelsForRequest.isEmpty()) {
                    final Body newBody = new BodyImpl(null, modelsForRequest, null);
                    requests.add(new MessageRequest(new RawMessageImpl(blockHeader, msr.getByteFromBody(newBody)), srv));

                }
            }
            try {
                executorServiceProvider.getExecutorService().invokeAll(requests, requestTimeout, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
                e.printStackTrace();
            }
            if (hardBlock) {
                throw new LogicalException(ErrorCodes.Block.ERROR_CACHE_HARD_BLOCK);
            } else {
                throw new LogicalException(ErrorCodes.Block.ERROR_CACHE_SOFT_BLOCK);

            }
        }

    }

    protected RawMessage unblock(final String sessionId, final MessageSenderReceiver msr) throws SystemException {
        final byte[] message = MessageUtil.getEmptyBody(); // for unblock need
                                                           // only sessionId
        final MessageHeader header = new MessageHeader();
        header.setSessionId(sessionId);
        return unblock(header, message, msr);
    }

    protected RawMessage unblock(final MessageHeader header, final InputStream input, final MessageSenderReceiver msr) throws EOMException, SystemException {
        final byte[] message =  MessageUtil.readByteBody(input);
        return unblock(header, message, msr);
    }

    protected RawMessage unblock(final MessageHeader header, final byte[] message, final MessageSenderReceiver msr) throws SystemException {
        // local
        return sendBroadcastMessageForEachServer(MessageUtil.getHeaderCopy(header, ServiceCommand.UNBLOCK.toString()), message, msr, true);
    }

    /**
     * this server unknown about Model
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    RawMessage sendBroadcastMessageForEachServerAndSumResults(final MessageHeader header, final byte[] message, final MessageSenderReceiver msr,
            final boolean local) throws SystemException, LogicalException {

        final Collection<StoreConnectionPool> servers = childrenServerService.getCacheConnectionsPools(header.getModelName());

        if (servers == null) {
            LOGGER.error("server:{} Model: {} does not supported", server.getServerModel().getServerName(), header);
            return new RawMessageImpl(header, message, new ResultImpl(OperationStatus.ERROR, "model does nod supported: " + header));

        }

        final Collection<MessageRequest> requests = new ArrayList<>(servers.size());

        for (final StoreConnectionPool srv : servers) {
            if (!srv.isLocal() || local) { // not local

                LOGGER.debug("sendBradcastMessageForEachServerAndSumResults: client:{}, server:{}", server.getServerModel().getServerName(), " local");
                requests.add(new MessageRequest(new RawMessageImpl(header, message), srv));
            }
        }
        List<Future<MessageAnswer>> resultList;
        try {
            resultList = executorServiceProvider.getExecutorService().invokeAll(requests, requestTimeout, TimeUnit.SECONDS);
        } catch (InterruptedException e1) {
            throw new SystemException(e1);
        }
        final Set<Identifier> identifiers = new HashSet<>();
        final Set<Model> models = new HashSet<>();
        for (final Future<MessageAnswer> fmsg : resultList) {
            try {
                final MessageAnswer messageAnswer = fmsg.get();
                if (OperationStatus.OK.equals(messageAnswer.getMessage().getResult().getOperationStatus())) {
                    final Body body = msr.getBodyFromByte(messageAnswer.getMessage().getMessage());
                    if (body.getResponse() != null && body.getResponse().getIdentifiers() != null) {
                        for (final Identifier id : body.getResponse().getIdentifiers()) {
                            identifiers.add(id);
                        }
                    }
                    if (body.getResponse() != null && body.getResponse().getResultList() != null) {
                        for (final Model model : body.getResponse().getResultList()) {
                            models.add(model);
                        }
                    }
                }
            } catch (final ExecutionException e) {
                throw new SystemException(e);
            } catch (final Exception e) {
                throw new SystemException(e);
            }
        }
        StatisticResultImpl<? extends Model> result = new StatisticResultImpl<>();
        result.setIdentifiers(new ArrayList<>(identifiers));
        result.setResultList(new ArrayList(models));
        final Body resultBody = new BodyImpl(result);

        return new RawMessageImpl(header, msr.getByteFromBody(resultBody), new ResultImpl(OperationStatus.OK));
    }

    protected Map<String, List<Identifier>> getSimpleNormalMapForCacheManager(final MessageHeader header, final Collection<? extends Identifier> ids) {

        final Collection<StoreConnectionPool> servers = childrenServerService.getCacheConnectionsPools(header.getModelName());
        final Map<Identifier, Collection<String>> stores = new HashMap<>();

        // normalize stores
        for (final Identifier id : ids) {
            Collection<String> connections = stores.get(id);
            if (connections == null) {
                connections = childrenServerService.getConnectionsPoolsIds(id);
            } else {
                final int count = childrenServerService.getStoriesCount(id);
                if (connections.size() < count) {
                    connections = childrenServerService.fillPoolsIds(id, connections);
                }
            }
            stores.put(id, connections);

        }
        // translate
        final Map<String, StoreConnectionPool> serversMap = new HashMap<>(servers.size(), 1);
        for (final StoreConnectionPool client : servers) {
            serversMap.put(client.getPoolId(), client);
        }

        final Map<String, List<Identifier>> mapPoolId = new HashMap<>(servers.size(), 1);

        for (final Identifier id : ids) {

            for (final String clientId : stores.get(id)) {
                List<Identifier> mappedIds = mapPoolId.get(clientId);
                if (mappedIds == null) {
                    mappedIds = new ArrayList<>();
                    mapPoolId.put(clientId, mappedIds);
                }
                mappedIds.add(id);
            }
        }

        return mapPoolId;
    }

    protected StatisticResult<Model> addStatistic(StatisticResult<Model> target, StatisticResult<Model> source) {
        if (source == null) {
            return target;
        }
        if (target == null) {
            return source;
        }
        target.setCountAllRow(target.getCountAllRow() + source.getCountAllRow());
        List<Identifier> ids = null;
        if (target.getIdentifiers() != null) {
            ids = new ArrayList<Identifier>(target.getIdentifiers());
        }
        if (source.getIdentifiers() != null) {
            if (ids == null) {
                ids = new ArrayList<Identifier>(target.getIdentifiers());
            } else {
                ids.addAll(source.getIdentifiers());
            }
        }
        target.setIdentifiers(ids);
        target.setPageSize(source.getPageSize());

        List<Model> models = null;
        if (target.getResultList() != null) {
            models = new ArrayList<Model>(target.getResultList());
        }
        if (source.getResultList() != null) {
            if (models == null) {
                models = new ArrayList<Model>(target.getResultList());
            } else {
                models.addAll(source.getResultList());
            }
        }
        target.setResultList(models);
        target.setStartPosition(source.getStartPosition());
        // statustic
        List<StatisticElement> statistics = null;
        if (target.getStatistics() != null) {
            statistics = new ArrayList<StatisticElement>(target.getStatistics());
        }

        if (source.getStatistics() != null) {
            if (statistics == null) {
                statistics = new ArrayList<StatisticElement>(source.getStatistics());
            } else {// merge
                for (StatisticElement statisticElement : source.getStatistics()) {
                    int index = statistics.indexOf(statisticElement);
                    if (index >= 0) {
                        mergeElements(statistics.get(index), statisticElement, 0);
                    } else {
                        statistics.add(statisticElement);
                    }
                }
            }
        }
        target.setStatistics(statistics);

        // grouping
        statistics = null;
        if (target.getGroups() != null) {
            statistics = new ArrayList<StatisticElement>(target.getGroups());
        }
        if (source.getGroups() != null) {
            if (statistics == null) {
                statistics = new ArrayList<StatisticElement>(source.getGroups());
            } else {// merge
                for (StatisticElement statisticElement : source.getGroups()) {
                    int index = statistics.indexOf(statisticElement);
                    if (index >= 0) {
                        mergeElements(statistics.get(index), statisticElement, 0);
                    } else {
                        statistics.add(statisticElement);
                    }
                }
            }
        }
        target.setGroups(statistics);

        return target;

    }

    private void mergeElements(StatisticElement target, StatisticElement source, int level) {
        // SUM, AVG, MIN, MAX, COUNT
        if (source.getFunction() != null) {
            switch (source.getFunction()) {
            case SUM:
            case COUNT:
                target.setValue(target.getDoubleValue() + source.getDoubleValue());
                break;
            case AVG:
                target.setValue((target.getDoubleValue() + source.getDoubleValue()) / 2);
                break;
            case MAX:
                target.setValue(Math.max(target.getDoubleValue(), source.getDoubleValue()));
                break;
            case MIN:
                target.setValue(Math.min(target.getDoubleValue(), source.getDoubleValue()));
                break;
            default:
                break;
            }
            return;
        }
        if (target.getChildren().size() == 0 && source.getChildren() == null) {
            return;
        }
        if (source.getChildren() == null) {
            return;
        }
        if (target.getChildren() == null) {
            target.getChildren().addAll(source.getChildren());
            return;
        }

        for (StatisticElement element : target.getChildren()) {
            StatisticElement childElement = source.getChild(element);

            if (childElement != null) {
                mergeElements(element, childElement, level + 1);
            }
        }
        for (StatisticElement element : source.getChildren()) {
            if (target.getChild(element) == null) {
                target.getChildren().add(element);
            }
        }
    }

    /**
     * return map client-> list Identifier
     * 
     * @return
     */
    protected Map<String, List<Identifier>> getNormalFullMapForCacheManager(final MessageHeader header, final List<? extends Identifier> ids,
            final MessageSenderReceiver msr) {
        Map<String, List<Identifier>> result = new HashMap<>();
        for (Identifier identifier : ids) {
            Collection<String> serverIds = childrenServerService.getFullConnectionsPoolsIds(identifier);
            for (String serverId : serverIds) {
                List<Identifier> ids1 = result.get(serverId);
                if (ids1 == null) {
                    ids1 = new ArrayList<>();
                    result.put(serverId, ids1);
                }
                ids1.add(identifier);
            }
        }
        return result;

    }

    protected Collection<Identifier> getIdentifiers(Collection<Identifier> identifiers, Collection<Model> models) {
        if (identifiers == null) {
            identifiers = new ArrayList<Identifier>();
        }
        if (models != null && !models.isEmpty()) {
            for (Model model : models) {
                identifiers.add(model.getIdentifier());
            }
        }
        return identifiers;
    }

}
