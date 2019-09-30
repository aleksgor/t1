package com.nomad.server.service;

import java.io.File;

import com.nomad.exception.SystemException;
import com.nomad.message.ManagementMessage;
import com.nomad.model.CommonClientModel;
import com.nomad.server.ServiceInterface;

public interface ManagementService extends ServiceInterface {
    ManagementClientConnectionPool getClientPool(CommonClientModel colleague);

    ManagementMessage getCommand(final String command, final File f) throws SystemException;

}
