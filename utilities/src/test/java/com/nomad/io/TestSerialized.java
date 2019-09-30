package com.nomad.io;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.datadefinition.DataDefinitionServiceImpl;
import com.nomad.exception.SystemException;
import com.nomad.io.model.ChildModel;
import com.nomad.io.model.ChildModelId;
import com.nomad.io.model.EnumTest;
import com.nomad.io.model.MasterModel;
import com.nomad.io.model.MasterModelId;
import com.nomad.server.DataDefinitionService;

public class TestSerialized {

    private static Logger LOGGER = LoggerFactory.getLogger(TestSerialized.class);

    private int testSize = 1000;

    @org.junit.Test
    public void testSerialize() throws Exception {
        ChildModel child = new ChildModel(new ChildModelId(1));
        child.setName("child child child child child child child child child child child child child child child child child child child child child child child child ");

        byte[] data = serialize(child, 1000);
        ChildModel child1 = (ChildModel) readObject(data);
        assertEquals(child, child1);

    }

    @SuppressWarnings("unchecked")
    @org.junit.Test
    public void testLongObject() throws Exception {
        List<MasterModel> source = getLongObject(testSize);
        byte[] data = serialize(source, 18800);
        List<MasterModel> result = (List<MasterModel>) readObject(data);

        assertEquals(source.size(), result.size());

        long start = System.currentTimeMillis();
        data = serialize(source, 18900);
        result = (List<MasterModel>) readObject(data);

        assertEquals(source.size(), result.size());
        LOGGER.info("time:" + (System.currentTimeMillis() - start));

    }

    @SuppressWarnings("unchecked")
    @org.junit.Test
    public void testZipLongObject() throws Exception {
        List<MasterModel> source = getLongObject(testSize);
        byte[] data = zipSerialize(source);

        List<MasterModel> result = (List<MasterModel>) readZipObject(data);

        assertEquals(source, result);
    }

    private List<MasterModel> getLongObject(int length) {
        List<MasterModel> result = new ArrayList<>(length);

        for (int i = 0; i < length; i++) {
            MasterModel test = new MasterModel(new MasterModelId(i));
            test.setDate(new Date(i));
            test.setName("test test tets test test tets test test tets test test tets test test tets test test tets test test tets test test tets ");
            ChildModel child = new ChildModel(new ChildModelId(i));
            child.setName("child child child child child child child child child child child child child child child child child child child child child child child child ");
            test.setChildId(i);
            test.setChild(child);
            result.add(test);
        }
        return result;
    }

    @org.junit.Test
    public void testSpeed() throws Exception {
        for (int i = 0; i < 1000; i++) {
            ChildModel child = new ChildModel(new ChildModelId(1));
            child.setName("child child child child child child child child child child child child child child child child child child child child child child child child ");

            byte[] data = serialize(child, 1000);
            readObject(data);
        }
    }

    @org.junit.Test
    public void testEnum() throws Exception {
        EnumTest first = EnumTest.FIRST;

        byte[] data = serialize(first, 1000);
        assertEquals(first, readObject(data, first));

        first = EnumTest.SECOND;
        data = serialize(first, 1000);
        assertEquals(first, readObject(data, first));

    }

    @SuppressWarnings("resource")
    private byte[] serialize(Object object, int initSize) throws IOException, SystemException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(initSize);
        MessageOutputStream messageOutputStream = new MessageOutputStream(outputStream, dataDefinition);
        messageOutputStream.writeObject(object);
        return outputStream.toByteArray();
    }

    @SuppressWarnings("resource")
    private byte[] zipSerialize(Object object) throws IOException, SystemException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream zip = new GZIPOutputStream(out);
        new MessageOutputStream(zip, dataDefinition).writeObject(object);
        zip.finish();

        return out.toByteArray();
    }

    private Object readObject(byte[] data) throws IOException, SystemException {
        return readObject(data, null);
        // InputStream input = new ByteArrayInputStream(data);
        // return new MessageInputStream(input,dataDefinition).readObject();
    }

    @SuppressWarnings("resource")
    private Object readObject(byte[] data, Object object) throws IOException, SystemException {
        InputStream input = new ByteArrayInputStream(data);
        return new MessageInputStream(input, dataDefinition).readObject(object);
    }

    @SuppressWarnings("resource")
    private Object readZipObject(byte[] data) throws IOException, SystemException {
        InputStream input = new ByteArrayInputStream(data);
        GZIPInputStream zip = new GZIPInputStream(input);
        return new MessageInputStream(zip, dataDefinition).readObject();
    }

    @BeforeClass
    public static void start() throws Exception {

        dataDefinition = new DataDefinitionServiceImpl(null, "model.xml", null);
        dataDefinition.start();

    }

    private static DataDefinitionService dataDefinition;
}
