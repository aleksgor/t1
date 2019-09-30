package com.nomad.server;

import com.nomad.model.ListenerModel;

public interface ServerListener extends Runnable{

    ListenerModel getListener();

    void setListener(ListenerModel listener);

    void setServerContext(ServerContext context);

    void start();

    void stop();

    Status getStatus() ;

    void addStopListener(CommonThreadListener listener);

    void removeStopListener(CommonThreadListener listener);

    String getThreadName();


}
