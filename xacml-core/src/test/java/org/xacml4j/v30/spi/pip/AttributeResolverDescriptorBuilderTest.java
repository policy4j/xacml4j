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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.Categories;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.collect.Iterables;

public class AttributeResolverDescriptorBuilderTest
{

	@Test(expected=IllegalArgumentException.class)
	public void testBuildDescriptiorWithContextKeyReferringToThisDescriptor()
	{
		AttributeResolverDescriptorBuilder.builder(
				"id", "name", "issuer", Categories.SUBJECT_ACCESS)
		.attribute("testId1", XacmlTypes.INTEGER)
		.attribute("testId2", XacmlTypes.STRING)
		.requestContextKey(Categories.SUBJECT_ACCESS, "testId1", XacmlTypes.INTEGER)
		.build();
	}

	@Test
	public void testBuildDescriptorWithIssuer()
	{
		AttributeResolverDescriptor d = AttributeResolverDescriptorBuilder.builder(
				"id", "name", "issuer", Categories.SUBJECT_ACCESS)
		.attribute("testId1", XacmlTypes.INTEGER)
		.attribute("testId2", XacmlTypes.STRING).build();
		assertEquals("id", d.getId());
		assertEquals("name", d.getName());
		assertEquals("issuer", d.getIssuer());
		assertEquals(Categories.SUBJECT_ACCESS, d.getCategory());

		AttributeDesignatorKey.Builder key = AttributeDesignatorKey
				.builder()
				.category(Categories.SUBJECT_ACCESS)
				.attributeId("testId1")
				.dataType(XacmlTypes.INTEGER);

		assertTrue(d.canResolve(key.build()));
		assertTrue(d.canResolve(key.issuer("issuer").build()));
		assertFalse(d.canResolve(key.dataType(XacmlTypes.STRING).build()));

		Map<AttributeDesignatorKey, AttributeDescriptor> byKey = d.getAttributesByKey();
		assertEquals(2, byKey.size());

		AttributeDesignatorKey.Builder key0 = AttributeDesignatorKey
				.builder()
				.category(Categories.SUBJECT_ACCESS)
				.attributeId("testId1")
				.dataType(XacmlTypes.INTEGER)
				.issuer("issuer");

		AttributeDesignatorKey.Builder key1 = AttributeDesignatorKey
				.builder()
				.category(Categories.SUBJECT_ACCESS)
				.attributeId("testId2")
				.dataType(XacmlTypes.STRING)
				.issuer("issuer");

		assertNotNull(byKey.get(key0.build()));
		assertNotNull(byKey.get(key1.build()));


		Iterable<AttributeDesignatorKey> keys = d.getAttributesByKey().keySet();

		assertEquals(key0.build(), Iterables.get(keys, 0));
		assertEquals(key1.build(), Iterables.get(keys, 1));
	}

	@Test
	public void testBuildDescriptorWithIssuerNull()
	{
		AttributeResolverDescriptor d = AttributeResolverDescriptorBuilder.builder(
				"id", "name", null, Categories.SUBJECT_ACCESS)
		.attribute("testId1", XacmlTypes.INTEGER)
		.attribute("testId2", XacmlTypes.STRING).build();
		assertEquals("id", d.getId());
		assertEquals("name", d.getName());
		assertNull(d.getIssuer());
		assertEquals(Categories.SUBJECT_ACCESS, d.getCategory());

		AttributeDesignatorKey.Builder key = AttributeDesignatorKey
				.builder()
				.category(Categories.SUBJECT_ACCESS)
				.attributeId("testId1")
				.dataType(XacmlTypes.INTEGER);

		assertTrue(d.canResolve(key.build()));
		assertFalse(d.canResolve(key.issuer("issuer").build()));
		assertFalse(d.canResolve(key.dataType(XacmlTypes.STRING).build()));


		Iterable<AttributeDesignatorKey> keys = d.getAttributesByKey().keySet();

		AttributeDesignatorKey.Builder key0 = AttributeDesignatorKey
				.builder()
				.category(Categories.SUBJECT_ACCESS)
				.attributeId("testId1")
				.dataType(XacmlTypes.INTEGER);
		AttributeDesignatorKey.Builder key1 = AttributeDesignatorKey
				.builder()
				.category(Categories.SUBJECT_ACCESS)
				.attributeId("testId2")
				.dataType(XacmlTypes.STRING);

		assertEquals(key0.build(), Iterables.get(keys, 0));
		assertEquals(key1.build(), Iterables.get(keys, 1));
	}
}
