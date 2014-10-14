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

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.Attribute.Builder;
import org.xacml4j.v30.types.*;

import com.google.common.collect.ImmutableSet;


public class AttributeTest
{
	private Collection<AttributeExp> values;

	@Before
	public void init()
	{
		this.values = new LinkedList<AttributeExp>();
		values.add(IntegerExp.of(1));
		values.add(IntegerExp.of(1));
		values.add(IntegerExp.of(3));
		values.add(IntegerExp.of(2));
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
		assertTrue(attr.containsAll(values));
	}

	@Test
	public void testCreateMethod()
	{
		Attribute attr = Attribute
				.builder("testId")
				.value(StringExp.of("value1"), StringExp.of("value2"))
				.build();
		assertEquals("testId", attr.getAttributeId());
		assertEquals(null, attr.getIssuer());
		assertFalse(attr.isIncludeInResult());
		assertEquals(2, attr.getValues().size());
		assertTrue(attr.getValues().contains(StringExp.of("value1")));
		assertTrue(attr.getValues().contains(StringExp.of("value2")));
	}

	@Test
	public void testCreateWithTheSameValues()
	{
		Collection<AttributeExp> values = new LinkedList<AttributeExp>();
		values.add(IntegerExp.of(1));
		values.add(IntegerExp.of(1));
		Builder b = Attribute.builder("testId").issuer("testIssuer").includeInResult(true).values(values);
		Attribute attr = b.build();
		Attribute attr1 = b.issuer(null).build();
		Attribute attr2 = b.includeInResult(false).build();
		Attribute attr3 = b.includeInResult(true).issuer("testIssuer").build();
		assertEquals("testId", attr.getAttributeId());
		assertEquals("testIssuer", attr.getIssuer());
		assertTrue(attr.isIncludeInResult());
		assertEquals(values.size(), attr.getValues().size());
		assertTrue(attr.containsAll(values));
		assertFalse(attr.equals(attr1));
		assertFalse(attr.equals(attr2));
		assertTrue(attr.equals(attr3));
	}

    @Test
    public void testBuilderValues(){

       Attribute a = Attribute
                .builder("testId")
                .value(StringExp.of("a"), null, StringExp.of("b"))
                .build();

       Attribute b = Attribute
               .builder("testId")
               .stringValue("a")
               .stringValues(ImmutableList.of("b"))
               .build();
        assertTrue(b.containsAll(StringExp.of("a")));
        assertTrue(b.containsAll(StringExp.of("b")));
        assertEquals(a, b);
    }

	@Test
	public void testBuilder()
	{
		Iterable<AttributeExp> a = ImmutableSet.<AttributeExp>of(StringExp.of("test1"), StringExp.of("test2"));
		Attribute.builder("testId")
		.values(a)
		.value(StringExp.of("test2"), StringExp.of("test3"))
		.build();
	}


    @Test
    public void testCopyOf(){
        Attribute a = Attribute.builder("testId")
                .value(StringExp.of("1"), StringExp.of("2"), StringExp.of("3"), StringExp.of("4"))
                .build();
        assertEquals(a, Attribute.builder().copyOf(a).build());


    }


    @Test
    public void testBuilderTypeValueMethods(){
        Calendar now = Calendar.getInstance();
        Attribute a =
                Attribute.builder()
                        .id("testId")
                        .stringValue("a")
                        .booleanValue(false)
                        .dateTimeValue(now)
                        .dateValue(now)
                        .intValue(1, 2, 4, 5, 7)
                        .ipAddressValue("192.168.1.2:80-443")
                        .ipAddressValues(ImmutableList.of("111.168.1.2:80-443"))
                        .entityValue(Entity.builder().build())
                .build();
        assertTrue(a.containsAll(StringExp.of("a")));
        assertTrue(a.containsAll(DateTimeExp.of(now)));
        assertTrue(a.containsAll(DateExp.of(now)));
        assertTrue(a.containsAll(EntityExp.of(Entity.builder().build())));
    }

}
