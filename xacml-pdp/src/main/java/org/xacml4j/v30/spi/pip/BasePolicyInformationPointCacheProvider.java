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

import java.time.Duration;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * Base class for {@link PolicyInformationPointCacheProvider} implementations
 *
 * @author Giedrius Trumpickas
 */
public abstract class BasePolicyInformationPointCacheProvider
		implements PolicyInformationPointCacheProvider
{
	private final static Logger LOG = LoggerFactory.getLogger(BasePolicyInformationPointCacheProvider.class);

	@Override
	public final Optional<ContentRef> getContent(ResolverContext context) {
		return (context.getDescriptor().isCacheable() ?
				Optional.ofNullable(doGetContent(context)): Optional.empty());

	}

	@Override
	public final void putContent(ResolverContext context, ContentRef content) {
		Preconditions.checkArgument(context.getDescriptor().getId().equals(content.getDescriptor().getId()),
				"ContentRef descriptor Id \"%s\" " +
						"must match resolver context descriptor Id \"%s\"",
				content.getDescriptor().getId(), context.getDescriptor().getId());
		ContentResolverDescriptor d = (ContentResolverDescriptor) context.getDescriptor();
		if (d.isCacheable() ||
				!isExpired(content, context)) {
			doPutContent(context, content);
		}
	}

	@Override
	public final Optional<AttributeSet> getAttributes(ResolverContext context) {
		return (context.getDescriptor().isCacheable() ?
				Optional.ofNullable(doGetAttributes(context)) : Optional.empty());
	}

	@Override
	public final void putAttributes(ResolverContext context, AttributeSet v) {
		Preconditions.checkArgument(context.getDescriptor().getId().equals(v.getDescriptor().getId()),
				"Attribute set descriptor Id \"%s\" must match resolver context descriptor Id \"%s\"",
				v.getDescriptor().getId(), context.getDescriptor().getId());
		if (context.getDescriptor().isCacheable() ||
				isExpired(v, context)) {
			doPutAttributes(context, v);
		}
	}

	protected abstract ContentRef doGetContent(ResolverContext context);

	protected abstract AttributeSet doGetAttributes(ResolverContext context);

	protected abstract void doPutContent(ResolverContext context, ContentRef content);

	protected abstract void doPutAttributes(ResolverContext context, AttributeSet v);

	private static boolean isExpired(AttributeSet v, ResolverContext context) {
		Duration expiresIn = Duration.between(context.getClock().instant(), v.getTimestamp());
		if (LOG.isDebugEnabled()) {
			LOG.debug("AttributeSet=\"{}\" expiresIn=\"{}\" in cache", v, expiresIn);
		}
		return expiresIn
				.compareTo(
						v.getDescriptor().getPreferredCacheTTL()) > 0;
	}

	private static boolean isExpired(ContentRef v, ResolverContext context) {

		Duration expiresIn = Duration.between(context.getClock().instant(),
		                                      v.getTimestamp());
		if (LOG.isDebugEnabled()) {
			LOG.debug("ContentRef=\"{}\" expiresIn=\"{}\" in cache", v, expiresIn);
		}
		return expiresIn
				.compareTo(
						v.getDescriptor().getPreferredCacheTTL()) > 0;
	}
}
