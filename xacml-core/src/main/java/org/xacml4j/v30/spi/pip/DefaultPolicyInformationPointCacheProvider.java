package org.xacml4j.v30.spi.pip;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

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

	@Override
	public Content getContent(ResolverContext context) {
		ContentResolverDescriptor d = (ContentResolverDescriptor)context.getDescriptor();
		if(d.isCacheable()){
			Content v = contentCache.getIfPresent(ResolverCacheKey
					.builder()
					.id(d)
					.keys(context.getKeys())
					.build());
			return isExpired(v, context)?null:v;
		}
		return null;
	}

	@Override
	public void putContent(ResolverContext context, Content content) {
		ContentResolverDescriptor d = (ContentResolverDescriptor)context.getDescriptor();
		Preconditions.checkArgument(context.getDescriptor() == content.getDescriptor());
		if(d.isCacheable()){
			contentCache.put(ResolverCacheKey
					.builder()
					.id(d)
					.keys(context.getKeys())
					.build(), content);
		}
	}

	@Override
	public AttributeSet getAttributes(ResolverContext context) {
		AttributeResolverDescriptor d = (AttributeResolverDescriptor)context.getDescriptor();
		if(d.isCacheable()){
			ResolverCacheKey key =
					ResolverCacheKey
						.builder()
						.id(d)
						.keys(context.getKeys())
						.build();
			AttributeSet v = attributeCache.getIfPresent(key);
			if(v!= null && isExpired(v, context)){
				attributeCache.invalidate(key);
			}
			return v;
		}
		return null;
	}

	@Override
	public final void putAttributes(ResolverContext context, AttributeSet v) {
		AttributeResolverDescriptor d = (AttributeResolverDescriptor)context.getDescriptor();
		Preconditions.checkArgument(d.getId().equals(v.getDescriptor().getId()));
		if(d.isCacheable()){
			ResolverCacheKey key =
					ResolverCacheKey
						.builder()
						.id(d)
						.keys(context.getKeys())
						.build();
			attributeCache.put(key, v);
		}
	}

	private boolean isExpired(AttributeSet v, ResolverContext context){
		return ((context.getTicker().read() - v.getCreatedTime()) /
				1000000000L) >= v.getDescriptor().getPreferredCacheTTL();
	}

	private boolean isExpired(Content v, ResolverContext context){
		return ((context.getTicker().read() - v.getTimestamp()) /
				1000000000L) >= v.getDescriptor().getPreferredCacheTTL();
	}
}
