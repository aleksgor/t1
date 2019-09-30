package com.nomad.communication;

import com.nomad.server.Status;

public interface NetworkServer extends Runnable {

    void close();

    Status getStatus();

}
