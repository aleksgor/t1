package com.nomad.core;

import java.util.Collection;
import java.util.List;


import com.nomad.InternalTransactDataStore;
import com.nomad.cache.commonclientserver.BodyImpl;
import com.nomad.cache.commonclientserver.FullMessageImpl;
import com.nomad.cache.commonclientserver.ResultImpl;
import com.nomad.exception.BlockException;
import com.nomad.exception.LogicalException;
import com.nomad.exception.SystemException;
import com.nomad.exception.UnsupportedModelException;
import com.nomad.message.Body;
import com.nomad.message.FullMessage;
import com.nomad.message.MessageHeader;
import com.nomad.message.OperationStatus;
import com.nomad.message.Result;
import com.nomad.model.BaseCommand;
import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.model.ServiceCommand;
import com.nomad.model.criteria.StatisticResultImpl;
import com.nomad.server.CommandPlugin;
import com.nomad.server.ServerContext;
import com.nomad.server.service.commandplugin.CommandPluginService;
import com.nomad.server.service.storemodelservice.StoreModelServiceImpl;
import com.nomad.utility.MessageUtil;

public class ObjectProcessingImpl extends CacheExecutor {

    private volatile CommandPluginService pluginService;
    private volatile StoreModelServiceImpl server;

    public ObjectProcessingImpl(final ServerContext context) {
        store = (InternalTransactDataStore) context.get(ServerContext.ServiceName.INTERNAL_TRANSACT_DATA_STORE);
        pluginService = (CommandPluginService) context.get(ServerContext.ServiceName.OBJECT_PLUGINS);
        server = (StoreModelServiceImpl) context.get(ServerContext.ServiceName.STORE_MODEL_SERVICE);
    }

    public FullMessage execMessage(final FullMessage fullMessage) {
        final Body message = fullMessage.getBody();
        if (message == null) {
            LOGGER.error("the message is NULL!");
        }
        final MessageHeader header = fullMessage.getHeader();
        normalizeHeader(header);
        LOGGER.debug("Server:{}, header: {} , message {} ", new Object[] {server.getServerModel().getServerName() , header, message});

        final String sessionId = header.getSessionId();
        try {
            try {
                BaseCommand command = null;
                command = BaseCommand.valueOf(header.getCommand());
                return executeBaseCommand(header, command, message, sessionId);
            } catch (final IllegalArgumentException e) {
                // ignore
            }

            try {
                ServiceCommand serviceCommand = null;
                serviceCommand = ServiceCommand.valueOf(header.getCommand());
                return execServiceCommand(header, serviceCommand, message);

            } catch (final IllegalArgumentException e) {
                if (pluginService != null) {
                    final CommandPlugin plugin = pluginService.getPlugin(header.getCommand().toString());

                    if (plugin != null) {
                        LOGGER.debug("Found plugin for command:{}", header.getCommand());
                        try {
                            return plugin.executeMessage(fullMessage);
                        } finally {
                            if (plugin != null) {
                                plugin.freeObject();
                            }
                        }
                    } else {
                        LOGGER.warn("Invalid operation name:" + header);
                        final Result res = new ResultImpl(OperationStatus.INVALID_OPERATION_NAME, "header:" + header);
                        return new FullMessageImpl(header, new BodyImpl(), res);

                    }
                } else {
                    LOGGER.warn("Invalid operation name:" + header);

                    final Result res = new ResultImpl(OperationStatus.INVALID_OPERATION_NAME, "header:" + header);
                    return new FullMessageImpl(header, new BodyImpl(), res);

                }

            }

        } catch (final UnsupportedModelException e) {
            LOGGER.error("Server:" + server.getServerModel().getServerName() + " UnsupportedModelException:" + message + ":" + e.getMessage(), e);
            final Result res = new ResultImpl(OperationStatus.UNSUPPORTED_MODEL_NAME, "Model:" + header.getModelName() + " does not supported! on server:"
                    + server.getServerModel().getServerName());
            return new FullMessageImpl(header, new BodyImpl(), res);
        } catch (final BlockException e) {
            return new FullMessageImpl(header, new BodyImpl(), new ResultImpl(OperationStatus.BLOCKED, e.getBlockedSessions()));
        } catch (final LogicalException e) {
            return new FullMessageImpl(header, new BodyImpl(), new ResultImpl(e));
        } catch (final Throwable e) {
            LOGGER.error("error:" + e.getMessage(), e);
            final Result res = new ResultImpl(OperationStatus.ERROR, e.getMessage());
            return new FullMessageImpl(header, new BodyImpl(), res);
        }

    }

    private void normalizeHeader(final MessageHeader header){
        if(header.getSessionId()!=null){
            if (header.getMainSession()==null){
                header.setMainSession(header.getSessionId());
            }
            if (header.getSessions().size()==0){
                header.getSessions().add(header.getSessionId());
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private FullMessage executeBaseCommand(final MessageHeader header, final BaseCommand command, final Body message, final String sessionId) throws UnsupportedModelException,
    BlockException, SystemException, LogicalException {
        Collection<Model> models = null;
        Collection<Identifier> ids;
        final int commandId = command.getCommandIndex();

        switch (commandId) {
        case 2: // Get:
            models= get( message.getRequest().getIdentifiers(), new SessionContainerImpl(header));
            return new FullMessageImpl(header, new BodyImpl(new StatisticResultImpl(models)), new ResultImpl(OperationStatus.OK));
        case 3:// Put:
            models=message.getRequest().getModels();
            if (models != null) {
                models=put(message.getRequest().getModels(), new SessionContainerImpl(header));
                return new FullMessageImpl(header, new BodyImpl(new StatisticResultImpl(models)), new ResultImpl(OperationStatus.OK));
            } else {
                LOGGER.warn("put: nothing to do:{}", message);
                return new FullMessageImpl(header, message, new ResultImpl(OperationStatus.EMPTY_ANSWER));
            }
        case 4:// Delete:
            
            ids = delete(message.getRequest().getIdentifiers(), new SessionContainerImpl(header) );
            return new FullMessageImpl(header, new BodyImpl(MessageUtil.getStatisticResult(ids)), new ResultImpl(OperationStatus.OK));
        case 1: // StartNewSession:
            return new FullMessageImpl(header, new BodyImpl(), new ResultImpl(OperationStatus.OK));
        case 5: // Commit:
            commit(new SessionContainerImpl(header));
            return new FullMessageImpl(header, new BodyImpl(), new ResultImpl(OperationStatus.OK));
        case 6: // Rollback:
            rollback(new SessionContainerImpl(header));
            return new FullMessageImpl(header, new BodyImpl(), new ResultImpl(OperationStatus.OK));
        case 7: // CloseSession:
            closeSession(new SessionContainerImpl(header));
            return new FullMessageImpl(header, new BodyImpl(), new ResultImpl(OperationStatus.OK));
        case 8: // Update:
            update(message.getRequest().getUpdateRequest(), message.getRequest().getIdentifiers(), new SessionContainerImpl(header));
            return new FullMessageImpl(header, new BodyImpl(), new ResultImpl(OperationStatus.OK));
        case 9: // InCache:
            return inCache(message,  header);
        case 10: // GetList:
            return new FullMessageImpl(header, new BodyImpl(getIdentifiers(message.getRequest().getCriteria())), new ResultImpl(OperationStatus.OK));

        }
        LOGGER.warn("Invalid operation name:" + header);
        final Result res = new ResultImpl(OperationStatus.INVALID_OPERATION_NAME, "header:" + header);
        return new FullMessageImpl(header, new BodyImpl(), res);
    }






    @SuppressWarnings({ "unchecked", "rawtypes" })
    private FullMessage execServiceCommand(final MessageHeader header, final ServiceCommand serviceCommand, final Body message) throws LogicalException,
    SystemException {
        Collection< Model> models;
        Collection<Identifier> ids;
        final int commandId = serviceCommand.getCommandIndex();
        switch (commandId) {
        case 21: // GetIdsByCriteria:
            message.setResponse(getIdentifiers(message.getRequest().getCriteria()));
            message.cleanRequest();
            return new FullMessageImpl(header, message, new ResultImpl(OperationStatus.OK));
        case 19: // unblock:
            unblock(new SessionContainerImpl(header));
            return new FullMessageImpl(header, new BodyImpl(), new ResultImpl(OperationStatus.OK));
        case 18: // Commitphase2:
            commitPhase2(new SessionContainerImpl(header));
            return new FullMessageImpl(header, new BodyImpl(), new ResultImpl(OperationStatus.OK));
        case 17: // Commitphase1:
            commitPhase1(new SessionContainerImpl(header));
            return new FullMessageImpl(header, new BodyImpl(), new ResultImpl(OperationStatus.OK));
        case 16: // Block:
            
            Collection<Identifier> blockIds =block( message.getRequest().getIdentifiers(),message.getRequest().getModels(),new SessionContainerImpl(header));
            if (blockIds.isEmpty()) {
                return new FullMessageImpl(header, new BodyImpl(), new ResultImpl(OperationStatus.OK));
            } else {
                final ResultImpl res = new ResultImpl();
                res.setStatus(OperationStatus.BLOCKED);
                message.setResponse(MessageUtil.getStatisticResult((List<Identifier>) blockIds));
                return new FullMessageImpl(header, message, res);
            }

        case 15: // RollBackInCache:
            rollback(new SessionContainerImpl(header));
            return new FullMessageImpl(header, new BodyImpl(), new ResultImpl(OperationStatus.OK));
        case 14: // DeleteFromCache:
            ids = deleteFromCache(message.getRequest().getIdentifiers(),message.getRequest().getModels(), new SessionContainerImpl(header));
            return new FullMessageImpl(header, new BodyImpl(MessageUtil.getStatisticResult(ids)), new ResultImpl(OperationStatus.OK));
        case 13:// PutIntoCache:
            models = putIntoCache(message.getRequest().getModels(), new SessionContainerImpl(header));
            return new FullMessageImpl(header, new BodyImpl(new StatisticResultImpl(models)), new ResultImpl(OperationStatus.OK));
        case 12: // GetFromCache:
            models = getFromCache(message.getRequest().getIdentifiers(),message.getRequest().getModels(), new SessionContainerImpl(header));
            return new FullMessageImpl(header, new BodyImpl(new StatisticResultImpl(models)), new ResultImpl(OperationStatus.OK));
        case 11: // In Local Cache:
            return inCache(message, header);
        case 23: // In Local Cache info:
            return cacheInfo(message, header);
        }
        LOGGER.warn("Invalid operation name:" + header);
        final Result res = new ResultImpl(OperationStatus.INVALID_OPERATION_NAME, "header:" + header);
        return new FullMessageImpl(header, new BodyImpl(), res);

    }



    private FullMessage inCache(final Body message,final MessageHeader header) throws SystemException, UnsupportedModelException, LogicalException {
        LOGGER.debug("In cache?  Header:{} body:{}", header, message);
        Collection <Identifier> ids = inCache(message.getRequest().getIdentifiers(), message.getRequest().getModels());
        return new FullMessageImpl(header, new BodyImpl(MessageUtil.getStatisticResult(ids)), new ResultImpl(OperationStatus.OK));
    }

    private FullMessage cacheInfo(final Body message,final MessageHeader header) throws SystemException {
        final double freeMemory = Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory();
        final double busyMemory = Runtime.getRuntime().maxMemory()-freeMemory;
        final double busyPart=(busyMemory/freeMemory)*1000;
        header.setMainSession(""+busyPart);
        return new FullMessageImpl(header, message, new ResultImpl(OperationStatus.OK));

    }

    
}
