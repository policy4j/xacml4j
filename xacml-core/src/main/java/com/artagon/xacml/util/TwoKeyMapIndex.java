package com.artagon.xacml.util;

import java.util.Map;

public final class TwoKeyMapIndex <Key0, Key1, V> implements TwoKeyIndex<Key0, Key1, V>
{
	private MapMaker maker;
	private Map<Key0, Map<Key1, V>> index;
	
	public TwoKeyMapIndex(MapMaker maker){
		this.maker = maker;
		this.index = maker.make();
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
			index2 = maker.make();
			index.put(k0, index2);
		}
		return index2.put(k1, v);
	}
}
