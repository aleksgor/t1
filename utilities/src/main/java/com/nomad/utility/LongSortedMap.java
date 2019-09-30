package com.nomad.utility;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;



public class LongSortedMap {

    private Map<String, Long> map = new LinkedHashMap<>();

    public Long get(String key) {
        return map.get(key);
    }

    public void updateSession(String sessionId) {
        map.remove(sessionId);
        map.put(sessionId, System.currentTimeMillis());
    }
    public Long put(String key, Long value) {
        map.remove(key);
        return map.put(key, value);
    }

    public Collection<Long> getSortedValue(){
        return map.values();
    }

    public Set<String> getSortedKeys(){

        return map.keySet();
    }


    public Long remove(String key) {
        Long result= map.remove(key);
        return result;
    }
    public int size(){
        return map.size();
    }
    public boolean containsKey(String key){
        return map.containsKey(key);
    }
}