package com.nomad.server.service;

import com.nomad.client.ClientPooledInterface;
import com.nomad.exception.SystemException;
import com.nomad.message.ManagementMessage;
import com.nomad.server.ServiceInterface;

public interface ManagementClientConnectionPool extends ServiceInterface{

    ClientPooledInterface<ManagementMessage, ManagementMessage> getClient() throws SystemException;
}
