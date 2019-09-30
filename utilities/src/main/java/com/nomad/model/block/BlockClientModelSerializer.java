package com.nomad.model.block;

import java.io.IOException;

import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.serializer.Serializer;

public class BlockClientModelSerializer implements Serializer<BlockClientModelImpl> {


    @Override
    public void write(MessageOutputStream out, BlockClientModelImpl data) throws IOException {

        out.writeString(data.getHost());
        out.writeInteger(data.getPort());
        out.writeInteger(data.getThreads());
        out.writeInteger(data.getTimeOut());

    }

    @Override
    public BlockClientModelImpl read(MessageInputStream input) throws IOException {

        BlockClientModelImpl result = new BlockClientModelImpl();
        result.setHost(input.readString());
        result.setPort(input.readInteger());
        result.setThreads(input.readInteger());
        result.setTimeOut(input.readInteger());

        return result;
    }

}
