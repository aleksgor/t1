package com.nomad.client;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.exception.SystemException;
import com.nomad.message.FullMessage;
import com.nomad.model.BaseCommand;
import com.nomad.model.Criteria;
import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.model.ServiceCommand;
import com.nomad.model.server.ProtocolType;

public class SimpleCacheClient extends SingleCacheClient {

    protected static final Logger LOGGER = LoggerFactory.getLogger(SimpleCacheClient.class);

    public SimpleCacheClient(final String host, final int port, final int timeout, final ProtocolType protocolType)  throws SystemException {
        super(host, port, timeout, protocolType, null);
    }
    public SimpleCacheClient(final String host, final int port) throws SystemException {
        super(host, port,  ProtocolType.TCP);
    }

    public SimpleCacheClient(final String host, final int port, final ProtocolType protocolType)  throws SystemException{
        super(host, port, 1000, protocolType, null);
    }

    public FullMessage sendCommand(final String command, final List<Identifier> ids, final List<Model> models, final Criteria<? extends Model> criteria, final String session)
            throws SystemException {
        return super.sendCommand1(command, ids, models, criteria, session);
    }

    public FullMessage sendCommandForId(final BaseCommand command, final Identifier id) throws SystemException {
        return sendCommand(command.toString(), Collections.singletonList(id), null, null);
    }

    public FullMessage sendCommandForId(final ServiceCommand command, final Identifier id) throws SystemException {
        return sendCommand(command.toString(), Collections.singletonList(id), null, null);
    }

    public FullMessage sendCommandForId(final BaseCommand command, final Identifier id, final String session) throws SystemException {
        return sendCommand(command.toString(), Collections.singletonList(id), null, session);
    }

    public FullMessage sendCommandForId(final ServiceCommand command, final Identifier id, final String session) throws SystemException {
        return sendCommand(command.toString(), Collections.singletonList(id), null, session);
    }
    public FullMessage sendCommandForId(final ServiceCommand command, final Collection<Identifier> ids, final String session) throws SystemException {
        return sendCommand(command.toString(), ids, null, session);
    }

    public FullMessage sendCommandForId(final String command, final Identifier id, final String session) throws SystemException {
        return sendCommand(command, Collections.singletonList(id), null, session);
    }

    public FullMessage sendCommandForId(final String command, final Identifier id) throws SystemException {
        return sendCommand(command, Collections.singletonList(id), null, null);
    }

    public FullMessage sendCommand(final BaseCommand command, final String session) throws SystemException {
        return sendCommand(command.toString(), null, null, session);
    }

    public FullMessage sendCommand(final ServiceCommand command, final String session) throws SystemException {
        return sendCommand(command.toString(), null, null, session);
    }

    public FullMessage sendCommand(final BaseCommand command) throws SystemException {
        return sendCommand(command.toString(), null, null, null);
    }

    public FullMessage sendCommand(final String command, final String session) throws SystemException {
        return sendCommand(command, null, null, session);
    }

    public FullMessage sendCommandForId(final BaseCommand command, final Collection<Identifier> ids, final String session) throws SystemException {
        return sendCommand(command.toString(), ids, null, session);
    }

    public FullMessage sendCommandForId(final String command, final Collection<Identifier> ids, final String session) throws SystemException {
        return sendCommand(command, ids, null, session);
    }

    public FullMessage sendCommandForModel(final BaseCommand command, final Model model, final String session) throws SystemException {
        return sendCommand(command.toString(), null, Collections.singletonList(model), session);
    }

    public FullMessage sendCommandForModel(final BaseCommand command, final Model model) throws SystemException {
        return sendCommand(command.toString(), null, Collections.singletonList(model), null);
    }

    public FullMessage sendCommandForModel(final ServiceCommand command, final Model model, final String session) throws SystemException {
        return sendCommand(command.toString(), null, Collections.singletonList(model), session);
    }

    public FullMessage sendCommandForModel(final String command, final Model model, final String session) throws SystemException {
        return sendCommand(command, null, Collections.singletonList(model), session);
    }

    public FullMessage sendCommandForModel(final BaseCommand command, final Collection<Model> models, final String session) throws SystemException {
        return sendCommand(command.toString(), null, models, session);
    }

    public FullMessage sendCommandForModel(final String command, final Collection<Model> models, final String session) throws SystemException {
        return sendCommand(command, null, models, session);
    }

    public FullMessage sendCommand(final String command, final Collection<Identifier> ids, final Collection<Model> models, final String session) throws SystemException {
        return sendCommand1(command, ids, models, null, session);
    }

    public FullMessage sendCommand(final BaseCommand command, final Criteria<? extends Model> criteria, final String session) throws SystemException {
        return sendCommand1(command.toString(), null, null, criteria, session);
    }

    public FullMessage sendCommand(final BaseCommand command, final Criteria<? extends Model> criteria) throws SystemException {
        return sendCommand1(command.toString(), null, null, criteria, null);
    }
    public FullMessage sendCommand(final ServiceCommand command, final Criteria<? extends Model> criteria) throws SystemException {
        return sendCommand1(command.toString(), null, null, criteria, null);
    }

}
