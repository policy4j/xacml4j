package com.artagon.xacml.ehcache;

import java.util.List;
import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.spi.pip.AttributeResolverDescriptor;
import com.artagon.xacml.v3.spi.pip.AttributeSet;
import com.artagon.xacml.v3.spi.pip.BaseResolverResultCacheProvider;
import com.artagon.xacml.v3.spi.pip.Content;
import com.artagon.xacml.v3.spi.pip.ContentResolverDescriptor;
import com.artagon.xacml.v3.spi.pip.ResolverDescriptor;
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
	protected AttributeSet doGetAttributes(AttributeResolverDescriptor d,
			List<BagOfAttributeValues> keys) 
	{	
		Object k = createKey(d, keys);
		Element e = attributesCache.get(k);
		if(e == null){
			return null;
		}
		Object v = e.getObjectValue();
		if(v == null){
			return null;
		}
		return new AttributeSet(d, (Map<String, BagOfAttributeValues>)v);
	}

	@Override
	protected void doPutAttributes(AttributeResolverDescriptor d, List<BagOfAttributeValues> keys,
			AttributeSet v) {
		Object k = createKey(d, keys);
		Element e = new Element(k, v.toMap());
		e.setTimeToLive(d.getPreferreredCacheTTL());
		attributesCache.put(e);
	}

	@Override
	protected Content doGetContent(ContentResolverDescriptor d,
			List<BagOfAttributeValues> keys) 
	{
		Object k = createKey(d, keys);
		Element e = contentCache.get(k);
		if(e == null){
			return null;
		}
		Object v = e.getObjectValue();
		if(v == null){
			return null;
		}
		return (Content)v;
	}

	@Override
	protected void doPutContent(ContentResolverDescriptor d,
			List<BagOfAttributeValues> keys, Content content) 
	{
		Object k = createKey(d, keys);
		Element e = new Element(k, content);
		e.setTimeToLive(d.getPreferreredCacheTTL());
		contentCache.put(e);
	}
	
	private Object createKey(ResolverDescriptor d, List<BagOfAttributeValues> keys)
	{
		return new ResolverResultCacheKey(d.getId(), keys);
	}
}
