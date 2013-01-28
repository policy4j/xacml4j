package com.artagon.xacml.v30.spi.pip;

import java.util.List;

import com.artagon.xacml.v30.BagOfAttributeExp;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public final class DefaultPolicyInformationPointCacheProvider 
	extends BasePolicyInformationPointCacheProvider
{
	private Cache<ResolverCacheKey, AttributeSet> attributeCache;
	private Cache<ResolverCacheKey, Content> contentCache;
	
	public DefaultPolicyInformationPointCacheProvider(){
		this(Integer.MAX_VALUE/2, Integer.MAX_VALUE/2);
	}
	
	public DefaultPolicyInformationPointCacheProvider(
			int maxAttrSize, 
			int maxContentSize){
		this.attributeCache = CacheBuilder
				.newBuilder()
				.maximumSize(maxAttrSize)
				.build();
		this.contentCache = CacheBuilder
				.newBuilder()
				.maximumSize(maxContentSize)
				.build();
	}

	@Override
	protected Content doGetContent(ContentResolverDescriptor d,
			List<BagOfAttributeExp> keys) {
		return contentCache.getIfPresent(ResolverCacheKey.builder().id(d).keys(keys).build());
	}

	@Override
	protected AttributeSet doGetAttributes(AttributeResolverDescriptor d,
			List<BagOfAttributeExp> keys) {
		
		return attributeCache.getIfPresent(ResolverCacheKey.builder().id(d).keys(keys).build());
	}

	@Override
	protected void doPutContent(ContentResolverDescriptor d,
			List<BagOfAttributeExp> keys, Content content) {
		contentCache.put(ResolverCacheKey.builder().id(d).keys(keys).build(), content);
	}

	@Override
	protected void doPutAttributes(AttributeResolverDescriptor d,
			List<BagOfAttributeExp> keys, AttributeSet v) {
		attributeCache.put(ResolverCacheKey.builder().id(d).keys(keys).build(), v);
	}
}
