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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

import java.time.Clock;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.spi.pip.AttributeResolverDescriptor;
import org.xacml4j.v30.spi.pip.ResolverContext;
import org.xacml4j.v30.types.Value;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.collect.ImmutableMap;

public class ExpectedAttributeResolverBuilderTest
{
	@Test
	public void testScalarValuePreservesTypeIssuerAndResolvedValue()
	{
		Value<?> value = XacmlTypes.STRING.ofAny("preserved-value");
		AttributeResolverDescriptor descriptor = ExpectedAttributeResolverBuilder
				.builder("resolver-id", CategoryId.RESOURCE, "issuer-id")
				.value("attribute-id", value)
				.build(ImmutableMap.of("attribute-id", value.toBag()));
		assertEquals(XacmlTypes.STRING, descriptor.getAttribute("attribute-id").get().getDataType());
		assertEquals(CategoryId.RESOURCE, descriptor.getCategory());
		assertEquals("issuer-id", descriptor.getIssuer());
		AttributeDesignatorKey.Builder key = AttributeDesignatorKey.builder()
				.category(CategoryId.RESOURCE).issuer("issuer-id")
				.attributeId("attribute-id").dataType(XacmlTypes.STRING);
		assertTrue(descriptor.canResolve(key.build()));
		assertFalse(descriptor.canResolve(key.dataType(XacmlTypes.INTEGER).build()));
		assertResolvedValue(descriptor, value.toBag());
	}

	@Test
	public void testBagValuePreservesMultiplicity()
	{
		BagOfValues bag = XacmlTypes.INTEGER.bagBuilder().value(7, 7).build();
		AttributeResolverDescriptor descriptor = ExpectedAttributeResolverBuilder
				.builder("resolver-id", CategoryId.RESOURCE)
				.value("attribute-id", bag).build(ImmutableMap.of("attribute-id", bag));
		assertEquals(XacmlTypes.INTEGER, descriptor.getAttribute("attribute-id").get().getDataType());
		assertResolvedValue(descriptor, bag);
	}

	private void assertResolvedValue(AttributeResolverDescriptor descriptor, BagOfValues expected)
	{
		EvaluationContext context = mock(EvaluationContext.class);
		when(context.getClock()).thenReturn(Clock.systemUTC());
		assertEquals(expected, descriptor.getResolverFunction()
				.apply(ResolverContext.createContext(context, descriptor)).get()
				.get("attribute-id").get());
		verify(context).getClock();
	}
}
