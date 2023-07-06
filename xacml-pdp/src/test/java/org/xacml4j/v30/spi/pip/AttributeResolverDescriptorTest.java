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

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.Clock;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.collect.ImmutableMap;

public class AttributeResolverDescriptorTest
{
	private IMocksControl c;
	private EvaluationContext context;
	@Before
	public void setUp(){
		this.c = EasyMock.createControl();
		this.context = c.createMock(EvaluationContext.class);
	}

	@Test(expected= SyntaxException.class)
	public void testBuildDescriptorWithContextKeyReferringToThisDescriptor()
	{
		AttributeResolverDescriptor.builder(
				"id", "name", "issuer", CategoryId.SUBJECT_ACCESS)
		.attribute("testId1", XacmlTypes.INTEGER)
		.attribute("testId2", XacmlTypes.STRING)
		.contextRef(AttributeDesignatorKey
				.builder()
				.category(CategoryId.SUBJECT_ACCESS)
				.issuer("issuer")
				.attributeId("testId1")
				.dataType(XacmlTypes.INTEGER)
				.build())
		.build((c)-> ImmutableMap.of());
	}

	@Test
	public void testBuildDescriptorWithIssuer()
	{
		AttributeResolverDescriptor d = AttributeResolverDescriptor
				.builder(
				"id", "name", "issuer", CategoryId.SUBJECT_ACCESS)
		.attribute("testId1", XacmlTypes.INTEGER, "testAlias1")
		.attribute("testId2", XacmlTypes.STRING, "testAlias2")
				.build((c)-> ImmutableMap.of("testId1", XacmlTypes.INTEGER.ofAny(10).toBag()));
		assertEquals("id", d.getId());
		assertEquals("name", d.getName());
		assertEquals("issuer", d.getIssuer());
		assertTrue(d.getCategory().equals(CategoryId.SUBJECT_ACCESS));

		AttributeDesignatorKey.Builder key = AttributeDesignatorKey
				.builder()
				.category(CategoryId.SUBJECT_ACCESS)
				.attributeId("testId1")
				.dataType(XacmlTypes.INTEGER);

		assertTrue(d.canResolve(key.build()));
		assertTrue(d.canResolve(key.issuer("issuer").build()));
		assertFalse(d.canResolve(key.dataType(XacmlTypes.STRING).build()));

		Map<AttributeDesignatorKey, AttributeDescriptor> byKey = d.getAttributesByKey();
		assertEquals(4, byKey.size());

		AttributeDesignatorKey.Builder key0 = AttributeDesignatorKey
				.builder()
				.category(CategoryId.SUBJECT_ACCESS)
				.attributeId("testId1")
				.dataType(XacmlTypes.INTEGER)
				.issuer("issuer");

		AttributeDesignatorKey.Builder key1 = AttributeDesignatorKey
				.builder()
				.category(CategoryId.SUBJECT_ACCESS)
				.attributeId("testId2")
				.dataType(XacmlTypes.STRING)
				.issuer("issuer");

		assertNotNull(byKey.get(key0.build()));
		assertNotNull(byKey.get(key1.build()));

		expect(context.getClock()).andReturn(Clock.systemUTC());
		c.replay();
		Function<ResolverContext, Optional<AttributeSet>> r = d.getResolverFunction();
		ResolverContext resolverContext = ResolverContext.createContext(context, d);
		assertEquals(XacmlTypes.INTEGER.ofAny(10).toBag(),
		             r.apply(resolverContext).get().get("testId1").get());
		c.verify();
	}

	@Test
	public void testBuildDescriptorWithIssuerNull()
	{
		AttributeResolverDescriptor d = AttributeResolverDescriptor
				.builder("id", "name", CategoryId.SUBJECT_ACCESS)
		.attribute("testId1", XacmlTypes.INTEGER)
		.attribute("testId2", XacmlTypes.STRING).build((c)->ImmutableMap.of());
		assertEquals("id", d.getId());
		assertEquals("name", d.getName());
		assertNull(d.getIssuer());
		assertTrue(d.getCategory().equals(CategoryId.SUBJECT_ACCESS));

		AttributeDesignatorKey.Builder key = AttributeDesignatorKey
				.builder()
				.category(CategoryId.SUBJECT_ACCESS)
				.attributeId("testId1")
				.dataType(XacmlTypes.INTEGER);

		assertTrue(d.canResolve(key.build()));
		assertFalse(d.canResolve(key.issuer("issuer").build()));
		assertFalse(d.canResolve(key.dataType(XacmlTypes.STRING).build()));


		Map<AttributeDesignatorKey, AttributeDescriptor> keys = d.getAttributesByKey();

		AttributeDesignatorKey.Builder key0 = AttributeDesignatorKey
				.builder()
				.category(CategoryId.SUBJECT_ACCESS)
				.attributeId("testId1")
				.dataType(XacmlTypes.INTEGER);
		AttributeDesignatorKey.Builder key1 = AttributeDesignatorKey
				.builder()
				.category(CategoryId.SUBJECT_ACCESS)
				.attributeId("testId2")
				.dataType(XacmlTypes.STRING);

		assertTrue(keys.containsKey(key0.build()));
		assertTrue(keys.containsKey(key1.build()));
	}
}
