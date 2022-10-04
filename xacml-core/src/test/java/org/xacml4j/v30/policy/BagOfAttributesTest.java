package org.xacml4j.v30.policy;

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

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.Value;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.types.XacmlTypes;

import java.util.Collection;
import java.util.LinkedList;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import static org.xacml4j.v30.types.XacmlTypes.INTEGER;
import static org.xacml4j.v30.types.XacmlTypes.STRING;


public class BagOfAttributesTest
{
	private EvaluationContext context;

	@Before
	public void init(){
		this.context = createStrictMock(EvaluationContext.class);
	}

	@Test
	public void testContains() throws Exception
	{
		BagOfValues bag = STRING.bag().attribute(STRING.of("1"), STRING.of("2"), STRING.of("3")).build();
		assertTrue(bag.contains(STRING.of("1")));
		assertTrue(bag.contains(STRING.of("2")));
		assertTrue(bag.contains(STRING.of("3")));
		assertFalse(bag.contains(STRING.of("4")));
	}

	@Test
	public void testEqualsEmptyBags()
	{
		assertEquals(STRING.bag().build(), STRING.bag().build());
		assertEquals(STRING.bag().build(), STRING.bag().build());
	}

	
	@Test
	public void testValue(){
		BagOfValues b = STRING.bag().attribute(
				XacmlTypes.STRING.of("1"),
				XacmlTypes.STRING.of("aaa"),
				XacmlTypes.STRING.of("BB")).build();
		Value v = b.value();
		assertEquals(XacmlTypes.STRING.of("1"), v);
	}
	
	@Test
	public void testCreateBagFromValues()
	{
		BagOfValues b = STRING.bag().attribute(
				XacmlTypes.STRING.of("1"),
				XacmlTypes.STRING.of("aaa"),
				XacmlTypes.STRING.of("BB")).build();
		assertTrue(b.contains(XacmlTypes.STRING.of("1")));
		assertTrue(b.contains(XacmlTypes.STRING.of("aaa")));
		assertTrue(b.contains(XacmlTypes.STRING.of("BB")));
		assertFalse(b.contains(XacmlTypes.STRING.of("aaaa")));
	}

	@Test
	public void testContainsAll() throws Exception
	{
		BagOfValues bag = INTEGER.bag().attribute(INTEGER.of(1), INTEGER.of(2), INTEGER.of(1)).build();
		assertTrue(bag.containsAll(INTEGER.bag().attribute(INTEGER.of(1), INTEGER.of(2)).build()));
		assertFalse(bag.containsAll(INTEGER.bag().attribute(INTEGER.of(1), INTEGER.of(3)).build()));
	}

	@Test
	public void testEqualsWithElementsInTheSameOrder()
	{
		Collection<Value> content1 = new LinkedList<Value>();
		content1.add(INTEGER.of(1));
		content1.add(INTEGER.of(2));
		content1.add(INTEGER.of(3));
		BagOfValues bag1 = INTEGER.bag().attributes(content1).build();

		Collection<Value> content2 = new LinkedList<Value>();
		content2.add(INTEGER.of(1));
		content2.add(INTEGER.of(2));
		content2.add(INTEGER.of(3));
		BagOfValues bag2 = INTEGER.bag().attributes(content2).build();

		assertEquals(bag1, bag2);

		Collection<Value> content3 = new LinkedList<Value>();
		content3.add(INTEGER.of(1));
		content3.add(INTEGER.of(3));
		content3.add(INTEGER.of(2));
		BagOfValues bag3 = INTEGER.bag().attributes(content3).build();

		assertTrue(bag1.equals(bag3));
		assertTrue(bag2.equals(bag3));
	}



	@Test(expected=IllegalArgumentException.class)
	public void testCreateWithDifferentAttributeTypes()
	{
		Collection<Value> attr = new LinkedList<Value>();
		attr.add(INTEGER.of(1));
		attr.add(XacmlTypes.STRING.of("aaa"));
		INTEGER.bag().attributes(attr).build();
	}

	@Test
	public void testEvaluateBag() throws EvaluationException
	{
		Collection<Value> content2 = new LinkedList<Value>();
		content2.add(INTEGER.of(3));
		content2.add(INTEGER.of(4));
		content2.add(INTEGER.of(5));
		BagOfValues bag2 = INTEGER.bag().attributes(content2).build();
		replay(context);
		assertSame(bag2, bag2.evaluate(context));
		verify(context);
	}

	@Test
	public void testUnion()
	{
		BagOfValues bag0 = INTEGER.bag().attribute(
				INTEGER.of(1),
				INTEGER.of(2),
				INTEGER.of(3),
				INTEGER.of(6)).build();
		assertEquals(4, bag0.size());

		BagOfValues bag1 = INTEGER.bag().attribute(
				INTEGER.of(2),
				INTEGER.of(2),
				INTEGER.of(7),
				INTEGER.of(6)).build();

		assertEquals(4, bag1.size());

		BagOfValues bag2 = bag0.union(bag1);

		assertTrue(bag2.contains(INTEGER.of(2)));
		assertTrue(bag2.contains(INTEGER.of(7)));
		assertTrue(bag2.contains(INTEGER.of(6)));
		assertTrue(bag2.contains(INTEGER.of(1)));
		assertTrue(bag2.contains(INTEGER.of(3)));
		assertEquals(8, bag2.size());

	}

	@Test
	public void testIntersection()
	{
		BagOfValues bag0 = INTEGER.bag().attribute(
				INTEGER.of(1),
				INTEGER.of(2),
				INTEGER.of(3),
				INTEGER.of(6)).build();

		BagOfValues bag1 = INTEGER.bag().attribute(
				INTEGER.of(9),
				INTEGER.of(2),
				INTEGER.of(6),
				INTEGER.of(4)).build();

		BagOfValues bag3 = bag0.intersection(bag1);
		assertTrue(bag3.contains(INTEGER.of(2)));
		assertTrue(bag3.contains(INTEGER.of(6)));
		assertEquals(2, bag3.size());
	}

	@Test
	public void testBuilder()
	{
		BagOfValues bag0 = INTEGER.bag().attribute(
				INTEGER.of(1),
				INTEGER.of(2),
				INTEGER.of(3)).build();
		BagOfValues bag1 = INTEGER.bag().attribute(
				INTEGER.of(2),
				INTEGER.of(1),
				INTEGER.of(3)).build();
		Iterable<Value> values = ImmutableList.<Value>of(
				INTEGER.of(2),
				INTEGER.of(1),
				INTEGER.of(3));
		BagOfValues bag3 = INTEGER.bag().attributes(values).build();
		BagOfValues bag4 = INTEGER.bag().attributes(values).build();
		assertEquals(bag0, bag1);
		assertEquals(bag1, bag3);
		assertEquals(bag1, bag4);
	}
}
