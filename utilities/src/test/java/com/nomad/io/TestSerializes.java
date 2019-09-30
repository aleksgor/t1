package com.nomad.io;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.nomad.exception.SystemException;
import com.nomad.model.ServerModelImpl;

public class TestSerializes {

    @org.junit.Test
    public void testServerModelSerialize() throws Exception {
        ServerModelImpl model = new ServerModelImpl();
        byte[] data = serialize(model, 1000);
        ServerModelImpl copy = (ServerModelImpl) readObject(data);
        assertEquals(model, copy);

    }

    @SuppressWarnings("resource")
    private byte[] serialize(Object object, int initSZ) throws IOException, SystemException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(initSZ);
        MessageOutputStream messageOutStream = new MessageOutputStream(out, null);
        messageOutStream.writeObject(object);
        return out.toByteArray();
    }

    @SuppressWarnings("resource")
    private Object readObject(byte[] data) throws IOException, SystemException {
        InputStream input = new ByteArrayInputStream(data);
        return new MessageInputStream(input, null).readObject();
    }

}
