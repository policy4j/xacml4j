package org.xacml4j.v30;

/*
 * #%L
 * Xacml4J Policy Unit Test Support
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

import java.util.Map;

import org.xacml4j.v30.spi.pip.AttributeResolverDescriptor;

import com.google.common.collect.ImmutableMap;


public class ExpectedAttributeResolverBuilder
{
	private AttributeResolverDescriptor.Builder b;
	private ImmutableMap.Builder<String, BagOfValues> values;

	private ExpectedAttributeResolverBuilder(AttributeResolverDescriptor.Builder b){
		this.b = b;
		this.values = ImmutableMap.builder();
	}

	public static ExpectedAttributeResolverBuilder builder(String id, CategoryId category, String issuer){
		return new ExpectedAttributeResolverBuilder(AttributeResolverDescriptor.builder(id,
				"ExpectedAttributeResolver " + id, issuer, category));
	}

	public static ExpectedAttributeResolverBuilder builder(String id, CategoryId category){
		return builder(id, category, null);
	}

	public ExpectedAttributeResolverBuilder designatorKeyRef(
			CategoryId category, String attributeId, ValueType type)
	{
		b.contextRef(AttributeDesignatorKey.builder()
		                                   .category(category)
		                                   .attributeId(attributeId)
		                                   .dataType(type).build());
		return this;
	}
	public ExpectedAttributeResolverBuilder value(String id, Value value){
		b.attribute(id, value.getEvaluatesTo());
		this.values.put(id, value.toBag());
		return this;
	}

	public ExpectedAttributeResolverBuilder value(String id, BagOfValues value){
		b.attribute(id, value.getBagValueType());
		this.values.put(id, value);
		return this;
	}

	public AttributeResolverDescriptor build(Map<String, BagOfValues> values){
		return b.build(r->values);
	}
}
