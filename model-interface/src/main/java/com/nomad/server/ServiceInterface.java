package com.nomad.server;

import com.nomad.exception.SystemException;

public interface ServiceInterface {

    void start() throws SystemException;

    public void stop();

}
