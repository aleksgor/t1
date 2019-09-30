package com.nomad.io;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Test;

import com.nomad.datadefinition.DataDefinitionServiceImpl;
import com.nomad.io.model.ChildModel;
import com.nomad.message.MessageAssembler;
import com.nomad.server.DataDefinitionService;

public class TestFakeReader {

    @Test
    public void testFakeRead() throws Exception {
        final ChildModel childModel = new ChildModel();
        childModel.setId(6);
        String name = "";
        while (name.length() < 2000) {
            name += "shdashdajkvsdfjakhvsdfjashd##vqsygq$%^wcwjhdcbvsd,nbvsdnvb salhdv glsdhv ldsh  jsdkvasdjs   hkjhkjhk";
        }
        childModel.setName(name);
        final ByteArrayOutputStream data = new ByteArrayOutputStream();
        final DataDefinitionService dataDefinitionService = new DataDefinitionServiceImpl(null, "model.xml", null);
        Thread.currentThread().getContextClassLoader().getResources("model.xml");
        dataDefinitionService.start();
        final MessageAssembler assembler = new BinaryMessageAssembler(dataDefinitionService);
        assembler.storeObject(childModel, data);

        final ByteArrayOutputStream copy = new ByteArrayOutputStream();
        final FakeMessageInputStream test = new FakeMessageInputStream(new ByteArrayInputStream(data.toByteArray()), copy);
        test.readObject();

        final ChildModel result = (ChildModel) assembler.getObject(new ByteArrayInputStream(copy.toByteArray()));
        assertEquals(result, childModel);

    }
}
