package org.xacml4j.v30.pdp;

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

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.xacml4j.v30.types.XacmlTypes.INTEGER;
import static org.xacml4j.v30.types.XacmlTypes.STRING;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.BagOfAttributeExpType;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.types.IntegerExp;
import org.xacml4j.v30.types.StringExp;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.collect.ImmutableList;

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
		BagOfAttributeExpType bagType = XacmlTypes.STRING.bagType();
		Collection<AttributeExp> content = new LinkedList<AttributeExp>();
		content.add(StringExp.valueOf("1"));
		content.add(StringExp.valueOf("2"));
		content.add(StringExp.valueOf("3"));
		BagOfAttributeExp bag = bagType.create(content);
		assertTrue(bag.contains(StringExp.valueOf("1")));
		assertTrue(bag.contains(StringExp.valueOf("2")));
		assertTrue(bag.contains(StringExp.valueOf("3")));
		assertFalse(bag.contains(StringExp.valueOf("4")));
	}

	@Test
	public void testEqualsEmptyBags()
	{
		assertEquals(STRING.emptyBag(), STRING.emptyBag());
		assertEquals(STRING.emptyBag(), STRING.emptyBag());
	}

	@Test
	public void testCreateBagFromValues()
	{
		BagOfAttributeExp b = STRING.bagOf(
				StringExp.valueOf("1"),
				StringExp.valueOf("aaa"),
				StringExp.valueOf("BB"));
		assertTrue(b.contains(StringExp.valueOf("1")));
		assertTrue(b.contains(StringExp.valueOf("aaa")));
		assertTrue(b.contains(StringExp.valueOf("BB")));
		assertFalse(b.contains(StringExp.valueOf("aaaa")));
	}

	@Test
	public void testContainsAll() throws Exception
	{
		Collection<AttributeExp> content = new LinkedList<AttributeExp>();
		content.add(IntegerExp.valueOf(1));
		content.add(IntegerExp.valueOf(2));
		content.add(IntegerExp.valueOf(1));
		BagOfAttributeExp bag = INTEGER.bagOf(content);
		Collection<AttributeExp> test = new LinkedList<AttributeExp>();
		test.add(IntegerExp.valueOf(1));
		test.add(IntegerExp.valueOf(2));
		assertTrue(bag.containsAll(INTEGER.bagOf(test)));
		test = new LinkedList<AttributeExp>();
		test.add(IntegerExp.valueOf(1));
		test.add(IntegerExp.valueOf(3));
		assertFalse(bag.containsAll(INTEGER.bagOf(test)));
	}

	@Test
	public void testEqualsWithElementsInTheSameOrder()
	{
		Collection<AttributeExp> content1 = new LinkedList<AttributeExp>();
		content1.add(IntegerExp.valueOf(1));
		content1.add(IntegerExp.valueOf(2));
		content1.add(IntegerExp.valueOf(3));
		BagOfAttributeExp bag1 = INTEGER.bagOf(content1);

		Collection<AttributeExp> content2 = new LinkedList<AttributeExp>();
		content2.add(IntegerExp.valueOf(1));
		content2.add(IntegerExp.valueOf(2));
		content2.add(IntegerExp.valueOf(3));
		BagOfAttributeExp bag2 = INTEGER.bagOf(content2);

		assertEquals(bag1, bag2);

		Collection<AttributeExp> content3 = new LinkedList<AttributeExp>();
		content3.add(IntegerExp.valueOf(1));
		content3.add(IntegerExp.valueOf(3));
		content3.add(IntegerExp.valueOf(2));
		BagOfAttributeExp bag3= INTEGER.bagOf(content3);

		assertTrue(bag1.equals(bag3));
		assertTrue(bag2.equals(bag3));
	}



	@Test(expected=IllegalArgumentException.class)
	public void testCreateWithDifferentAttributeTypes()
	{
		Collection<AttributeExp> attr = new LinkedList<AttributeExp>();
		attr.add(IntegerExp.valueOf(1));
		attr.add(StringExp.valueOf("aaa"));
		INTEGER.bagOf(attr);
	}

	@Test
	public void testEvaluateBag() throws EvaluationException
	{
		Collection<AttributeExp> content2 = new LinkedList<AttributeExp>();
		content2.add(IntegerExp.valueOf(3));
		content2.add(IntegerExp.valueOf(4));
		content2.add(IntegerExp.valueOf(5));
		BagOfAttributeExp bag2 = INTEGER.bagOf(content2);
		replay(context);
		assertSame(bag2, bag2.evaluate(context));
		verify(context);
	}

	@Test
	public void testUnion()
	{
		BagOfAttributeExp bag0 = INTEGER.bagOf(
				IntegerExp.valueOf(1),
				IntegerExp.valueOf(2),
				IntegerExp.valueOf(3),
				IntegerExp.valueOf(6));

		BagOfAttributeExp bag1 = INTEGER.bagOf(
				IntegerExp.valueOf(2),
				IntegerExp.valueOf(2),
				IntegerExp.valueOf(7),
				IntegerExp.valueOf(6));

		BagOfAttributeExp bag3 = bag0.union(bag1);

		assertTrue(bag3.contains(IntegerExp.valueOf(2)));
		assertTrue(bag3.contains(IntegerExp.valueOf(7)));
		assertTrue(bag3.contains(IntegerExp.valueOf(6)));
		assertTrue(bag3.contains(IntegerExp.valueOf(1)));
		assertTrue(bag3.contains(IntegerExp.valueOf(3)));
		assertEquals(5, bag3.size());

	}

	@Test
	public void testIntersection()
	{
		BagOfAttributeExp bag0 = INTEGER.bagOf(
				IntegerExp.valueOf(1),
				IntegerExp.valueOf(2),
				IntegerExp.valueOf(3),
				IntegerExp.valueOf(6));

		BagOfAttributeExp bag1 = INTEGER.bagOf(
				IntegerExp.valueOf(9),
				IntegerExp.valueOf(2),
				IntegerExp.valueOf(6),
				IntegerExp.valueOf(4));

		BagOfAttributeExp bag3 = bag0.intersection(bag1);
		assertTrue(bag3.contains(IntegerExp.valueOf(2)));
		assertTrue(bag3.contains(IntegerExp.valueOf(6)));
		assertEquals(2, bag3.size());
	}

	@Test
	public void testBuilder()
	{
		BagOfAttributeExp bag0 = INTEGER.bagOf(
				IntegerExp.valueOf(1),
				IntegerExp.valueOf(2),
				IntegerExp.valueOf(3));
		BagOfAttributeExp bag1 = INTEGER.bag().attribute(
				IntegerExp.valueOf(2),
				IntegerExp.valueOf(1),
				IntegerExp.valueOf(3)).build();
		Iterable<AttributeExp> values = ImmutableList.<AttributeExp>of(
				IntegerExp.valueOf(2),
				IntegerExp.valueOf(1),
				IntegerExp.valueOf(3));
		BagOfAttributeExp bag3 = INTEGER.bag().attributes(values).build();
		BagOfAttributeExp bag4 = INTEGER.bag().attributes(values).build();
		assertEquals(bag0, bag1);
		assertEquals(bag1, bag3);
		assertEquals(bag1, bag4);
	}
}
