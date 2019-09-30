package com.nomad.cache.commonclientserver.session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.nomad.exception.SystemException;
import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.model.session.SessionCommand;
import com.nomad.serializer.Serializer;

public class SessionMessageSerializer implements Serializer<SessionMessageImpl> {

    @Override
    public void write(final MessageOutputStream out, final SessionMessageImpl data) throws IOException,SystemException {

        out.writeString(data.getOperation());
        out.writeList(new ArrayList<>(data.getSessionIds()));
        out.writeString(data.getModelName());
        if (data.getSessionCommand() == null) {
            out.writeString(null);
        } else {
            out.writeString(data.getSessionCommand().toString());
        }
        out.writeString(data.getMainSession());
        out.writeString(data.getUserName());
        out.writeString(data.getPassword());

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public SessionMessageImpl read(final MessageInputStream input) throws IOException, SystemException {

        final SessionMessageImpl result = new SessionMessageImpl();

        result.setOperation(input.readString());
        result.getSessionIds().addAll((List)input.readList());
        result.setModelName(input.readString());
        final String command = input.readString();
        if (command != null) {
            result.setSessionCommand(SessionCommand.valueOf(command));
        }
        result.setMainSession(input.readString());
        result.setUserName(input.readString());
        result.setPassword(input.readString());

        return result;
    }

}
