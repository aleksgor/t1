package com.nomad.utility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class  CollectionHashMap< K, V >  extends HashMap<K, Collection<V>>  {

    
    public  Collection<V> putValue(K key, V value) {
        Collection<V> list= get(key);
        if(list==null){
            list= new ArrayList<V>();
            super.put(key,list);
        }
        list.add(value);
        return list;
    }

    @Override
    public Collection<V> get(Object key) {
        return super.get(key);
    }


    
}
