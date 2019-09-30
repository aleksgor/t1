package com.nomad.client;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.cache.commonclientserver.BodyImpl;
import com.nomad.cache.commonclientserver.FullMessageImpl;
import com.nomad.cache.commonclientserver.RawMessageImpl;
import com.nomad.cache.commonclientserver.RequestImpl;
import com.nomad.cache.commonclientserver.ResultImpl;
import com.nomad.communication.binders.ServerFactory;
import com.nomad.datadefinition.DataDefinitionServiceImpl;
import com.nomad.exception.SystemException;
import com.nomad.message.Body;
import com.nomad.message.FullMessage;
import com.nomad.message.MessageHeader;
import com.nomad.message.MessageSenderReceiver;
import com.nomad.message.MessageSenderReceiverImpl;
import com.nomad.message.OperationStatus;
import com.nomad.message.RawMessage;
import com.nomad.message.Request;
import com.nomad.model.BaseCommand;
import com.nomad.model.CommonClientModel;
import com.nomad.model.CommonClientModelImpl;
import com.nomad.model.Criteria;
import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.model.server.ProtocolType;
import com.nomad.model.update.UpdateRequest;
import com.nomad.utility.SimpleServerContext;
import com.nomad.utility.Validate;

public class SingleCacheClient {

    protected static final Logger LOGGER = LoggerFactory.getLogger(SingleCacheClient.class);

    protected final byte version = 0x1;
    protected final int timeout;
    protected final DataDefinitionServiceImpl dataDefinition;
    private SimpleServerContext context;

    private final RawClientInterface client;

    public SingleCacheClient(final String host, final int port, final int timeout, final ProtocolType pType, Map<String, String> properties) throws SystemException {

        CommonClientModel clientModel = new CommonClientModelImpl();
        clientModel.setHost(host);
        clientModel.setPort(port);
        clientModel.setTimeout(timeout);
        clientModel.setProtocolType(pType);
        if (properties != null) {
            clientModel.getProperties().putAll(properties);
        }
        context = new SimpleServerContext();
        client = ServerFactory.getRawSingleThreadClient(clientModel, context);
        dataDefinition = new DataDefinitionServiceImpl(null, "model.xml", null);
        try {
            dataDefinition.start();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage());
        }
        this.timeout = timeout;
    }

    public SingleCacheClient(final String host, final int port, final ProtocolType pType, Map<String, String> properties) throws SystemException{
        this(host, port, 1000, pType, properties);
    }
    public SingleCacheClient(final String host, final int port,  final ProtocolType pType) throws SystemException {
        this(host, port, 1000, pType, null);
    }
    public SingleCacheClient(final String host, final int port) throws SystemException {
        this(host,  port, 1000, ProtocolType.TCP,null);
    }

    protected FullMessage sendCommand1(final String command, final Collection<Identifier> ids, final Collection<? extends Model> models, final Criteria<? extends Model> criteria,
            final String session) throws SystemException {
        return sendCommand1( command,  ids,  models, criteria,null, session, null,null);
    }
    @SuppressWarnings("unchecked")
    protected FullMessage sendCommand1(final String command, final Collection<Identifier> ids, final Collection<? extends  Model> models, final Criteria<? extends Model> criteria, final Collection<UpdateRequest> updateRequests, final String session, String user, String password)
            throws SystemException {
        String modelName = null;
        if (ids != null && ids.size() > 0) {
            modelName = ids.iterator().next().getModelName();
        }
        if (modelName == null && models != null && models.size() > 0) {
            modelName = models.iterator().next().getModelName();
        }
        if (modelName == null && criteria != null) {
            modelName = criteria.getModelName();
        }
        final MessageHeader header = new MessageHeader();
        header.setCommand(command);
        header.setVersion(version);
        header.setSessionId(session);
        header.setModelName(modelName);
        header.setUserName(user);
        header.setPassword(password);
        final MessageSenderReceiver msr = new MessageSenderReceiverImpl(version, dataDefinition);
        Request request= new RequestImpl((Collection<Model>) models, ids, criteria);
        request.setUpdateRequest(updateRequests);
        Body body = new BodyImpl(request );
        final byte[]  data = msr.getByteFromBody(body);
        RawMessage message= new RawMessageImpl(header,data , new ResultImpl(OperationStatus.OK));
        message=client.sendRawMessage(message);
        body=  msr.getBodyFromByte(message.getMessage());
        final FullMessage result=new FullMessageImpl(message.getHeader(), body);
        result.setResult(message.getResult());
        return result;
    }

    public ModelsResult getModels(final List<Identifier> ids, final String sessionId) throws SystemException {
        try {
            final FullMessage message = sendCommand1(BaseCommand.GET.name(), ids, null, null, sessionId);
            final ModelsResult result = new ModelsResult();
            if (message.getBody().getResponse().getResultList() != null) {
                result.getModels().addAll(message.getBody().getResponse().getResultList());
            }
            fillResult(result, message);
            return result;

        } catch (final Exception e) {
            throw new SystemException(e.getMessage(), e);
        }
    }

    public ModelsResult getModel(final Identifier id, final String sessionId) throws SystemException {
        try {
            final FullMessage message = sendCommand1(BaseCommand.GET.name(), Collections.singletonList(id), null, null, sessionId);
            final ModelsResult result = new ModelsResult();
            if (message.getBody().getResponse() != null && message.getBody().getResponse().getResultList() != null) {
                result.getModels().addAll(message.getBody().getResponse().getResultList());
            }
            fillResult(result, message);
            return result;

        } catch (final Exception e) {
            throw new SystemException(e.getMessage(), e);
        }
    }

    public ModelsResult putModels(final Collection<? extends Model> models, final String sessionId) throws SystemException {
        try {
            final FullMessage message = sendCommand1(BaseCommand.PUT.name(), null, models, null, sessionId);
            final ModelsResult result = new ModelsResult();
            if (message != null && message.getBody() != null && message.getBody().getResponse() != null && message.getBody().getResponse().getResultList() != null) {
                result.getModels().addAll(message.getBody().getResponse().getResultList());
            }
            fillResult(result, message);
            return result;

        } catch (final Exception e) {
            throw new SystemException(e.getMessage(), e);
        }
    }

    public ModelsResult putModel(final Model model, final String sessionId) throws SystemException {
        try {
            final FullMessage message = sendCommand1(BaseCommand.PUT.name(), null, Collections.singletonList(model), null, sessionId);
            final ModelsResult result = new ModelsResult();
            if (message.getBody() != null && message.getBody().getResponse() != null && message.getBody().getResponse().getResultList() != null) {
                result.getModels().addAll(message.getBody().getResponse().getResultList());
            }
            fillResult(result, message);
            return result;

        } catch (final Exception e) {
            throw new SystemException(e.getMessage(), e);
        }
    }
    
    public ModelsResult updateModels( final List<Identifier> ids,final Criteria<? extends Model> criteria, final UpdateRequest request, final String sessionId) throws SystemException {
        try {
            final FullMessage message = sendCommand1(BaseCommand.UPDATE.name(), ids, null , criteria,Collections.singletonList(request),sessionId, null,null);
            final ModelsResult result = new ModelsResult();
            if (message.getBody() != null && message.getBody().getResponse() != null && message.getBody().getResponse().getResultList() != null) {
                result.getModels().addAll(message.getBody().getResponse().getResultList());
            }
            fillResult(result, message);
            return result;

        } catch (final Exception e) {
            throw new SystemException(e.getMessage(), e);
        }
    }

    public VoidResult commit(final String sessionId) throws SystemException {
        Validate.notNull(sessionId, "session must be");
        try {
            final FullMessage message = sendCommand1(BaseCommand.COMMIT.name(), null, null, null, sessionId);
            final VoidResult result= new VoidResult();
            fillResult(result, message);
            return result;

        } catch (final Exception e) {
            throw new SystemException(e.getMessage(), e);
        }
    }

    public VoidResult rollback(final String sessionId) throws SystemException {
        Validate.notNull(sessionId, "session must be");
        try {
            final FullMessage message = sendCommand1(BaseCommand.ROLLBACK.name(), null, null, null, sessionId);
            final VoidResult result= new VoidResult();
            fillResult(result, message);
            return result;

        } catch (final Exception e) {
            throw new SystemException(e.getMessage(), e);
        }
    }

    public IdentifiersResult removeModels(final List<Identifier> ids, final String sessionId) throws SystemException {
        try {
            final FullMessage message = sendCommand1(BaseCommand.DELETE.name(), ids, null, null, sessionId);
            final IdentifiersResult result = new IdentifiersResult();
            if (message.getBody().getResponse() != null && message.getBody().getResponse().getIdentifiers() != null) {
                result.getIdentifiers().addAll(message.getBody().getResponse().getIdentifiers());
            }
            fillResult(result, message);
            return result;

        } catch (final Exception e) {
            throw new SystemException(e.getMessage(), e);
        }
    }
    public IdentifiersResult removeModel(final Identifier id, final String sessionId) throws SystemException {
        try {
            final FullMessage message = sendCommand1(BaseCommand.DELETE.name(), Collections.singletonList(id), null, null, sessionId);
            final IdentifiersResult result = new IdentifiersResult();
            if (message.getBody().getResponse() != null && message.getBody().getResponse().getIdentifiers() != null) {
                result.getIdentifiers().addAll(message.getBody().getResponse().getIdentifiers());
            }
            fillResult(result, message);
            return result;

        } catch (final Exception e) {
            throw new SystemException(e.getMessage(), e);
        }
    }

    public CountResult removeModel(final Criteria<? extends Model> criteria, final String sessionId) throws SystemException {
        try {
            final FullMessage message = sendCommand1(BaseCommand.DELETE_BY_CRITERIA.name(), null, null, criteria, sessionId);
            CountResult result;
            if (message.getBody().getResponse() != null) {
                result = new CountResult(message.getBody().getResponse().getCountAllRow());
            } else {
                result = new CountResult(0);
            }
            fillResult(result, message);
            return result;

        } catch (final Exception e) {
            throw new SystemException(e.getMessage(), e);
        }
    }

    public <T extends Model> CriteriaResult<T> getModels(final Criteria<T> criteria, final String sessionId) throws SystemException {
        try {
            final FullMessage message = sendCommand1(BaseCommand.GET_LIST_ID_BY_CRITERIA.name(), null, null, criteria, sessionId);
            @SuppressWarnings("unchecked")
            final CriteriaResult<T> result = (CriteriaResult<T>) new CriteriaResult<>(message.getBody().getResponse());
            fillResult(result, message);
            return result;
        } catch (final Exception e) {
            throw new SystemException(e.getMessage(), e);
        }
    }

    public SessionResult startSession() throws SystemException {
        try {
            final FullMessage message = sendCommand1(BaseCommand.START_NEW_SESSION.name(), null, null, null, null);
            final SessionResult result =new SessionResult(message.getHeader().getSessionId());
            fillResult(result, message);
            return result;
        } catch (final Exception e) {
            throw new SystemException(e.getMessage(), e);
        }
    }

    public SessionResult startSession(String user, String password) throws SystemException {
        try {
            final FullMessage message = sendCommand1(BaseCommand.START_NEW_SESSION.name(), null,null, null, null, null, user, password);
            final SessionResult result = new SessionResult(message.getHeader().getSessionId());
            fillResult(result, message);
            return result;
        } catch (final Exception e) {
            throw new SystemException(e.getMessage(), e);
        }
    }

    public SessionResult startChildSession(final String sessionId) throws SystemException {
        try {
            final FullMessage message = sendCommand1(BaseCommand.START_CHILD_SESSION.name(), null, null, null, sessionId);
            final SessionResult result =new SessionResult(message.getHeader().getSessionId());
            fillResult(result, message);
            return result;
        } catch (final Exception e) {
            throw new SystemException(e.getMessage(), e);
        }
    }

    public SessionResult closeSession(final String sessionId) throws SystemException {
        try {
            final FullMessage message = sendCommand1(BaseCommand.CLOSE_SESSION.name(), null, null, null, sessionId);
            final SessionResult result =new SessionResult();
            fillResult(result, message);
            return result;
        } catch (final Exception e) {
            throw new SystemException(e.getMessage(), e);
        }
    }

    private void fillResult(final AbstractResult result, final FullMessage message) {
        if (message != null && message.getResult() != null) {
            result.setAnswerCode(message.getResult().getErrorCode());
            result.setAnswerMessage(message.getResult().getMessage());
            result.setOperationStatus(message.getResult().getOperationStatus());
        }
    }

    public void close() {
        if(client!=null){
            client.close();
        }
        if (context != null) {
            context.close();
        }
    }
}
