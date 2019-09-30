package com.nomad.server.processing;

import java.io.InputStream;

import com.nomad.exception.LogicalException;
import com.nomad.exception.SystemException;
import com.nomad.message.MessageHeader;
import com.nomad.message.MessageSenderReceiver;
import com.nomad.message.RawMessage;

public interface ProxyProcessingInterface {
     RawMessage execMessage(final MessageHeader header, final InputStream input, final MessageSenderReceiver msr)  throws SystemException, LogicalException;

}
