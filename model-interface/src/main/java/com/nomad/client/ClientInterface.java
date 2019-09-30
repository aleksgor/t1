package com.nomad.client;

import com.nomad.exception.SystemException;
import com.nomad.message.CommonAnswer;
import com.nomad.message.CommonMessage;

public interface ClientInterface<T extends CommonMessage, K extends CommonAnswer> {

	K sendMessage(final T message) throws SystemException;

	void close();

	long getMessageSize();
}
