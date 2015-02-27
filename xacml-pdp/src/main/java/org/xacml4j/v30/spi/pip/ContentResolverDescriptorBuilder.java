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
import com.google.common.collect.Iterables;
import org.xacml4j.v30.*;

import java.util.LinkedList;
import java.util.List;

public final class ContentResolverDescriptorBuilder
{
	private String id;
	private String name;
	private CategoryId category;
	private List<AttributeReferenceKey> keys;
	private int cacheTTL;

	private ContentResolverDescriptorBuilder(String id, String name, CategoryId category)
	{
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(name);
		Preconditions.checkNotNull(category);
		this.id = id.replace(":", ".");
		this.name = name;
		this.category = category;
		this.keys = new LinkedList<AttributeReferenceKey>();
	}

	public static ContentResolverDescriptorBuilder builder(String id, String name, CategoryId category){
		return new ContentResolverDescriptorBuilder(id, name, category);
	}

	public ContentResolverDescriptorBuilder designatorRef(CategoryId category,
			String attributeId, AttributeExpType dataType, String issuer)
	{
		this.keys.add(AttributeDesignatorKey
				.builder()
				.category(category)
				.dataType(dataType)
				.attributeId(attributeId)
				.issuer(issuer)
				.build());
		return this;
	}

	public ContentResolverDescriptorBuilder selectorRef(
			CategoryId category,
			String xpath, AttributeExpType dataType,
			String contextAttributeId)
	{
		this.keys.add(AttributeSelectorKey
				.builder()
				.category(category)
				.xpath(xpath)
				.dataType(dataType)
				.contextSelectorId(contextAttributeId)
				.build());
		return this;
	}

	public ContentResolverDescriptorBuilder keys(Iterable<AttributeReferenceKey> keys){
		Iterables.addAll(this.keys, keys);
		return this;
	}

	public ContentResolverDescriptorBuilder noCache(){
		this.cacheTTL = -1;
		return this;
	}

	public ContentResolverDescriptorBuilder cache(int ttl){
		this.cacheTTL = ttl;
		return this;
	}

	public ContentResolverDescriptor build(){
		return new ContentResolverDescriptorImpl();
	}

	public class ContentResolverDescriptorImpl
		extends BaseResolverDescriptor implements ContentResolverDescriptor
	{

		public ContentResolverDescriptorImpl() {
			super(id, name, category, keys, cacheTTL);
		}

		@Override
		public boolean canResolve(CategoryId category) {
			return getCategory().equals(category);
		}
	}
}
