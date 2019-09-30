package com.nomad.server.service.saveservice;

import com.nomad.message.SaveRequest;
import com.nomad.message.SaveResult;
import com.nomad.server.service.common.AbstractCallable;
import com.nomad.server.service.saveservice.model.SaveResultImpl;
import com.nomad.utility.NetworkConnectionPool;

public class SaveServiceCallable extends AbstractCallable<SaveResult, SaveRequest> {


    public SaveServiceCallable(final NetworkConnectionPool<SaveResult, SaveRequest> connectPool, final SaveRequest message) {
        super(connectPool, message);
    }

    @Override
    public SaveResult getErrorMessage(final SaveRequest message) {
        final SaveResult result= new SaveResultImpl(message.getIdentifiers());
        result.setResultCode(-1);
        return result;
    }

}
