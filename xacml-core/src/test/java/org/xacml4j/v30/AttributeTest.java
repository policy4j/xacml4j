package org.xacml4j.v30;

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

import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.Attribute.Builder;
import org.xacml4j.v30.types.XacmlTypes;

import java.util.Collection;
import java.util.LinkedList;

import static org.junit.Assert.*;


public class AttributeTest
{
	private Collection<AttributeValue> values;

	@Before
	public void init()
	{
		this.values = new LinkedList<AttributeValue>();
		values.add(XacmlTypes.INTEGER.of(1));
		values.add(XacmlTypes.INTEGER.of(1));
		values.add(XacmlTypes.INTEGER.of(3));
		values.add(XacmlTypes.INTEGER.of(2));
	}

	@Test
	public void testCreateWithAllArguments()
	{
		Attribute attr = Attribute
				.builder("testId")
				.issuer("testIssuer")
				.includeInResult(true)
				.values(values)
				.build();
		assertEquals("testId", attr.getAttributeId());
		assertEquals("testIssuer", attr.getIssuer());
		assertTrue(attr.isIncludeInResult());
		assertEquals(values.size(), attr.getValues().size());
		assertTrue(attr.getValues().containsAll(values));
		assertTrue(values.containsAll(attr.getValues()));
	}

	@Test
	public void testCreateMethod()
	{
		Attribute attr = Attribute
				.builder("testId")
				.value(XacmlTypes.STRING.of("value1"), XacmlTypes.STRING.of("value2"))
				.build();
		assertEquals("testId", attr.getAttributeId());
		assertEquals(null, attr.getIssuer());
		assertFalse(attr.isIncludeInResult());
		assertEquals(2, attr.getValues().size());
		assertTrue(attr.getValues().contains(XacmlTypes.STRING.of("value1")));
		assertTrue(attr.getValues().contains(XacmlTypes.STRING.of("value2")));
	}

	@Test
	public void testCreateWithTheSameValues()
	{
		Collection<AttributeValue> values = new LinkedList<AttributeValue>();
		values.add(XacmlTypes.INTEGER.of(1));
		values.add(XacmlTypes.INTEGER.of(1));
		Builder b = Attribute.builder("testId").issuer("testIssuer").includeInResult(true).values(values);
		Attribute attr = b.build();
		Attribute attr1 = b.issuer(null).build();
		Attribute attr2 = b.includeInResult(false).build();
		Attribute attr3 = b.includeInResult(true).issuer("testIssuer").build();
		assertEquals("testId", attr.getAttributeId());
		assertEquals("testIssuer", attr.getIssuer());
		assertTrue(attr.isIncludeInResult());
		assertEquals(values.size(), attr.getValues().size());
		assertTrue(attr.getValues().containsAll(values));
		assertTrue(values.containsAll(attr.getValues()));
		assertFalse(attr.equals(attr1));
		assertFalse(attr.equals(attr2));
		assertTrue(attr.equals(attr3));
	}


	@Test
	public void testCreateWithIdAndValuesVarArg()
	{
		Attribute attr = Attribute.builder("testId")
				.value(
						XacmlTypes.INTEGER.of(1),
						XacmlTypes.INTEGER.of(2),
						XacmlTypes.INTEGER.of(3),
						XacmlTypes.INTEGER.of(2))
				.build();
		assertEquals("testId", attr.getAttributeId());
		assertNull(attr.getIssuer());
		assertFalse(attr.isIncludeInResult());
		assertEquals(values.size(), attr.getValues().size());
		assertTrue(attr.getValues().containsAll(values));
		assertTrue(values.containsAll(attr.getValues()));
	}

	@Test
	public void testBuilder()
	{
		Iterable<AttributeValue> a = ImmutableSet.of(XacmlTypes.STRING.of("test1"), XacmlTypes.STRING.of("test2"));
		Attribute.builder("testId")
		.values(a)
		.value(XacmlTypes.STRING.of("test2"), XacmlTypes.STRING.of("test3"))
		.build();
	}

}
