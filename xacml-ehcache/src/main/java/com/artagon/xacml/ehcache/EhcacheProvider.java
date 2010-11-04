package com.artagon.xacml.ehcache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import com.artagon.xacml.v3.spi.CacheProvider;
import com.google.common.base.Preconditions;

public class EhcacheProvider implements CacheProvider
{
	private Cache cache;
	
	public EhcacheProvider(Cache cache){
		Preconditions.checkNotNull(cache);
		this.cache = cache;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object key) 
	{
		Element e = cache.get(key);
		return (T)e.getValue();
	}

	@Override
	public void put(Object key, Object v, int ttl) {
		Element e = new Element(key, v);
		e.setTimeToLive(ttl);
		cache.put(e);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T putIfAbsent(Object key, T v, int ttl) {
		
		Element e = new Element(key, v);
		e.setTimeToLive(ttl);
		Element old = cache.putIfAbsent(e);
		return (old != null)?(T)e.getValue():null;
	}
	
}
