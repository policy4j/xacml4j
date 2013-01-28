package org.xacml4j.v30.spi.pip;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public final class DefaultPolicyInformationPointCacheProvider 
	implements PolicyInformationPointCacheProvider
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
	
	public Content getContent(ResolverContext context) {
		ContentResolverDescriptor d = (ContentResolverDescriptor)context.getDescriptor();
		if(d.isCachable()){
			Content v = contentCache.getIfPresent(ResolverCacheKey
					.builder()
					.id(d)
					.keys(context.getKeys())
					.build());
			return isExpired(v, context)?null:v;
		}
		return null;
	}
	
	public void putContent(ResolverContext context, Content content) {
		ContentResolverDescriptor d = (ContentResolverDescriptor)context.getDescriptor();
		Preconditions.checkArgument(context.getDescriptor() == content.getDescriptor());
		if(d.isCachable()){
			contentCache.put(ResolverCacheKey
					.builder()
					.id(d)
					.keys(context.getKeys())
					.build(), content);
		}
	}
	
	public AttributeSet getAttributes(ResolverContext context) {
		AttributeResolverDescriptor d = (AttributeResolverDescriptor)context.getDescriptor();
		if(d.isCachable()){
			AttributeSet v = attributeCache.getIfPresent(ResolverCacheKey
					.builder()
					.id(d)
					.keys(context.getKeys())
					.build());
			return isExpired(v, context)?null:v;
		}
		return null;
	}

	@Override
	public final void putAttributes(ResolverContext context, AttributeSet v) {
		AttributeResolverDescriptor d = (AttributeResolverDescriptor)context.getDescriptor();
		Preconditions.checkArgument(d.getId().equals(v.getDescriptor().getId()));
		if(d.isCachable()){
			attributeCache.put(ResolverCacheKey
					.builder()
					.id(d)
					.keys(context.getKeys())
					.build(), v);
		}
	}

	private boolean isExpired(AttributeSet v, ResolverContext context){
		return ((context.getTicker().read() - v.getTimestamp()) / 
				1000000000L) >= v.getDescriptor().getPreferreredCacheTTL();
	}
	
	private boolean isExpired(Content v, ResolverContext context){
		return ((context.getTicker().read() - v.getTimestamp()) / 
				1000000000L) >= v.getDescriptor().getPreferreredCacheTTL();
	}
}
