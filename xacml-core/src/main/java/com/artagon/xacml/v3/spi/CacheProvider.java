package com.artagon.xacml.v3.spi;


public interface CacheProvider 
{
	<T> T get(Object v);
	
	void put(Object key, Object v, int ttl);
	
	<T> T putIfAbsent(Object key, T v, int ttl);
}
