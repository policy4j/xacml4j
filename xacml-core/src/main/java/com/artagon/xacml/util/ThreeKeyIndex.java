package com.artagon.xacml.util;

public interface ThreeKeyIndex <Key0, Key1, Key2, V>
{
	V get(Key0 k0, Key1 k1, Key2 k2);
	V put(Key0 k0, Key1 k1, Key2 key2, V v);
}

