package com.nomad.client;

import com.nomad.message.CommonAnswer;
import com.nomad.message.CommonMessage;
import com.nomad.utility.PooledObject;

public interface ClientPooledInterface<K extends CommonMessage, T extends CommonAnswer> extends PooledObject, ClientInterface<K, T> {

    void setShouldClose(boolean b);

}
