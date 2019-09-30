package com.nomad.model;

import java.io.IOException;
import java.util.List;

import com.nomad.exception.SystemException;
import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.serializer.Serializer;

public class ConnectModelSerializer implements Serializer<ConnectModelImpl> {


    @Override
    public void write(final MessageOutputStream out, final ConnectModelImpl data) throws IOException,SystemException {

        String temp = null;
        final ConnectStatus status = data.getStatus();
        if (status != null) {
            temp = status.toString();
        }
        out.writeString(temp);
        out.writeInteger(data.getThreads());

        out.writeObject(data.getManagementClient());
        out.writeObject(data.getManagementServer());
        out.writeObject(data.getListener());
        out.writeList(data.getDataSources());
        out.writeList(data.getStoreModels());


    }

    @SuppressWarnings("unchecked")
    @Override
    public ConnectModelImpl read(final MessageInputStream input) throws IOException,SystemException {

        final ConnectModelImpl result = new ConnectModelImpl();

        final String temp = input.readString();
        if (temp == null) {
            result.setStatus(null);
        } else {
            final ConnectStatus status = ConnectStatus.valueOf(temp);
            result.setStatus(status);
        }
        result.setThreads(input.readInteger());
        result.setManagementClient((CommonClientModel) input.readObject());
        result.setManagementServer((CommonClientModel) input.readObject());
        result.setListener((ListenerModelImpl) input.readObject());
        result.setDataSources((List<String>) input.readList());
        input.readList(result.getStoreModels());
        return result;
    }

}
