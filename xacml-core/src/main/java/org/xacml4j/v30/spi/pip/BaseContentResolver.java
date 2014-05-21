package org.xacml4j.v30.spi.pip;

/*
 * #%L
 * Artagon XACML 3.0 Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Artagon
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

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.google.common.base.Preconditions;

/**
 * A base class for {@link ContentResolver} implementations
 *
 * @author Giedrius Trumpickas
 */
public abstract class BaseContentResolver implements ContentResolver
{
	private final static Logger log = LoggerFactory.getLogger(BaseContentResolver.class);

	private ContentResolverDescriptor descriptor;

	private AtomicInteger preferredCacheTTL;


	protected BaseContentResolver(ContentResolverDescriptor descriptor){
		Preconditions.checkArgument(descriptor != null);
		this.descriptor = new ContentResolverDescriptorDelegate(descriptor){
			@Override
			public int getPreferredCacheTTL() {
				return (preferredCacheTTL == null)?
						super.getPreferredCacheTTL(): preferredCacheTTL.get();
			}
		};
	}

	@Override
	public final ContentResolverDescriptor getDescriptor() {
		return descriptor;
	}

	@Override
	public final Content resolve(
			ResolverContext context) throws Exception
	{
		Preconditions.checkArgument(context.getDescriptor() == descriptor);
		if(log.isDebugEnabled()){
			log.debug("Retrieving content via resolver id=\"{}\" name=\"{}\"",
					descriptor.getId(),
					descriptor.getName());
		}
		return Content.builder()
				.resolver(this)
				.content(doResolve(context))
				.ticker(context.getTicker())
				.build();
	}

	/**
	 * Performs an actual content resolution
	 *
	 * @param context a policy information context
	 * @return {@link Node} a resolved content or {@code null}
	 * @throws Exception if an error occurs
	 */
	abstract Node doResolve(
			ResolverContext context)
		throws Exception;


	@Override
	public final int getPreferredCacheTTL() {
		return descriptor.getPreferredCacheTTL();
	}

	@Override
	public final void setPreferredCacheTTL(int ttl) {
		if(descriptor.isCacheable()
				&& ttl > 0){
			this.preferredCacheTTL.set(ttl);
		}
	}
}
