package com.nomad.io;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.BeforeClass;

import com.nomad.datadefinition.DataDefinitionServiceImpl;
import com.nomad.exception.SystemException;
import com.nomad.io.model.ChildModel;
import com.nomad.io.model.ChildModelId;
import com.nomad.io.model.MasterModel;
import com.nomad.server.DataDefinitionService;

public class TestFieldSerialized {
    @org.junit.Test
    public void testSerialize() throws Exception {

        MasterModel masterModel = new MasterModel();
        masterModel.setName("mastermodel");
        ChildModel child = new ChildModel(new ChildModelId(1));
        child.setName("child child child child child child child child child child child child child child child child child child child child child child child child ");

        byte[] data = serialize(child, 1000);
        ChildModel child1 = (ChildModel) readObject(data);
        assertEquals(child, child1);

    }

    @SuppressWarnings("resource")
    private byte[] serialize(Object object, int initSize) throws IOException, SystemException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(initSize);
        MessageOutputStream messageOutputStream = new MessageOutputStream(outputStream, dataDefinition);
        messageOutputStream.writeObject(object);
        return outputStream.toByteArray();
    }

    @SuppressWarnings("resource")
    private Object readObject(byte[] data) throws IOException, SystemException {
        InputStream input = new ByteArrayInputStream(data);
        return new MessageInputStream(input, dataDefinition).readObject();
    }

    @BeforeClass
    public static void start() throws Exception {

        dataDefinition = new DataDefinitionServiceImpl(null, "model2.xml", null);
        dataDefinition.start();

    }

    private static DataDefinitionService dataDefinition;
}
