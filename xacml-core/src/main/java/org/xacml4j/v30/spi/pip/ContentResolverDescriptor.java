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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.Content;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public interface ContentResolverDescriptor
		extends ResolverDescriptor
{

	static Resolver<ContentRef> of(ContentResolverDescriptor d,
									 Function<ResolverContext, Content> function)

	{
		return new ContentResolver(d, function);
	}

	final class ContentResolver implements Resolver<ContentRef>
	{
		private final static Logger log = LoggerFactory.getLogger(ContentResolver.class);

		private ContentResolverDescriptor descriptor;
		private Function<ResolverContext, Content> function;

		public ContentResolver(ContentResolverDescriptor descriptor,
							   Function<ResolverContext, Content> function){
			Preconditions.checkArgument(descriptor != null);
			this.descriptor = Objects.requireNonNull(descriptor,
					ContentResolverDescriptor.class.getSimpleName());
			this.function = Objects.requireNonNull(function, "Content resolution function");
		}

		@Override
		public final ContentResolverDescriptor getDescriptor() {
			return descriptor;
		}

		@Override
		public Optional<ContentRef> resolve(
				ResolverContext context)
		{
			if(log.isDebugEnabled()){
				log.debug("Retrieving content via resolver id=\"{}\" name=\"{}\"",
						descriptor.getId(),
						descriptor.getName());
			}
			return Optional.ofNullable(function.apply(context))
					.map(c->ContentRef.builder()
							.resolver(getDescriptor())
							.content(c)
							.clock(context.getClock())
							.build());
		}

	}
}
