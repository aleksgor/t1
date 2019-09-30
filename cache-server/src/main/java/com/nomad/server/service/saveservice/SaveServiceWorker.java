package com.nomad.server.service.saveservice;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.communication.MessageExecutor;
import com.nomad.message.SaveRequest;
import com.nomad.message.SaveResult;
import com.nomad.model.Identifier;
import com.nomad.server.ServerContext;
import com.nomad.server.service.saveservice.model.SaveResultImpl;

public class SaveServiceWorker implements MessageExecutor<SaveRequest, SaveResult> {
    protected static Logger LOGGER = LoggerFactory.getLogger(SaveServiceWorker.class);
    private volatile SaveServerStore store;

    public SaveServiceWorker(final SaveServerStore store, final ServerContext context) {
        this.store = store;
    }

    @Override
    public SaveResult execute(final SaveRequest message) {

        SaveResult result = new SaveResultImpl(Collections.<Identifier> emptyList());

        try {
            switch (message.getCommand()) {
            case RELEASE:
                store.cleanSession(message.getSessionIds());
                break;
            case TRY_BLOCK:
                result = store.check(message);
                break;
            case PRECHECK:
                result = store.preCheck(message);
                break;
            case TEST:
                result = new SaveResultImpl(null, 0);
                break;

            }
        } catch (Throwable e) {
            result = new SaveResultImpl(Collections.<Identifier> emptyList(), -1);
        }
        LOGGER.debug("execute message:{} return:{} ", message, result);
        return result;
    }

    @Override
    public void stop() {
        // TODO Auto-generated method stub

    }

}