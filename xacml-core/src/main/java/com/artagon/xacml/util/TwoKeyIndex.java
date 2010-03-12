package com.artagon.xacml.util;

public interface TwoKeyIndex <Key0, Key1, V>
{
	V get(Key0 k0, Key1 k1);
	V put(Key0 k0, Key1 k1, V v);
}
