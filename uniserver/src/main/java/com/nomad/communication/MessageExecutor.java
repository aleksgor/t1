package com.nomad.communication;

import com.nomad.message.CommonAnswer;
import com.nomad.message.CommonMessage;

public interface MessageExecutor<K extends CommonMessage, T extends CommonAnswer> {

    T execute(K message)  throws Exception;
    void stop();
}
