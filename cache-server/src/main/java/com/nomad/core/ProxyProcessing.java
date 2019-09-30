package com.nomad.core;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.cache.commonclientserver.BodyImpl;
import com.nomad.cache.commonclientserver.FullMessageImpl;
import com.nomad.cache.commonclientserver.RawMessageImpl;
import com.nomad.cache.commonclientserver.ResultImpl;
import com.nomad.exception.LogicalException;
import com.nomad.exception.SystemException;
import com.nomad.exception.UnsupportedModelException;
import com.nomad.message.Body;
import com.nomad.message.FullMessage;
import com.nomad.message.MessageHeader;
import com.nomad.message.MessageSenderReceiver;
import com.nomad.message.OperationStatus;
import com.nomad.message.RawMessage;
import com.nomad.message.Result;
import com.nomad.model.BaseCommand;
import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.model.ServiceCommand;
import com.nomad.model.criteria.StatisticResult;
import com.nomad.model.criteria.StatisticResultImpl;
import com.nomad.server.CommandPlugin;
import com.nomad.server.ExecutorServiceProvider;
import com.nomad.server.ServerContext;
import com.nomad.server.ServerContext.ServiceName;
import com.nomad.server.SessionResult;
import com.nomad.server.SessionService;
import com.nomad.server.SessionState;
import com.nomad.server.StoreModelService;
import com.nomad.server.processing.ProxyProcessingInterface;
import com.nomad.server.service.commandplugin.CommandPluginService;
import com.nomad.session.SessionContainer;
import com.nomad.session.SessionThread;
import com.nomad.utility.MessageUtil;

public class ProxyProcessing implements ProxyProcessingInterface {

    private static Logger LOGGER = LoggerFactory.getLogger(ProxyProcessing.class);
    private volatile CommandPluginService commandPluginService;
    private final SessionService transactSessionAdvisor;
    private volatile StoreModelService server;

    private final ProxyCacheProcessing cacheProcessing;
    private final ProxyCacheManagerProcessing cacheManagerProcessing;
    private final ServiceProcessing serviceProcessing;
    private final ExecutorServiceProvider executorServiceProvider;
    private static Set<String> sessionFreeCommands = new HashSet<String>();
    private static Set<String> sessionMandatoryCommands = new HashSet<String>();
    private final ServerContext context;

    static {
        sessionFreeCommands.add(ServiceCommand.TEST.name());
        sessionFreeCommands.add(ServiceCommand.CHARACTERISTIC_TEST.name());
        sessionFreeCommands.add(BaseCommand.START_NEW_SESSION.name());

        sessionMandatoryCommands.add(BaseCommand.COMMIT.name());
        sessionMandatoryCommands.add(BaseCommand.ROLLBACK.name());
    }

    public ProxyProcessing(final ServerContext context, String serverName) throws SystemException {
        super();
        // this.context=context;
        server = (StoreModelService) context.get(ServerContext.ServiceName.STORE_MODEL_SERVICE);
        commandPluginService = (CommandPluginService) context.get(ServerContext.ServiceName.PROXY_PLUGIN);
        transactSessionAdvisor = (SessionService) context.get(ServerContext.ServiceName.SESSION_SERVICE);

        executorServiceProvider = context.getExecutorServiceProvider();

        cacheManagerProcessing = new ProxyCacheManagerProcessing(context, executorServiceProvider, this);
        cacheProcessing = new ProxyCacheProcessing(context, executorServiceProvider);
        serviceProcessing = new ServiceProcessing(context, executorServiceProvider);
        this.context = context;

    }

    public ProxyProcessing(final ServerContext context, final ExecutorServiceProvider executorProvider) throws SystemException {
        super();
        server = (StoreModelService) context.get(ServerContext.ServiceName.STORE_MODEL_SERVICE);
        this.commandPluginService = (CommandPluginService) context.get(ServerContext.ServiceName.PROXY_PLUGIN);
        transactSessionAdvisor = (SessionService) context.get(ServerContext.ServiceName.SESSION_SERVICE);
        executorServiceProvider = executorProvider;
        cacheManagerProcessing = new ProxyCacheManagerProcessing(context, executorServiceProvider, this);
        cacheProcessing = new ProxyCacheProcessing(context, executorServiceProvider);
        serviceProcessing = new ServiceProcessing(context, executorServiceProvider);
        this.context = context;

    }

    private SessionContainer getSessionContainer(SessionState state) {
        SessionContainer result = new SessionContainer();
        result.setSession(state);
        return result;
    }

    @Override
    public RawMessage execMessage(final MessageHeader header, final InputStream input, final MessageSenderReceiver msr) throws SystemException, LogicalException {

        LOGGER.debug("Proxy server:{} msg: {}", server.getServerModel().getServerName(), header);
        final String sessionId = header.getSessionId();
        try {
            if (sessionId != null && !transactSessionAdvisor.isTrustService() && (!sessionFreeCommands.contains(header.getCommand()) || sessionMandatoryCommands.contains(header.getCommand()))) {
                final SessionState sessionState = transactSessionAdvisor.getSessionState(sessionId, null, header.getCommand());
                header.setMainSession(sessionState.getMainSession());
                header.getSessions().addAll(sessionState.getChildrenSessions());
                if (!SessionResult.OK.equals(sessionState.getResult())) {
                    MessageUtil.readByteBody(input);
                    return new RawMessageImpl(header, msr.getEmptyBody(), new ResultImpl(OperationStatus.INVALID_SESSION));
                }
                SessionThread.set(getSessionContainer(sessionState));
                header.getSessions().addAll(sessionState.getChildrenSessions());
            }

            if (sessionId == null && !server.getServerModel().isTrustSessions() && !sessionFreeCommands.contains(header.getCommand())) {
                MessageUtil.readByteBody(input);
                return new RawMessageImpl(header, msr.getEmptyBody(), new ResultImpl(OperationStatus.INVALID_SESSION));
            }
            try {
                final BaseCommand command = BaseCommand.valueOf(header.getCommand());
                return callBaseCommand(command, input, msr, header);
            } catch (final IllegalArgumentException e) {
                
            } catch (final Exception e) {
                LOGGER.error("server:" + server.getServerModel().getServerName()+" " + e.getMessage(), e);
                return new RawMessageImpl(header, msr.getEmptyBody(), new ResultImpl(OperationStatus.ERROR, e.getMessage()));
            }

            try {
                final ServiceCommand command = ServiceCommand.valueOf(header.getCommand());
                return callServiceCommand(command, input, msr, header);
            } catch (final IllegalArgumentException e) {
            } catch (final LogicalException e) {
                return new RawMessageImpl(header, msr.getEmptyBody(), new ResultImpl(e));
            } catch (final Exception e) {
                return new RawMessageImpl(header, msr.getEmptyBody(), new ResultImpl(OperationStatus.ERROR, e.getMessage()));
            }

            CommandPlugin pluginInstance = commandPluginService.getPlugin(header.getCommand());
            if (pluginInstance != null) {
                try {
                    final FullMessage fullMessage = pluginInstance.executeMessage(new FullMessageImpl(header, msr.getBody(input)));
                    return new RawMessageImpl(fullMessage.getHeader(), msr.getByteFromBody(fullMessage.getBody()), fullMessage.getResult());

                } catch (final Throwable x) {
                    LOGGER.error(x.getMessage(), x);

                    final Result res = new ResultImpl(OperationStatus.BLOCKED, x.getMessage());

                    return new RawMessageImpl(header, msr.getEmptyBody(), res);

                } finally {
                    if (pluginInstance != null) {
                        pluginInstance.freeObject();
                    }
                }
            }
            MessageUtil.readByteBody(input);
            LOGGER.warn("Invalid operation name header:" + header);
            return new RawMessageImpl(header, msr.getEmptyBody(), new ResultImpl(OperationStatus.INVALID_OPERATION_NAME, "header:" + header));
        } catch (final Exception x) {
            LOGGER.error(x.getMessage(), x);
            byte[] data = null;
            data = MessageUtil.readByteBody(input);
            final Result res = new ResultImpl(OperationStatus.BLOCKED, x.getMessage());

            return new RawMessageImpl(header, data, res);
        }

    }

    private RawMessage callBaseCommand(final BaseCommand command, final InputStream input, final MessageSenderReceiver msr, final MessageHeader header) throws SystemException, LogicalException {

        final int commandIndex = command.getCommandIndex();
        switch (commandIndex) {
        case 1: // StartNewSession:
            return startNewSession(header, input, msr);
        case 2: // Get:
            return get(header, input, msr);
        case 3: // Put:
            return put(header, input, msr);
        case 4: // Delete:
            return delete(header, input, msr);
        case 5: // Commit:
            return commit(header, input, msr);
        case 6: // Rollback:
            return rollback(header, input, msr);
        case 7: // CloseSession:
            return closeSession(header, input, msr);
        case 8: // update
            return update(header, input, msr);
        case 9: // inCache:
            return inCache(header, input, msr);
        case 10: // GetIdentifiersByCriteria:
            return getModelsByCriteria(header, input, msr);
        case 11: // DeleteByCriteria:
            return deleteByCriteria(header, input, msr);
        case 12: // Start New Child Session:
            return startNewChildSession(input, header, msr);
        default:
            throw new IllegalArgumentException();
        }

    }

    private RawMessage callServiceCommand(final ServiceCommand command, final InputStream input, final MessageSenderReceiver msr, final MessageHeader header) throws LogicalException, SystemException {

        LOGGER.debug("CallServiceCommand server: {} header:{}", server.getServerModel().getServerName(), header);
        final int commandIndex = command.getCommandIndex();
        switch (commandIndex) {
        case 11: // InLocalCache:
            return inLocalCache(header, input, msr);
        case 12: // GetFromCache:
            return getGromCache(header, input, msr);
        case 13: // PutIntoCache:
            return putIntoCache(header, input, msr);
        case 14: // DeleteFromCache:
            return deleteFromCache(header, input, msr);
        case 17: // Commit_ph1:
            return commitPhase1(header, input, msr);
        case 18: // Commit_ph2:
            return commitPhase2(header, input, msr);
        case 16: // Block:
            cacheProcessing.block(header, input, msr);
            return new RawMessageImpl(header, MessageUtil.getEmptyBody(), new ResultImpl(OperationStatus.OK));
        case 19: // unBlock:
            return cacheProcessing.unblock(header, input, msr);
        case 20: // CleanSaveService:
            cacheProcessing.closeSessionInSaveService(header.getSessions());
            return new RawMessageImpl(header, MessageUtil.readByteBody(input), new ResultImpl(OperationStatus.OK));
        case 21: // GetIdsByCriteria:
            return cacheProcessing.execInLocalServer(header, input, msr);
        case 15: // RollBackInCache:
            return serviceProcessing.sendBroadcastMessage(header, input, msr, true);
        case 22: // Test:
            return new RawMessageImpl(header, MessageUtil.readByteBody(input), new ResultImpl(OperationStatus.OK));
        case 23: // get Characteristic:
            return cacheProcessing.execInLocalServer(header, input, msr);
        default:
            throw new IllegalArgumentException();
        }
    }

    private RawMessage closeSession(MessageHeader header, InputStream input, MessageSenderReceiver msr) throws SystemException, LogicalException {
        MessageUtil.readByteBody(input);
        OperationStatus operationStatus = OperationStatus.OK;
        if (!transactSessionAdvisor.removeSession(header.getSessionId())) {
            operationStatus = OperationStatus.INVALID_SESSION;
        }
        return new RawMessageImpl(header, msr.getEmptyBody(), new ResultImpl(operationStatus));

    }

    private RawMessage startNewSession(MessageHeader header, InputStream input, MessageSenderReceiver msr) throws SystemException, LogicalException {
        final byte[] sessionFreeCommandMessage = MessageUtil.readByteBody(input);
        if (context.get(ServiceName.SESSION_SERVICE) == null) {
            return serviceProcessing.askСhildrenSequentially(header, sessionFreeCommandMessage, msr, false);
        } else {
            SessionState sessionState = transactSessionAdvisor.startNewSession(null, header.getUserName(), header.getPassword());
            header.setSessionId(sessionState.getSessionId());
            OperationStatus operationResult = null;
            if (SessionResult.OK.equals(sessionState.getResult())) {
                operationResult = OperationStatus.OK;
            } else if (SessionResult.ACCESS_DENIED.equals(sessionState.getResult())) {
                operationResult = OperationStatus.ACCESS_DENIED;
            } else {
                operationResult = OperationStatus.INVALID_SESSION;
            }
            return new RawMessageImpl(header, msr.getEmptyBody(), new ResultImpl(operationResult));
        }

    }

    private RawMessage deleteFromCache(MessageHeader header, InputStream input, MessageSenderReceiver msr) throws SystemException, LogicalException {
        Body body = msr.getBody(input);
        Collection<Identifier> identifiers = cacheProcessing.getIdentifiers(body.getRequest().getIdentifiers(), body.getRequest().getModels());
        identifiers = cacheProcessing.deleteFromCache(identifiers, new SessionContainerImpl(header));
        body.cleanRequest();
        StatisticResult<? extends Model> response = new StatisticResultImpl<>();
        response.setIdentifiers(identifiers);
        body.setResponse(response);
        return new RawMessageImpl(header, msr.getByteFromBody(body), new ResultImpl(OperationStatus.OK));
    }

    private RawMessage putIntoCache(MessageHeader header, InputStream input, MessageSenderReceiver msr) throws SystemException, LogicalException {
        Body body = msr.getBody(input);
        com.nomad.model.core.SessionContainer sessions = new SessionContainerImpl(header);
        LOGGER.debug("putIntoCache server: " + server.getServerModel().getServerName() + " sessions:{} models:{}", sessions, body.getRequest().getModels());
        final Collection<Model> models = cacheProcessing.put(body.getRequest().getModels(), sessions);
        body.cleanRequest();
        body.setResponse(new StatisticResultImpl<>(models));
        return new RawMessageImpl(header, msr.getByteFromBody(body), new ResultImpl(OperationStatus.OK));
    }

    private RawMessage getGromCache(MessageHeader header, InputStream input, MessageSenderReceiver msr) throws SystemException, LogicalException {
        Body body = msr.getBody(input);
        final Collection<Model> models = cacheProcessing.getFromCache(body.getRequest().getIdentifiers(), body.getRequest().getModels(), new SessionContainerImpl(header));
        body.cleanRequest();
        body.setResponse(new StatisticResultImpl<>(models));
        return new RawMessageImpl(header, msr.getByteFromBody(body), new ResultImpl(OperationStatus.OK));
    }

    private RawMessage inLocalCache(MessageHeader header, InputStream input, MessageSenderReceiver msr) throws SystemException, UnsupportedModelException, LogicalException {
        Body body = msr.getBody(input);
        final Collection<Identifier> identifiers = cacheProcessing.inCache(body.getRequest().getIdentifiers(), body.getRequest().getModels());
        body.cleanRequest();
        StatisticResult<? extends Model> response = new StatisticResultImpl<>();
        response.setIdentifiers(identifiers);
        body.setResponse(response);
        return new RawMessageImpl(header, msr.getByteFromBody(body), new ResultImpl(OperationStatus.OK));
    }

    private RawMessage commit(final MessageHeader header, final InputStream input, final MessageSenderReceiver msr) throws LogicalException, SystemException {
        final byte[] message = MessageUtil.readByteBody(input);

        if (server.isCacheManager()) {
            final OperationStatus status = cacheManagerProcessing.commitPhase1AndPhase2(header, msr, message);
            return new RawMessageImpl(header, message, new ResultImpl(status));
        }
        if (server.isCache()) {
            final OperationStatus commitOK = cacheProcessing.commit(new SessionContainerImpl(header), msr);
            return new RawMessageImpl(header, message, new ResultImpl(commitOK));
        }
        return serviceProcessing.sendBroadcastMessageForEachServerAndSumResults(header, message, msr, true);
    }

    private RawMessage update(MessageHeader header, InputStream input, MessageSenderReceiver msr) throws SystemException, LogicalException {
        final byte[] message = MessageUtil.readByteBody(input);

        if (server.isCacheManager(header.getModelName())) {
            final Result result = cacheManagerProcessing.update(header, message, msr);
            return new RawMessageImpl(header, message, result);
        }
        if (server.isCache(header.getModelName())) {
            final Result result = cacheProcessing.update(header, message, msr);
            return new RawMessageImpl(header, message, result);
        }

        return serviceProcessing.sendBroadcastMessageForEachServerAndSumResults(header, message, msr, true);
    }

    private RawMessage rollback(final MessageHeader header, final InputStream input, final MessageSenderReceiver msr) throws LogicalException, SystemException {
        final byte[] message = MessageUtil.readByteBody(input);

        if (server.isCacheManager()) {
            final OperationStatus status = cacheManagerProcessing.rollback(header.getSessionId(), msr, header.getMainSession(), header.getSessions());
            return new RawMessageImpl(header, message, new ResultImpl(status));
        }
        if (server.isCache()) {
            final OperationStatus commitOK = cacheProcessing.rollback(new SessionContainerImpl(header), msr);
            return new RawMessageImpl(header, message, new ResultImpl(commitOK));
        }

        return serviceProcessing.sendBroadcastMessageForEachServerAndSumResults(header, message, msr, true);
    }

    private RawMessage commitPhase1(final MessageHeader header, final InputStream input, final MessageSenderReceiver msr) throws LogicalException, SystemException {

        final byte[] message = MessageUtil.readByteBody(input);

        final String modelName = header.getModelName();
        if (server.isCacheManager(modelName)) {

            return cacheManagerProcessing.commitPhase1(header, message, msr);
        }

        if (server.isCache(modelName) || modelName == null) {
            final OperationStatus commitOK = cacheProcessing.commitPhase1(new SessionContainerImpl(header));
            return new RawMessageImpl(header, message, new ResultImpl(commitOK));
        }

        return serviceProcessing.sendBroadcastMessageForEachServer(header, message, msr, true);

    }

    private RawMessage commitPhase2(final MessageHeader header, final InputStream input, final MessageSenderReceiver msr) throws LogicalException, SystemException {

        final byte[] message = MessageUtil.readByteBody(input);

        final String modelName = header.getModelName();
        if (server.isCacheManager(modelName)) {
            return cacheManagerProcessing.commitPhase2(header, message, msr);
        }

        if (server.isCache(modelName)) {
            final OperationStatus status = cacheProcessing.commitPhase2(new SessionContainerImpl(header));
            return new RawMessageImpl(header, message, new ResultImpl(status));
        }
        final RawMessage result = serviceProcessing.sendBroadcastMessageForEachServer(header, message, msr, true);
        return result;

    }

    private RawMessage inCache(final MessageHeader header, final InputStream input, final MessageSenderReceiver msr) throws LogicalException, SystemException {

        final byte[] message = MessageUtil.readByteBody(input);

        final String modelName = header.getModelName();
        if (server.isCacheManager(modelName)) {
            return cacheManagerProcessing.inCache(header, message, msr);
        }

        if (server.isCache(modelName)) {
            Body body = msr.getBodyFromByte(message);
            final Collection<Identifier> identifuers = cacheProcessing.inCache(body.getRequest().getIdentifiers(), body.getRequest().getModels());
            StatisticResult<?> response = new StatisticResultImpl<>();
            response.setIdentifiers(identifuers);
            body.setResponse(response);
            return new RawMessageImpl(header, msr.getByteFromBody(body), new ResultImpl(OperationStatus.OK));

        }

        return serviceProcessing.sendBroadcastMessageForEachServerAndSumResults(header, message, msr, false);

    }

    private RawMessage put(final MessageHeader header, final InputStream input, final MessageSenderReceiver msr) throws LogicalException, SystemException {

        final byte[] message = MessageUtil.readByteBody(input);

        final String modelName = header.getModelName();
        if (server.isCacheManager(modelName)) {
            RawMessage result = cacheManagerProcessing.put(header, message, msr);
            return result;
        }
        if (server.isCache(modelName)) {
            return cacheProcessing.execInLocalServer(header, message, msr);
        }

        return serviceProcessing.sendBroadcastMessageForAnyServer(header, message, msr, false);

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private RawMessage get(final MessageHeader header, final InputStream input, final MessageSenderReceiver msr) throws SystemException, LogicalException {

        final byte[] message = MessageUtil.readByteBody(input);

        final String modelName = header.getModelName();
        if (server.isCacheManager(modelName) || server.isCache(modelName)) {
            final Body body = msr.getBodyFromByte(message);
            Collection<Model> result = get(header, body.getRequest().getIdentifiers(), msr);
            final Body answerBody = new BodyImpl(new StatisticResultImpl(result));
            return new RawMessageImpl(header, msr.getByteFromBody(answerBody), new ResultImpl(OperationStatus.OK));
        }
        return serviceProcessing.sendBroadcastMessageForAnyServer(header, message, msr, false);

    }

    Collection<Model> get(final MessageHeader header, final Collection<Identifier> identifiers, final MessageSenderReceiver msr) throws SystemException, LogicalException {

        final String modelName = header.getModelName();
        if (server.isCacheManager(modelName)) {
            return cacheManagerProcessing.getModelList(header, identifiers , msr);
        }
        if (server.isCache(modelName)) {
            return cacheProcessing.get(identifiers, new SessionContainerImpl(header));
        }
        return null;
    }

    private RawMessage deleteByCriteria(final MessageHeader header, final InputStream input, final MessageSenderReceiver msr) throws LogicalException, SystemException {

        final byte[] message = MessageUtil.readByteBody(input);
        final String modelName = header.getModelName();
        if (server.isCacheManager(modelName)) {
            LOGGER.debug("deleteByCriteria isCacheManager:" + context.getServerName());
            return cacheManagerProcessing.deleteByCriteria(header, message, msr);
        }
        if (server.isCache(modelName)) {
            LOGGER.debug("deleteByCriteria isCache:" + context.getServerName());
            Body body = msr.getBodyFromByte(message);
            Collection<Identifier> deletedIds = cacheProcessing.deleteByCriteria(body.getRequest().getCriteria(), new SessionContainerImpl(header));
            body.cleanRequest();
            StatisticResult<?> result = new StatisticResultImpl<>();
            result.setIdentifiers(deletedIds);
            body.setResponse(result);
            return new RawMessageImpl(header, msr.getByteFromBody(body), new ResultImpl(OperationStatus.OK));
        }
        LOGGER.debug(" deleteByCriteria broadcast:" + context.getServerName());
        return serviceProcessing.sendBroadcastMessageForAnyServer(header, message, msr, false);

    }

    private RawMessage getModelsByCriteria(final MessageHeader header, final InputStream input, final MessageSenderReceiver msr) throws LogicalException, SystemException {

        final byte[] message = MessageUtil.readByteBody(input);
        return getModelsByCriteria(header, message,  msr);

    }

    RawMessage getModelsByCriteria(final MessageHeader header, final byte[] message, final MessageSenderReceiver msr) throws LogicalException, SystemException {

        final String modelName = header.getModelName();
        if (server.isCacheManager(modelName)) {
            StatisticResult<Model>  result=  cacheManagerProcessing.getByCriteria(header, message, msr);
            return new RawMessageImpl(header, msr.getByteFromBody(new BodyImpl(result)), new ResultImpl(OperationStatus.OK));
        }
        if (server.isCache(modelName)) {
            Body body = msr.getBodyFromByte(message);
            StatisticResult<? extends Model> result = cacheProcessing.getIdentifiersByCriteria(body.getRequest().getCriteria());
            body.setResponse(result);
            return new RawMessageImpl(header, msr.getByteFromBody(body), new ResultImpl(OperationStatus.OK));
        }
        return serviceProcessing.sendBroadcastMessageForAnyServer(header, message, msr, false);

    }

    private RawMessage delete(final MessageHeader header, final InputStream input, final MessageSenderReceiver msr) throws LogicalException, SystemException {

        final byte[] message = MessageUtil.readByteBody(input);
        final String modelName = header.getModelName();
        if (server.isCacheManager(modelName)) {
            return cacheManagerProcessing.delete(header, message, msr);
        }
        if (server.isCache(modelName)) {
            return cacheProcessing.execInLocalServer(header, message, msr);
        }
        return serviceProcessing.sendBroadcastMessageForEachServer(header, message, msr, false);

    }

    private RawMessage startNewChildSession(final InputStream input, MessageHeader header, final MessageSenderReceiver msr) throws SystemException, LogicalException {
        final byte[] sessionFreeMessage = MessageUtil.readByteBody(input);
        if (context.get(ServiceName.SESSION_SERVICE) == null) {
            return serviceProcessing.askСhildrenSequentially(header, sessionFreeMessage, msr, false);
        } else {
            SessionState sessionState = transactSessionAdvisor.startChildSession(header.getSessionId(), null);
            header.setSessionId(sessionState.getSessionId());
            header.setMainSession(sessionState.getMainSession());
            header.getSessions().addAll(sessionState.getChildrenSessions());
            OperationStatus operationResult = null;
            if (SessionResult.OK.equals(sessionState.getResult())) {
                operationResult = OperationStatus.OK;
            } else if (SessionResult.ACCESS_DENIED.equals(sessionState.getResult())) {
                operationResult = OperationStatus.ACCESS_DENIED;
            } else {
                operationResult = OperationStatus.INVALID_SESSION;
            }
            return new RawMessageImpl(header, msr.getEmptyBody(), new ResultImpl(operationResult));
        }

    }

    public void close() {

    }
}
