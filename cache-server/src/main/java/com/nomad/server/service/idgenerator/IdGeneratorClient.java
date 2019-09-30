package com.nomad.server.service.idgenerator;

import com.nomad.exception.SystemException;
import com.nomad.model.CommonClientModel;
import com.nomad.model.idgenerator.IdGeneratorMessage;
import com.nomad.server.ServerContext;
import com.nomad.server.service.common.AbstractConnectionImpl;

public class IdGeneratorClient extends AbstractConnectionImpl<IdGeneratorMessage, IdGeneratorMessage> {


    public IdGeneratorClient(final CommonClientModel clientModel, final ServerContext context) throws SystemException {
        super(clientModel, context);
    }





}
