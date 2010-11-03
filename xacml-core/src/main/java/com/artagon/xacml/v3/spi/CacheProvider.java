package com.artagon.xacml.v3.spi;


public interface CacheProvider 
{
	<T> T get(Object v);
	
	void put(Object k, Object v, long ttl);
	
	void putIfAbsent(Object k, Object v, long ttl);
}
