package com.nomad.server.service.saveservice;

import com.nomad.exception.SystemException;
import com.nomad.message.SaveRequest;
import com.nomad.message.SaveResult;
import com.nomad.model.CommonClientModel;
import com.nomad.server.ServerContext;
import com.nomad.server.service.common.AbstractConnectionImpl;

public class SaveServiceClient extends AbstractConnectionImpl<SaveResult,SaveRequest> {


    public SaveServiceClient(final CommonClientModel clientModel,  final ServerContext context) throws SystemException  {
        super(clientModel, context);
    }





}
