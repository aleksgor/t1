package com.nomad.cache.test;

import java.util.Comparator;
import java.util.Map;

public class ValueComparator implements Comparator<String> {

    Map<String, Integer> base;
    public ValueComparator(Map<String, Integer> base) {
        this.base = base;
    }

    public int compare(String a, String b) {
        return base.get(a).compareTo(base.get(b));
    }

}
