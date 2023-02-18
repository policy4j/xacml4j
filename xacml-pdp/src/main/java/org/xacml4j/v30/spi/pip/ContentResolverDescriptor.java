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


import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Content;

public class ContentResolverDescriptor
		extends BaseResolverDescriptor<ContentRef>
{
	private Function<ResolverContext, Content> contentFunction;

	private ContentResolverDescriptor(ContentResolverDescriptor.Builder b,
									  Function<ResolverContext, Content> contentFunction) {
		super(b);
		this.contentFunction = Objects.requireNonNull(contentFunction, "contentFunction");
	}
	public static Builder builder(String id, String name, CategoryId categoryId){
		return new Builder(id, name, categoryId);
	}

	@Override
	public boolean canResolve(AttributeReferenceKey key) {
		if(!(key instanceof AttributeSelectorKey)){
			return false;
		}
		return getCategory()
				.equals(key.getCategory());
	}

	@Override
	public Function<ResolverContext, Optional<ContentRef>> getResolverFunction() {
		return (context)->Optional.ofNullable(contentFunction.apply(context))
				.map(c->ContentRef.builder()
						.resolver(this)
						.content(c)
						.clock(context.getClock())
						.build());
	}

	final static class Builder
			extends BaseBuilder<ContentRef, ContentResolverDescriptor.Builder>
	{
		private Builder(
				String id,
				String name,
				CategoryId categoryId){
			super(id, name, categoryId);
		}

		@Override
		protected Builder getThis() {
			return ContentResolverDescriptor.Builder.this;
		}

		public ContentResolverDescriptor build(Function<ResolverContext, Content> resolverFunction){
			return new ContentResolverDescriptor(this, resolverFunction);
		}
	}

}
