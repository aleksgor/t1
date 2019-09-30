package com.nomad.core;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.nomad.cache.commonclientserver.BodyImpl;
import com.nomad.cache.commonclientserver.FullMessageImpl;
import com.nomad.cache.commonclientserver.RawMessageImpl;
import com.nomad.client.RawClientPooledInterface;
import com.nomad.exception.EOMException;
import com.nomad.exception.LogicalException;
import com.nomad.exception.SystemException;
import com.nomad.exception.UnsupportedModelException;
import com.nomad.message.FullMessage;
import com.nomad.message.MessageHeader;
import com.nomad.message.MessageSenderReceiver;
import com.nomad.message.OperationStatus;
import com.nomad.message.RawMessage;
import com.nomad.message.Result;
import com.nomad.model.BaseCommand;
import com.nomad.model.Criteria;
import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.model.core.SessionContainer;
import com.nomad.model.criteria.StatisticResult;
import com.nomad.server.ExecutorServiceProvider;
import com.nomad.server.ServerContext;
import com.nomad.server.service.childserver.MessageRequest;
import com.nomad.server.service.childserver.StoreConnectionPool;
import com.nomad.utility.MessageUtil;

public class ProxyCacheProcessing extends CommonProxyProcessing {

    public ProxyCacheProcessing(final ServerContext context, final ExecutorServiceProvider executorServiceProvider) throws SystemException {
        super(context, executorServiceProvider);
    }

    RawMessage execInLocalServer(final MessageHeader header, final InputStream input, final MessageSenderReceiver msr) throws EOMException, SystemException  {

        final byte[] message =  MessageUtil.readByteBody(input);
        return execInLocalServer(header, message, msr);
    }

    RawMessage execInLocalServer(final MessageHeader header, final byte[] message, final MessageSenderReceiver msr) throws SystemException  {
        LOGGER.info("Execute in Server {} header:{}", serverName, header);
        final StoreConnectionPool servers = childrenServerService.getLocalServer();
        if (servers == null) {
            LOGGER.error("Error in local : {} does not supported", header);
            return null;
        }
        final RawClientPooledInterface connection = servers.getObject();
        RawMessage result = null;
        try {
            result = connection.sendRawMessage(new RawMessageImpl(header, message));
        } finally {
            connection.freeObject();
        }

        return result;
    }

    Collection<Model> put(Collection<Model> models, SessionContainer sessions) throws LogicalException, SystemException {
        RawClientPooledInterface client = childrenServerService.getLocalServer().getObject();
        try {
            return client.getProcessing().put(models, sessions);
        } finally {
            client.freeObject();
        }
    }

    OperationStatus commitPhase1(SessionContainer sessions) {

        OperationStatus operationStatus;
        RawClientPooledInterface client = childrenServerService.getLocalServer().getObject();
        try {
            client.getProcessing().commitPhase1(sessions);
            operationStatus = OperationStatus.OK;
        } catch (SystemException e) {
            operationStatus = OperationStatus.ERROR;
        } finally {
            client.freeObject();
        }
        return operationStatus;
    }

    // cache
    OperationStatus commitPhase2(final SessionContainer sessions) {
        OperationStatus operationStatus = OperationStatus.ERROR;
        RawClientPooledInterface client = childrenServerService.getLocalServer().getObject();
        try {
            client.getProcessing().commitPhase2(sessions);
            operationStatus = OperationStatus.OK;
        } catch (SystemException e) {
            ;
        } finally {
            client.freeObject();
        }
        return operationStatus;
    }

    FullMessage execInLocalServerAndGetFullMessage(final MessageHeader header, final byte[] message, final MessageSenderReceiver msr) throws SystemException  {
        final StoreConnectionPool servers = childrenServerService.getLocalServer();
        if (servers == null) {
            LOGGER.error("Error in local : {} does not supported", header);
            return null;
        }
        final RawClientPooledInterface client = servers.getObject();
        FullMessage result = null;
        try {
            final RawMessage rawMessage = client.sendRawMessage(new RawMessageImpl(header, message));
            result = new FullMessageImpl(rawMessage.getHeader(), msr.getBodyFromByte(rawMessage.getMessage()), rawMessage.getResult());
        } finally {
            client.freeObject();
        }
        return result;
    }

    OperationStatus rollback(final SessionContainer sessions) {
        OperationStatus operationStatus = OperationStatus.ERROR;
        RawClientPooledInterface client = childrenServerService.getLocalServer().getObject();
        try {
            client.getProcessing().rollback(sessions);
            operationStatus = OperationStatus.OK;
        } finally {
            client.freeObject();
        }
        return operationStatus;
    }

    Collection<Identifier> inCache(Collection<Identifier> identifiers, Collection<Model> models) throws UnsupportedModelException, SystemException, LogicalException {
        RawClientPooledInterface client = childrenServerService.getLocalServer().getObject();
        try {
            return client.getProcessing().inCache(identifiers, models);
        } finally {
            client.freeObject();
        }
    }

    @SuppressWarnings("unchecked")
    protected Collection<Identifier> deleteByCriteria(Criteria<? extends Model> criteria, SessionContainer sessions) throws SystemException, LogicalException {
        final StoreConnectionPool server = childrenServerService.getLocalServer();
        if (server == null) {
            throw new UnsupportedModelException();
        }

        RawClientPooledInterface client = server.getObject();
        try {
            StatisticResult<?> result = client.getProcessing().getIdentifiers(criteria);
            Collection<Identifier> ids = result.getIdentifiers();
            if(ids==null){
                ids= new ArrayList<>();
            }
            if(result.getResultList()!= null && !result.getResultList().isEmpty()){
                for (Model model : result.getResultList()) {
                    ids.add(model.getIdentifier());
                }
            }
            if (ids != null && !ids.isEmpty()) {
                Collection<Identifier> deletedIds = client.getProcessing().delete(result.getIdentifiers(),sessions);
                return deletedIds;
            }
        } finally {
            client.freeObject();
        }
        return Collections.EMPTY_LIST;
    }

    OperationStatus rollback(final SessionContainer sessions, final MessageSenderReceiver msr)
            throws  SystemException{

        final StoreConnectionPool server = childrenServerService.getLocalServer();
        final MessageHeader header = new MessageHeader();
        header.setCommand(BaseCommand.ROLLBACK.name());
        header.setSessionId(sessions.getSessionId());
        MessageUtil.applySessions(header, sessions);

        final RawMessage getMessage = new RawMessageImpl(header, msr.getByteFromBody(new BodyImpl()));

        final MessageRequest request = new MessageRequest(getMessage, server);
        final RawMessage answer = request.executeMessage();
        return answer.getResult().getOperationStatus();
    }

    OperationStatus commit(SessionContainer sessions, final MessageSenderReceiver msr) throws SystemException  {
        final OperationStatus operationStatus = commitPhase1(sessions);
        if (OperationStatus.OK.equals(operationStatus)) {
            return commitPhase2(sessions);
        } else {
            rollback(sessions, msr);
        }

        return operationStatus;
    }

    public Result update(final MessageHeader header, final byte[] message, final MessageSenderReceiver msr) throws SystemException {

        final StoreConnectionPool servers = childrenServerService.getLocalServer();
        if (servers == null) {
            LOGGER.error("Error in local : {} does not supported", header);
            return null;
        }
        final RawClientPooledInterface connection = servers.getObject();
        try {
            RawMessage result = connection.sendRawMessage(new RawMessageImpl(header, message));
            return result.getResult();

        } finally {
            connection.freeObject();
        }

    }

    StatisticResult<? extends Model> getIdentifiersByCriteria(Criteria<? extends Model> criteria) throws UnsupportedModelException, SystemException, LogicalException {
        RawClientPooledInterface client = childrenServerService.getLocalServer().getObject();
        try {
            return client.getProcessing().getIdentifiers(criteria);
        } finally {
            client.freeObject();
        }
    }

    public Collection<Model> getModels(Collection<Identifier> identifiers, Collection<Model> models, SessionContainer sessions)
            throws LogicalException, SystemException {
        RawClientPooledInterface client = childrenServerService.getLocalServer().getObject();
        try {
            return client.getProcessing().get(getIdentifiers(identifiers, models), sessions);
        } finally {
            client.freeObject();
        }
    }

    public Collection<Identifier> deleteFromCache(Collection<Identifier> identifiers,  SessionContainer sessions) throws LogicalException, SystemException {
        RawClientPooledInterface client = childrenServerService.getLocalServer().getObject();
        try {
            return client.getProcessing().delete(identifiers,  sessions);
        } finally {
            client.freeObject();
        }
    }

    public Collection<Model> getFromCache(Collection<Identifier> identifiers, Collection<Model> models, SessionContainer sessions) throws LogicalException, SystemException {
        RawClientPooledInterface client = childrenServerService.getLocalServer().getObject();
        try {
            return client.getProcessing().getFromCache(identifiers, models, sessions);
        } finally {
            client.freeObject();
        }
    }

    public Collection<Model> get(Collection<Identifier> identifiers, SessionContainer sessions) throws LogicalException, SystemException {
        RawClientPooledInterface client = childrenServerService.getLocalServer().getObject();
        try {
            return client.getProcessing().get(identifiers,  sessions);
        } finally {
            client.freeObject();
        }
    }

}
