package com.nomad.server.service.session.synchronizesession;

import com.nomad.exception.SystemException;
import com.nomad.model.CommonClientModel;
import com.nomad.model.session.SessionAnswer;
import com.nomad.model.session.SessionMessage;
import com.nomad.server.ServerContext;
import com.nomad.server.service.common.AbstractConnectionImpl;

public class SynchronizeSessionClient extends AbstractConnectionImpl<SessionAnswer,SessionMessage> {

    public SynchronizeSessionClient(final CommonClientModel clientModel, final ServerContext context) throws SystemException  {
        super(clientModel, context);
    }


}
