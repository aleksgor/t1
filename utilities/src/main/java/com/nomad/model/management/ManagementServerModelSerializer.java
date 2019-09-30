package com.nomad.model.management;

import java.io.IOException;

import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.serializer.Serializer;

public class ManagementServerModelSerializer  implements Serializer<ManagementServerModelImpl> {

    @Override
    public void write(MessageOutputStream out, ManagementServerModelImpl data) throws IOException {

        out.writeString( data.getHost() );
        out.writeLong(data.getKeepAliveTime());
        out.writeInteger(data.getMinThreads());
        out.writeInteger(data.getMaxThreads());
        out.writeInteger(data.getPort());

    }

    @Override
    public ManagementServerModelImpl read(MessageInputStream input) throws IOException {
        ManagementServerModelImpl result = new ManagementServerModelImpl();
        result.setHost(input.readString());
        result.setKeepAliveTime(input.readLong());
        result.setMinThreads(input.readInteger());
        result.setMaxThreads(input.readInteger());
        result.setPort(input.readInteger());
        return result;
    }


}
