package com.nomad.cache.commonclientserver.idgenerator;

import java.io.IOException;
import java.math.BigInteger;

import com.nomad.exception.SystemException;
import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.serializer.Serializer;

public class BigIntegerSerializer implements Serializer<BigInteger> {

    @Override
    public void write(final MessageOutputStream out, final BigInteger data) throws IOException, SystemException {

        if (data == null) {
            out.writeBytes(null);
        } else {
            out.writeBytes(data.toByteArray());
        }

    }

    @Override
    public BigInteger read(final MessageInputStream input) throws IOException, SystemException {

        BigInteger result = null;

        byte[] bytes = input.readBytes();
        if (bytes != null) {
            result = new BigInteger(bytes);
        }
        return result;
    }

}
