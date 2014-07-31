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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public final class DefaultPolicyInformationPointCacheProvider
		extends BasePolicyInformationPointCacheProvider {
	private final Cache<ResolverCacheKey, AttributeSet> attributeCache;
	private final Cache<ResolverCacheKey, Content> contentCache;

	/**
	 * Default maximum number of items in the attribute cache
	 */
	private static final int DEFAULT_MAX_ATTRIBUTE_ITEMS = 2048;

	/**
	 * Default maximum number of items in the content cache
	 */
	private static final int DEFAULT_MAX_CONTENT_ITEMS = 2048;

	public DefaultPolicyInformationPointCacheProvider() {
		this(DEFAULT_MAX_ATTRIBUTE_ITEMS, DEFAULT_MAX_CONTENT_ITEMS);
	}

	public DefaultPolicyInformationPointCacheProvider(
			int maxAttrSize,
			int maxContentSize) {
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
	protected Content doGetContent(ResolverContext context) {
		ResolverCacheKey key = ResolverCacheKey
				.builder()
				.id(context.getDescriptor())
				.keys(context.getKeys())
				.build();
		Content v = contentCache.getIfPresent(key);
		if (v != null && isExpired(v, context)) {
			attributeCache.invalidate(key);
		}
		return v;
	}

	@Override
	protected void doPutContent(ResolverContext context, Content content) {
		contentCache.put(ResolverCacheKey
				.builder()
				.id(context.getDescriptor())
				.keys(context.getKeys())
				.build(), content);
	}

	@Override
	protected AttributeSet doGetAttributes(ResolverContext context) {
		ResolverCacheKey key = ResolverCacheKey
				.builder()
				.id(context.getDescriptor())
				.keys(context.getKeys())
				.build();
		AttributeSet v = attributeCache.getIfPresent(key);
		if (v != null && isExpired(v, context)) {
			attributeCache.invalidate(key);
		}
		return v;
	}

	@Override
	protected void doPutAttributes(ResolverContext context, AttributeSet v) {
		ResolverCacheKey key = ResolverCacheKey
				.builder()
				.id(context.getDescriptor())
				.keys(context.getKeys())
				.build();
		attributeCache.put(key, v);
	}

	private boolean isExpired(AttributeSet v, ResolverContext context) {
		return ((context.getTicker().read() - v.getCreatedTime()) /
				1000000000L) >= v.getDescriptor().getPreferredCacheTTL();
	}

	private boolean isExpired(Content v, ResolverContext context) {
		return ((context.getTicker().read() - v.getTimestamp()) /
				1000000000L) >= v.getDescriptor().getPreferredCacheTTL();
	}
}
