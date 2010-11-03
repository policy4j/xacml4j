package com.artagon.xacml.v3.spi.cache;

import com.artagon.xacml.v3.spi.CacheProvider;

public class NullCacheProvider implements CacheProvider
{

	@Override
	public <T> T get(Object v) {
		return null;
	}

	@Override
	public void put(Object k, Object v, long ttl) {
		
	}

	@Override
	public void putIfAbsent(Object k, Object v, long ttl) {
	}
	
}
