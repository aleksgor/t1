package com.nomad.server;

import java.util.List;
import java.util.Properties;

import com.nomad.exception.LogicalException;
import com.nomad.exception.SystemException;
import com.nomad.message.FullMessage;
import com.nomad.utility.PooledObject;

public interface CommandPlugin extends PooledObject{

    void init(ServerContext context, Properties property) throws SystemException, LogicalException;

    List<String> getCommands();

    FullMessage executeMessage(FullMessage message) throws SystemException;

    void close();

}
