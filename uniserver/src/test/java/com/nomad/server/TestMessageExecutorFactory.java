package com.nomad.server;

import com.nomad.communication.MessageExecutor;
import com.nomad.communication.MessageExecutorFactory;
import com.nomad.communication.NetworkServer;

public class TestMessageExecutorFactory implements MessageExecutorFactory<StringMessage,StringMessage> ,  MessageExecutor<StringMessage, StringMessage> {

    @Override
    public  MessageExecutor<StringMessage, StringMessage> getMessageExecutor(final ServerContext context, final int workerId, final NetworkServer server) {

        return  new TestMessageExecutorFactory();
    }

    @Override
    public StringMessage execute(final StringMessage message) throws Exception {
        return new StringMessage("Response: "+message.getData());
    }

    @Override
    public void stop() {
        // TODO Auto-generated method stub

    }

}
