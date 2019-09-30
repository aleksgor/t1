package com.nomad.saver.adapters;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public final class MapAdapter extends XmlAdapter<MapType, Map<String, String>> {

    @Override
    public MapType marshal(Map<String, String> map) throws Exception {
        MapType myMapType = new MapType();
        for (Entry<String, String> entry : map.entrySet()) {
            MapEntryType myMapEntryType = new MapEntryType();
            myMapEntryType.key = entry.getKey();
            myMapEntryType.value = entry.getValue();
            myMapType.entry.add(myMapEntryType);
        }
        return myMapType;
    }

    @Override
    public Map<String, String> unmarshal(MapType mapType) throws Exception {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        for (MapEntryType myEntryType : mapType.entry) {
            hashMap.put(myEntryType.key, myEntryType.value);
        }
        return hashMap;
    }

}