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
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.collect.ImmutableMap;

public class AttributeSetTest
{
	private AttributeResolverDescriptor noIssuer;
	private AttributeResolverDescriptor withIssuer;

	@Before
	public void init(){
		this.noIssuer = AttributeResolverDescriptor.builder(
				"id", "name", null, CategoryId.SUBJECT_ACCESS)
				.attribute("testId1", XacmlTypes.INTEGER)
				.attribute("testId2", XacmlTypes.STRING)
				.build((c)-> ImmutableMap.of());
		this.withIssuer = AttributeResolverDescriptor.builder(
				"id", "name", "issuer", CategoryId.SUBJECT_ACCESS)
				.attribute("testId1", XacmlTypes.INTEGER)
				.attribute("testId2", XacmlTypes.STRING)
				.build((c)-> ImmutableMap.of());
		assertEquals("issuer", withIssuer.getIssuer());
	}

	@Test
	public void testAttributeSetWithIssuer()
	{
		AttributeDesignatorKey key0 =
				AttributeDesignatorKey
				.builder()
				.category(CategoryId.SUBJECT_ACCESS)
				.attributeId("testId1")
				.dataType(XacmlTypes.INTEGER)
				.build();

		AttributeDesignatorKey key1 =
				AttributeDesignatorKey
				.builder()
				.category(CategoryId.SUBJECT_ACCESS)
				.attributeId("testId2")
				.issuer("issuer1")
				.dataType(XacmlTypes.STRING)
				.build();

		AttributeSet v0 = AttributeSet
				.builder(noIssuer)
				.attribute("testId1", XacmlTypes.INTEGER.ofAny(1).toBag())
				.attribute("testId2", XacmlTypes.STRING.ofAny("b").toBag())
				.build();
		AttributeSet v1 = AttributeSet
				.builder(withIssuer)
				.attribute("testId1", XacmlTypes.INTEGER.ofAny(1).toBag())
				.attribute("testId2", XacmlTypes.STRING.ofAny("b").toBag())
				.build();

		assertEquals(v0.get("testId1").get(), XacmlTypes.INTEGER.ofAny(1).toBag());
		assertEquals(v0.get(key0).get(), XacmlTypes.INTEGER.ofAny(1).toBag());

		assertEquals(v1.get(key1), Optional.empty());

	}

	@Test
	public void testAttributeSetWithNoIssuer()
	{
		AttributeDesignatorKey.Builder key =
				AttributeDesignatorKey
				.builder()
				.category(CategoryId.SUBJECT_ACCESS)
				.attributeId("testId1")
				.dataType(XacmlTypes.INTEGER);

		AttributeSet v = AttributeSet
				.builder(noIssuer)
				.build();
		assertNotNull(v.get("testId1"));
		assertNotNull(v.get("testId2"));
		Optional<BagOfValues> v1 = v.get(key.build());
		assertFalse(v1.isPresent());
	}


	@Test
	public void testIsEmpty(){
		AttributeSet v = AttributeSet
				.builder(noIssuer)
				.build();
		assertTrue(v.isEmpty());
	}

}
