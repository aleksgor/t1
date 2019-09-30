package com.nomad.cache.commonclientserver;

import java.io.IOException;
import java.util.List;

import com.nomad.exception.SystemException;
import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.message.MessageHeader;
import com.nomad.serializer.Serializer;

public class MessageHeaderSerializer implements Serializer<MessageHeader> {

    @Override
    public void write(final MessageOutputStream out, final MessageHeader data) throws IOException, SystemException {
        out.writeString( data.getCommand());
        out.writeString( data.getModelName());
        out.writeString( data.getSessionId());
        out.writeList(data.getSessions());
        out.writeString( data.getMainSession());
        out.writeString(data.getUserName());
        out.writeString(data.getPassword());

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public MessageHeader read(final MessageInputStream input) throws IOException, SystemException {
        final MessageHeader result = new MessageHeader();
        result.setCommand(input.readString());
        result.setModelName(input.readString());
        result.setSessionId(input.readString());
        result.getSessions().addAll((List)input.readList());
        result.setMainSession(input.readString());
        result.setUserName(input.readString());
        result.setPassword(input.readString());
        return result;
    }

}
