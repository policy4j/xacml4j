package com.artagon.xacml.v3.spi;


public interface CacheProvider 
{
	<T> T get(Object v);
	
	void put(Object key, Object v, long ttl);
	
	void putIfAbsent(Object key, Object v, long ttl);
}
