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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.BagOfAttributeValues;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.collect.Iterables;

public class AttributeSetTest
{
	private AttributeResolverDescriptor noIssuer;
	private AttributeResolverDescriptor withIssuer;

	@Before
	public void init(){
		this.noIssuer = AttributeResolverDescriptorBuilder.builder(
				"id", "name", null, CategoryId.SUBJECT_ACCESS)
				.attribute("testId1", XacmlTypes.INTEGER)
				.attribute("testId2", XacmlTypes.STRING)
				.build();
		this.withIssuer = AttributeResolverDescriptorBuilder.builder(
				"id", "name", "issuer", CategoryId.SUBJECT_ACCESS)
				.attribute("testId1", XacmlTypes.INTEGER)
				.attribute("testId2", XacmlTypes.STRING)
				.build();
		assertEquals("issuer", withIssuer.getIssuer());
	}

	@Test
	public void testAttributeSetWithIssuer()
	{
		AttributeDesignatorKey.Builder key =
				AttributeDesignatorKey
				.builder()
				.category(CategoryId.SUBJECT_ACCESS)
				.attributeId("testId1")
				.dataType(XacmlTypes.INTEGER)
				.issuer("issuer");

		AttributeDesignatorKey.Builder key1 =
				AttributeDesignatorKey
				.builder()
				.category(CategoryId.SUBJECT_ACCESS)
				.attributeId("testId2")
				.issuer("issuer")
				.dataType(XacmlTypes.STRING);

		AttributeSet v = AttributeSet
				.builder(withIssuer)
				.build();
		assertNotNull(v.get("testId1"));
		assertNotNull(v.get("testId2"));
		BagOfAttributeValues v1 = v.get(key.build());
		assertNotNull(v1);
		assertTrue(v1.isEmpty());
		assertEquals(XacmlTypes.INTEGER, v1.getDataType());

		BagOfAttributeValues v2 = v.get(key.build());
		assertNotNull(v2);
		assertTrue(v1.isEmpty());
		assertEquals(XacmlTypes.INTEGER, v1.getDataType());

		Iterable<AttributeDesignatorKey> keys = v.getAttributeKeys();
		assertEquals(key.build(), Iterables.get(keys, 0));
		assertEquals(key1.build(), Iterables.get(keys, 1));
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
		BagOfAttributeValues v1 = v.get(key.build());
		assertNotNull(v1);
		assertTrue(v1.isEmpty());
		assertEquals(XacmlTypes.INTEGER, v1.getDataType());
	}

	@Test
	public void testIsEmpty(){
		AttributeSet v = AttributeSet
				.builder(noIssuer)
				.build();
		assertTrue(v.isEmpty());
	}

}
