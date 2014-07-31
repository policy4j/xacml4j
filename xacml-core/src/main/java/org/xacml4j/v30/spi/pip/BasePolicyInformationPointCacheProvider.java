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

public abstract class BasePolicyInformationPointCacheProvider implements PolicyInformationPointCacheProvider {

	@Override
	public final Content getContent(ResolverContext context) {
		return context.getDescriptor().isCacheable() ? doGetContent(context) : null;
	}

	@Override
	public final void putContent(ResolverContext context, Content content) {
		Preconditions.checkArgument(context.getDescriptor().getId().equals(content.getDescriptor().getId()),
				"Content descriptor Id \"%s\" must match resolver context descriptor Id \"%s\"",
				content.getDescriptor().getId(), context.getDescriptor().getId());
		ContentResolverDescriptor d = (ContentResolverDescriptor) context.getDescriptor();
		if (d.isCacheable()) {
			doPutContent(context, content);
		}
	}

	@Override
	public final AttributeSet getAttributes(ResolverContext context) {
		return context.getDescriptor().isCacheable() ? doGetAttributes(context) : null;
	}

	@Override
	public final void putAttributes(ResolverContext context, AttributeSet v) {
		Preconditions.checkArgument(context.getDescriptor().getId().equals(v.getDescriptor().getId()),
				"Attribute set descriptor Id \"%s\" must match resolver context descriptor Id \"%s\"",
				v.getDescriptor().getId(), context.getDescriptor().getId());
		if (context.getDescriptor().isCacheable()) {
			doPutAttributes(context, v);
		}
	}

	protected abstract Content doGetContent(ResolverContext context);

	protected abstract AttributeSet doGetAttributes(ResolverContext context);

	protected abstract void doPutContent(ResolverContext context, Content content);

	protected abstract void doPutAttributes(ResolverContext context, AttributeSet v);
}
