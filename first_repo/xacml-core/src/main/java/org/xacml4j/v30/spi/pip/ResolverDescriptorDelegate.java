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

import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.CategoryId;

import com.google.common.base.Preconditions;

class ResolverDescriptorDelegate implements ResolverDescriptor
{
	private ResolverDescriptor d;

	protected ResolverDescriptorDelegate(ResolverDescriptor d){
		Preconditions.checkNotNull(d);
		this.d = d;
	}

	@Override
	public String getId() {
		return d.getId();
	}

	@Override
	public String getName() {
		return d.getName();
	}

	@Override
	public CategoryId getCategory() {
		return d.getCategory();
	}

	@Override
	public List<AttributeReferenceKey> getKeyRefs() {
		return d.getKeyRefs();
	}

	@Override
	public boolean isCacheable() {
		return d.isCacheable();
	}

	@Override
	public int getPreferredCacheTTL() {
		return d.getPreferredCacheTTL();
	}
}
