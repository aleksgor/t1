package com.nomad.client;

import com.nomad.exception.SystemException;
import com.nomad.message.RawMessage;

public interface RawClientInterface {

	RawMessage sendRawMessage(RawMessage message) throws SystemException;

	void close();

	long getMessageSize();
}
