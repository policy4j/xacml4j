package com.artagon.xacml.ehcache;

import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.w3c.dom.Node;

import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.spi.pip.AttributeResolverDescriptor;
import com.artagon.xacml.v3.spi.pip.AttributeSet;
import com.artagon.xacml.v3.spi.pip.ContentResolverDescriptor;
import com.artagon.xacml.v3.spi.pip.ResolverDescriptor;
import com.artagon.xacml.v3.spi.pip.cache.BaseResolverResultCacheProvider;
import com.google.common.base.Preconditions;

public class ResolverResultsEhcacheProvider extends BaseResolverResultCacheProvider
{
	private Cache attributesCache;
	private Cache contentCache;
	
	
	
	public ResolverResultsEhcacheProvider(
			Cache attributesCache,
			Cache contentCache){
		Preconditions.checkNotNull(attributesCache);
		Preconditions.checkNotNull(contentCache);
		this.attributesCache = attributesCache;
		this.contentCache = contentCache;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected AttributeSet doGet(AttributeResolverDescriptor d,
			BagOfAttributeValues[] keys) 
	{	
		Object k = createKey(d, keys);
		Element e = attributesCache.get(k);
		if(e == null || 
				e.isExpired()){
			return null;
		}
		return new AttributeSet(d, (Map<String, BagOfAttributeValues>)e.getObjectValue());
	}

	@Override
	protected void doPut(AttributeResolverDescriptor d, BagOfAttributeValues[] keys,
			AttributeSet v) {
		Object k = createKey(d, keys);
		Element e = new Element(k, v.toMap());
		e.setTimeToLive(d.getPreferreredCacheTTL());
		attributesCache.put(e);
	}

	@Override
	protected Node doGetContent(ContentResolverDescriptor d,
			BagOfAttributeValues[] keys) 
	{
		Object k = createKey(d, keys);
		Element e = contentCache.get(k);
		if(e == null || 
				e.isExpired()){
			return null;
		}
		return (Node)e.getObjectValue();
	}

	@Override
	protected void doPut(ContentResolverDescriptor d,
			BagOfAttributeValues[] keys, Node content) 
	{
		Object k = createKey(d, keys);
		Element e = new Element(k, content);
		e.setTimeToLive(d.getPreferreredCacheTTL());
		contentCache.put(e);
	}
	
	private Object createKey(ResolverDescriptor d, BagOfAttributeValues[] contextKeys)
	{
		return new ResolverResultCacheKey(d.getId(), contextKeys);
	}
}
