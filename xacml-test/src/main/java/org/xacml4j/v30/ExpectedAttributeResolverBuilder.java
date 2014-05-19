package org.xacml4j.v30;

/*
 * #%L
 * Artagon XACML 3.0 conformance tests
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

import java.util.Map;

import org.xacml4j.v30.spi.pip.AttributeResolver;
import org.xacml4j.v30.spi.pip.AttributeResolverDescriptorBuilder;
import org.xacml4j.v30.spi.pip.BaseAttributeResolver;
import org.xacml4j.v30.spi.pip.ResolverContext;

import com.google.common.collect.ImmutableMap;


public class ExpectedAttributeResolverBuilder
{
	private AttributeResolverDescriptorBuilder b;
	private ImmutableMap.Builder<String, BagOfAttributeExp> values;

	private ExpectedAttributeResolverBuilder(AttributeResolverDescriptorBuilder b){
		this.b = b;
		this.values = ImmutableMap.builder();
	}

	public static ExpectedAttributeResolverBuilder builder(String id, CategoryId category, String issuer){
		return new ExpectedAttributeResolverBuilder(AttributeResolverDescriptorBuilder.builder(id, 
				"ExpectedAttributeResolver " + id, issuer, category));
	}

	public static ExpectedAttributeResolverBuilder builder(String id, CategoryId category){
		return builder(id, category, null);
	}

	public ExpectedAttributeResolverBuilder designatorKeyRef(
			CategoryId category, String attributeId, AttributeExpType type)
	{
		b.requestContextKey(category, attributeId, type);
		return this;
	}
	public ExpectedAttributeResolverBuilder value(String id, AttributeExp value){
		b.attribute(id, value.getType());
		this.values.put(id, value.toBag());
		return this;
	}

	public ExpectedAttributeResolverBuilder value(String id, BagOfAttributeExp value){
		b.attribute(id, value.getDataType());
		this.values.put(id, value);
		return this;
	}

	public AttributeResolver build(){
		return new BaseAttributeResolver(b.build()) {

			@Override
			protected Map<String, BagOfAttributeExp> doResolve(ResolverContext context)
					throws Exception {
				return values.build();
			}
		};
	}
}
