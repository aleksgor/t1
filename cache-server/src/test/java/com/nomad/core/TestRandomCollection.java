package com.nomad.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.nomad.utility.RandomCollection;

public class TestRandomCollection {

    @org.junit.Test
    public void testServerModelSerialized() throws Exception {

        final Map<String, Integer> results = new HashMap<>();

        final RandomCollection<String> collection = new RandomCollection<String>();
        collection.add(1d, "1");
        collection.add(2d, "2");
        collection.add(3d, "3");
        collection.add(10d, "10");
        for (int i = 0; i < 100; i++) {
            final Collection<String> strs = collection.getRandomList(2);
            assertEquals(2, strs.size());
            for (final String string : strs) {
                Integer itg = results.get(string);
                if (itg == null) {
                    itg = 0;
                }
                results.put(string, ++itg);
            }
        }

        assertTrue(results.get("10") > results.get("3"));
        assertTrue(results.get("3") > results.get("2"));
        assertTrue(results.get("2") > results.get("1"));
        final Collection<String> strs = collection.getAllElements();
        assertEquals(4, strs.size());


    }

    @org.junit.Test
    public void testSameObject() throws Exception {

        final Map<String, Integer> results = new HashMap<>();

        final RandomCollection<String> collection = new RandomCollection<String>();
        collection.add(1d, "1");
        collection.add(2d, "2");
        collection.add(2d, "25");
        collection.add(10d, "10");
        for (int i = 0; i < 1000; i++) {
            final Collection<String> strs = collection.getRandomList(2);
            assertEquals(2, strs.size());
            for (final String string : strs) {
                Integer itg = results.get(string);
                if (itg == null) {
                    itg = 0;
                }
                results.put(string, ++itg);
            }
        }
        assertTrue(results.get("10") > results.get("25"));
        assertEquals(results.get("25"), results.get("25"), 20d);
        assertTrue(results.get("2") > results.get("1"));
        Collection<String> strs = collection.getAllElements();
        assertEquals(4, strs.size());

       
    }

}
