package com.artagon.xacml.util;

import java.util.HashMap;
import java.util.Map;

public class ThreeKeyHashIndex <Key0, Key1, Key2, V> 
	implements ThreeKeyIndex<Key0, Key1, Key2, V> {
	
	private Map<Key0 , TwoKeyHashIndex<Key1, Key2, V>> index;
	
	public ThreeKeyHashIndex(){
		this.index = new HashMap<Key0, TwoKeyHashIndex<Key1,Key2,V>>();
	}

	@Override
	public V get(Key0 k0, Key1 k1, Key2 k2) {
		TwoKeyHashIndex<Key1, Key2, V> index2 = index.get(k0);
		return index2 == null?null:index2.get(k1, k2);
	}

	@Override
	public V put(Key0 k0, Key1 k1, Key2 k2, V v) {
		TwoKeyHashIndex<Key1, Key2, V> index2 = index.get(k0);
		if(index2 == null){
			index2 = new TwoKeyHashIndex<Key1, Key2, V>();
			index.put(k0, index2);
		}
		return index2.put(k1, k2, v);
	}
}
