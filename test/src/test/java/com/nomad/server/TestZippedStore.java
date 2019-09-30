package com.nomad.server;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.BeforeClass;

import com.nomad.cache.test.model.MainTestModel;
import com.nomad.cache.test.model.MainTestModelId;
import com.nomad.store.ModelBytesStore;
import com.nomad.store.ZippedModelBytesStore;
import com.nomad.utility.SimpleServerContext;

public class TestZippedStore {

    @BeforeClass
    public static void init() {


    }

    /*
     * test session isolation
     */
    @org.junit.Test
    public void test1() throws Exception {

        final ZippedModelBytesStore store = new ZippedModelBytesStore(false, false, null, null, null, null);
        MainTestModel test = new MainTestModel();
        test.setId(5);
        test.setName("teststsst");
        store.put(Arrays.asList(test),null);
        store.getQuietly(new MainTestModelId(5));

        test =  (MainTestModel) store.get(Arrays.asList(new MainTestModelId(5))).iterator().next();
        assertEquals(5, test.getId());
        assertEquals("teststsst", test.getName());

        test.setId(6);
        final MainTestModelId id = new MainTestModelId(6);
        test.setIdentifier(id);

        final ModelBytesStore store2 = new ModelBytesStore(false, false, null, null, new SimpleServerContext(), null);
        store2.put(Arrays.asList(test),null);
        store2.getQuietly(id);

        test = (MainTestModel) store2.get(Arrays.asList(new MainTestModelId(6))).iterator().next();
        assertEquals(6, test.getId());
        assertEquals("teststsst", test.getName());

    }

    @org.junit.Test
    public void test2() throws Exception {

        MainTestModel test = new MainTestModel();
        test.setId(6);
        test.setName("teststsst");

        final ModelBytesStore store2 = new ModelBytesStore(false, false, null, null, new SimpleServerContext(), null);
        store2.put(Arrays.asList(test),null);
        store2.getQuietly(new MainTestModelId(6));

        test = (MainTestModel) store2.get(Arrays.asList(new MainTestModelId(6))).iterator().next();
        assertEquals(6, test.getId());
        assertEquals("teststsst", test.getName());

    }

}
