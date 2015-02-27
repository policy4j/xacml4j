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
import static com.google.common.base.Preconditions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class DefaultPolicyInformationPointCacheProvider
		extends BasePolicyInformationPointCacheProvider {

    private final static Logger log = LoggerFactory.getLogger(DefaultPolicyInformationPointCacheProvider.class);
    
	private Cache<ResolverCacheKey, AttributeSet> attributeCache;
	private Cache<ResolverCacheKey, Content> contentCache;

	/**
	 * Default maximum number of items in the category cache
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
		this.attributeCache = CacheBuilder.<ResolverCacheKey, AttributeSet>newBuilder()
                .maximumSize(maxAttrSize)
                .build();
        this.contentCache = CacheBuilder.<ResolverCacheKey, Content>newBuilder()
                .maximumSize(maxContentSize)
                .build();
	}

    public DefaultPolicyInformationPointCacheProvider(
            Cache<ResolverCacheKey, AttributeSet> attributeCache,
            Cache<ResolverCacheKey, Content> contentCache) {
        this.attributeCache = checkNotNull(attributeCache);
        this.contentCache = checkNotNull(contentCache);
    }

	@Override
	protected Content doGetContent(ResolverContext context) {
		ResolverCacheKey key = ResolverCacheKey
				.builder()
				.id(context.getDescriptor())
				.keys(context.getKeys())
				.build();
		Content v = contentCache.getIfPresent(key);
		if (v != null 
                && isExpired(v, context)) {
                if(log.isDebugEnabled()){
                    log.debug("Expired content, " +
                            "invalidating key=\"{}\"", key);
                }
            attributeCache.invalidate(key);
            v = null;
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
		if (v != null 
                && isExpired(v, context)) {
            if(log.isDebugEnabled()){
                log.debug("Expired attribute set, " +
                        "invalidating key=\"{}\"", key);
            }
			attributeCache.invalidate(key);
            v = null;
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
}
