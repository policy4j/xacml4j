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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.Attribute.Builder;
import org.xacml4j.v30.types.IntegerExp;
import org.xacml4j.v30.types.StringExp;

import com.google.common.collect.ImmutableSet;


public class AttributeTest
{
	private Collection<AttributeExp> values;

	@Before
	public void init()
	{
		this.values = new LinkedList<AttributeExp>();
		values.add(IntegerExp.valueOf(1));
		values.add(IntegerExp.valueOf(1));
		values.add(IntegerExp.valueOf(3));
		values.add(IntegerExp.valueOf(2));
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
				.value(StringExp.valueOf("value1"), StringExp.valueOf("value2"))
				.build();
		assertEquals("testId", attr.getAttributeId());
		assertEquals(null, attr.getIssuer());
		assertFalse(attr.isIncludeInResult());
		assertEquals(2, attr.getValues().size());
		assertTrue(attr.getValues().contains(StringExp.valueOf("value1")));
		assertTrue(attr.getValues().contains(StringExp.valueOf("value2")));
	}

	@Test
	public void testCreateWithTheSameValues()
	{
		Collection<AttributeExp> values = new LinkedList<AttributeExp>();
		values.add(IntegerExp.valueOf(1));
		values.add(IntegerExp.valueOf(1));
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
				.value(IntegerExp.valueOf(1), IntegerExp.valueOf(2), IntegerExp.valueOf(3), IntegerExp.valueOf(2))
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
		Iterable<AttributeExp> a = ImmutableSet.<AttributeExp>of(StringExp.valueOf("test1"), StringExp.valueOf("test2"));
		Attribute.builder("testId")
		.values(a)
		.value(StringExp.valueOf("test2"), StringExp.valueOf("test3"))
		.build();
	}

}
