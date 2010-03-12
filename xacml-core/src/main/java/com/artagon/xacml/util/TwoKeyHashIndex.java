package com.artagon.xacml.util;

import java.util.HashMap;
import java.util.Map;

public final class TwoKeyHashIndex <Key0, Key1, V> implements TwoKeyIndex<Key0, Key1, V>
{
	private Map<Key0, Map<Key1, V>> index;
	
	public TwoKeyHashIndex(){
		this.index = new HashMap<Key0, Map<Key1,V>>();
	}

	@Override
	public V get(Key0 k0, Key1 k1) {
		Map<Key1, V> index2 = index.get(k0);
		return index2 == null?null:index2.get(k1); 
	}

	@Override
	public V put(Key0 k0, Key1 k1, V v) {
		Map<Key1, V> index2 = index.get(k0);
		if(index2 == null){
			index2 = new HashMap<Key1, V>();
			index.put(k0, index2);
		}
		return index2.put(k1, v);
	}
}
