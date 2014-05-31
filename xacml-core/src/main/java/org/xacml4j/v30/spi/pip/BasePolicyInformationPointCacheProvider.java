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

import java.util.List;

import org.xacml4j.v30.BagOfAttributeExp;

import com.google.common.base.Preconditions;

public class BasePolicyInformationPointCacheProvider implements PolicyInformationPointCacheProvider
{

	@Override
	public final Content getContent(ResolverContext context) {
		ContentResolverDescriptor d = (ContentResolverDescriptor)context.getDescriptor();
		return d.isCacheable()?doGetContent(d, context.getKeys()):null;
	}

	@Override
	public final void putContent(ResolverContext context, Content content) {
		ContentResolverDescriptor d = (ContentResolverDescriptor)context.getDescriptor();
		Preconditions.checkArgument(context.getDescriptor() == content.getDescriptor());
		if(d.isCacheable()){
			doPutContent(d, context.getKeys(), content);
		}
	}

	@Override
	public final AttributeSet getAttributes(ResolverContext context) {
		AttributeResolverDescriptor d = (AttributeResolverDescriptor)context.getDescriptor();
		return d.isCacheable()?doGetAttributes(d, context.getKeys()):null;
	}

	@Override
	public final void putAttributes(ResolverContext context, AttributeSet v) {
		AttributeResolverDescriptor d = (AttributeResolverDescriptor)context.getDescriptor();
		Preconditions.checkArgument(d.getId().equals(v.getDescriptor().getId()));
		if(d.isCacheable()){
			doPutAttributes(d, context.getKeys(), v);
		}
	}

	protected Content doGetContent(ContentResolverDescriptor d, List<BagOfAttributeExp> keys){
		return null;
	}

	protected AttributeSet doGetAttributes(AttributeResolverDescriptor d,
			List<BagOfAttributeExp> keys) {
		return null;
	}

	protected void doPutContent(ContentResolverDescriptor d, List<BagOfAttributeExp> keys,
			Content content) {
	}

	protected void doPutAttributes(AttributeResolverDescriptor d, List<BagOfAttributeExp> keys,
			AttributeSet v) {

	}
}
