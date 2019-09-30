package com.nomad.client;

import com.nomad.server.processing.ObjectProcessing;
import com.nomad.utility.PooledObject;

public interface RawClientPooledInterface extends PooledObject, RawClientInterface {

    void setShouldClose(boolean b);

    ObjectProcessing getProcessing();

}
