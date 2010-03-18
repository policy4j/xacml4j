package com.artagon.xacml.util;

import java.util.Map;

public class ThreeKeyMapIndex <Key0, Key1, Key2, V> 
	implements ThreeKeyIndex<Key0, Key1, Key2, V> 
{
	private MapMaker maker;
	private Map<Key0 , TwoKeyMapIndex<Key1, Key2, V>> index;
	
	public ThreeKeyMapIndex(MapMaker maker){
		this.index = maker.make();
	}

	@Override
	public V get(Key0 k0, Key1 k1, Key2 k2) {
		TwoKeyMapIndex<Key1, Key2, V> index2 = index.get(k0);
		return (index2 == null)?null:index2.get(k1, k2);
	}

	@Override
	public V put(Key0 k0, Key1 k1, Key2 k2, V v) {
		TwoKeyMapIndex<Key1, Key2, V> index2 = index.get(k0);
		if(index2 == null){
			index2 = new TwoKeyMapIndex<Key1, Key2, V>(maker);
			index.put(k0, index2);
		}
		return index2.put(k1, k2, v);
	}
}
