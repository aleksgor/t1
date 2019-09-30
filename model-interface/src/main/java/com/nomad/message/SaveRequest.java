package com.nomad.message;

import java.util.Collection;

import com.nomad.model.Identifier;

public interface SaveRequest extends CommonMessage {


    Collection<String> getSessionIds() ;

    Collection<Identifier> getIdentifiers();

    SaveCommand getCommand();

    long getClientId();

    void setClientId(long clientId);



}
