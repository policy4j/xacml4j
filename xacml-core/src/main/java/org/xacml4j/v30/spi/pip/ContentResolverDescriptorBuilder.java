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

import java.util.*;

import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.CategoryId;

public final class ContentResolverDescriptorBuilder extends ResolverDescriptorBuilder<ContentResolverDescriptorBuilder>
{

	private ContentResolverDescriptorBuilder(String id, String name, CategoryId ... category)
	{
		super(id, name, category);
	}

	public static ContentResolverDescriptorBuilder builder(String id, String name, CategoryId category){
		return new ContentResolverDescriptorBuilder(id, name, category);
	}


	@Override
	protected ContentResolverDescriptorBuilder getThis() {
		return this;
	}

	public ContentResolverDescriptor build(){
		return new ContentResolverDescriptorImpl();
	}

	public class ContentResolverDescriptorImpl
		extends BaseResolverDescriptor implements ContentResolverDescriptor
	{

		public ContentResolverDescriptorImpl() {
			super(id, name, allCategories, cacheTTL, contextKeysResolutionPlan);
		}

		public Collection<CategoryId> getSupportedCategories(){
			return allCategories;
		}
		public boolean canResolve(AttributeReferenceKey category) {
			if(!(category instanceof AttributeSelectorKey)){
				return false;
			}
			return allCategories
					.contains(category.getCategory());
		}
	}
}
