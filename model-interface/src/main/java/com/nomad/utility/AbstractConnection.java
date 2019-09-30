package com.nomad.utility;

import com.nomad.exception.SystemException;
import com.nomad.message.CommonAnswer;
import com.nomad.message.CommonMessage;

public interface AbstractConnection<T extends CommonAnswer, K extends CommonMessage> extends PooledObject {

    T sendMessage(final K message) throws SystemException;

    @Override
    void closeObject();

    long getSize();

    void resetSize();

}
