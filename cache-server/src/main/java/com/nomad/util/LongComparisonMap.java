package com.nomad.util;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import com.nomad.model.Identifier;


public class LongComparisonMap {

	private Map<Identifier,Long> map = new LinkedHashMap<>();

	public Long put(Identifier key, Long value) {
		map.remove(key);
		return map.put(key, value);
	}
	
	public Iterator<Identifier> getSortedKeys(){
		return  map.keySet().iterator();
	} 
	
	public Long remove(Identifier key) {
		Long result= map.remove(key);
		return result;
	}
	public int size(){
		return map.size();
	}
}