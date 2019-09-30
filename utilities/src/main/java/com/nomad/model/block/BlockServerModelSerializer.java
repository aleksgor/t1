package com.nomad.model.block;

import java.io.IOException;

import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.serializer.Serializer;

public class BlockServerModelSerializer implements Serializer<BlockServerModelImpl> {


    @Override
    public void write(MessageOutputStream out, BlockServerModelImpl data) throws IOException {

        out.writeString(data.getHost());
        out.writeInteger(data.getPort());
        out.writeInteger(data.getThreads());
        out.writeLong(data.getSessionTimeout());


    }

    @Override
    public BlockServerModelImpl read(MessageInputStream input) throws IOException {

        BlockServerModelImpl result = new BlockServerModelImpl();
        result.setHost(input.readString());
        result.setPort(input.readInteger());
        result.setThreads(input.readInteger());
        result.setSessionTimeout(input.readLong());

        return result;
    }

}
