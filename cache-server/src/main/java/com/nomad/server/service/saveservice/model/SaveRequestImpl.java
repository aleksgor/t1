package com.nomad.server.service.saveservice.model;

import java.util.ArrayList;
import java.util.Collection;

import com.nomad.message.SaveCommand;
import com.nomad.message.SaveRequest;
import com.nomad.model.Identifier;

public class SaveRequestImpl implements SaveRequest{

    private final Collection<String> sessionIds= new ArrayList<>();
    private final Collection<Identifier> identifiers= new ArrayList<>();
    private final SaveCommand command;
    private long clientId;

    public SaveRequestImpl(final SaveCommand command, final long clientId) {
        this.command=command;
        this.clientId=clientId;
    }

    public SaveRequestImpl(final Collection<String> sessionIds, final Collection<Identifier> identifiers,final SaveCommand command, final long clientId) {
        this(command,clientId);
        this.sessionIds.addAll(sessionIds);
        this.identifiers.addAll(identifiers) ;

    }

    @Override
    public long getClientId() {
        return clientId;
    }

    @Override
    public void setClientId(final long clientId) {
        this.clientId = clientId;
    }

    @Override
    public Collection<String> getSessionIds() {
        return sessionIds;
    }

    @Override
    public Collection<Identifier> getIdentifiers() {
        return identifiers;
    }

    @Override
    public SaveCommand getCommand() {
        return command;
    }


    @Override
    public String toString() {
        return "SaveRequestImpl [sessionIds=" + sessionIds + ", identifiers=" + identifiers + ", command=" + command + "]";
    }

}
