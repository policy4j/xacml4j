package com.artagon.xacml.v30.pdp;

import static com.artagon.xacml.v30.types.IntegerType.INTEGER;
import static com.artagon.xacml.v30.types.StringType.STRING;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v30.AttributeExp;
import com.artagon.xacml.v30.BagOfAttributeExp;
import com.artagon.xacml.v30.BagOfAttributeExpType;
import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.EvaluationException;
import com.artagon.xacml.v30.types.IntegerType;
import com.artagon.xacml.v30.types.StringType;
import com.google.common.collect.ImmutableList;

public class BagOfAttributesTest
{
	private StringType stringType;
	private IntegerType intType;
	private EvaluationContext context;

	@Before
	public void init(){
		this.stringType = STRING;
		this.intType = INTEGER;
		this.context = createStrictMock(EvaluationContext.class);
	}

	@Test
	public void testContains() throws Exception
	{
		BagOfAttributeExpType bagType = stringType.bagType();
		Collection<AttributeExp> content = new LinkedList<AttributeExp>();
		content.add(stringType.create("1"));
		content.add(stringType.create("2"));
		content.add(stringType.create("3"));
		BagOfAttributeExp bag = bagType.create(content);
		assertTrue(bag.contains(stringType.create("1")));
		assertTrue(bag.contains(stringType.create("2")));
		assertTrue(bag.contains(stringType.create("3")));
		assertFalse(bag.contains(stringType.create("4")));
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
		BagOfAttributeExp b = StringType.STRING.bagOf("1", "aaa", "BB");
		assertTrue(b.contains(StringType.STRING.create("1")));
		assertTrue(b.contains(StringType.STRING.create("aaa")));
		assertTrue(b.contains(StringType.STRING.create("BB")));
		assertFalse(b.contains(StringType.STRING.create("aaaa")));
	}

	@Test
	public void testContainsAll() throws Exception
	{
		Collection<AttributeExp> content = new LinkedList<AttributeExp>();
		content.add(intType.create(1));
		content.add(intType.create(2));
		content.add(intType.create(1));
		BagOfAttributeExp bag = intType.bagOf(content);
		Collection<AttributeExp> test = new LinkedList<AttributeExp>();
		test.add(intType.create(1));
		test.add(intType.create(2));
		assertTrue(bag.containsAll(intType.bagOf(test)));
		test = new LinkedList<AttributeExp>();
		test.add(intType.create(1));
		test.add(intType.create(3));
		assertFalse(bag.containsAll(intType.bagOf(test)));
	}

	@Test
	public void testEqualsWithElementsInTheSameOrder()
	{
		Collection<AttributeExp> content1 = new LinkedList<AttributeExp>();
		content1.add(intType.create(1));
		content1.add(intType.create(2));
		content1.add(intType.create(3));
		BagOfAttributeExp bag1 = intType.bagOf(content1);

		Collection<AttributeExp> content2 = new LinkedList<AttributeExp>();
		content2.add(intType.create(1));
		content2.add(intType.create(2));
		content2.add(intType.create(3));
		BagOfAttributeExp bag2 = intType.bagOf(content2);

		assertEquals(bag1, bag2);

		Collection<AttributeExp> content3 = new LinkedList<AttributeExp>();
		content3.add(intType.create(1));
		content3.add(intType.create(3));
		content3.add(intType.create(2));
		BagOfAttributeExp bag3= intType.bagOf(content3);

		assertTrue(bag1.equals(bag3));
		assertTrue(bag2.equals(bag3));
	}



	@Test(expected=IllegalArgumentException.class)
	public void testCreateWithDifferentAttributeTypes()
	{
		Collection<AttributeExp> attr = new LinkedList<AttributeExp>();
		attr.add(intType.create(1));
		attr.add(stringType.create("aaa"));
		intType.bagOf(attr);
	}

	@Test
	public void testEvaluateBag() throws EvaluationException
	{
		Collection<AttributeExp> content2 = new LinkedList<AttributeExp>();
		content2.add(intType.create(3));
		content2.add(intType.create(4));
		content2.add(intType.create(5));
		BagOfAttributeExp bag2 = intType.bagOf(content2);
		replay(context);
		assertSame(bag2, bag2.evaluate(context));
		verify(context);
	}

	@Test
	public void testUnion()
	{
		BagOfAttributeExp bag0 = intType.bagOf(
				intType.create(1),
				intType.create(2),
				intType.create(3),
				intType.create(6));

		BagOfAttributeExp bag1 = intType.bagOf(
				intType.create(2),
				intType.create(2),
				intType.create(7),
				intType.create(6));

		BagOfAttributeExp bag3 = bag0.union(bag1);

		assertTrue(bag3.contains(intType.create(2)));
		assertTrue(bag3.contains(intType.create(7)));
		assertTrue(bag3.contains(intType.create(6)));
		assertTrue(bag3.contains(intType.create(1)));
		assertTrue(bag3.contains(intType.create(3)));
		assertEquals(5, bag3.size());

	}

	@Test
	public void testIntersection()
	{
		BagOfAttributeExp bag0 = intType.bagOf(1, 2, 3, 6);

		BagOfAttributeExp bag1 = intType.bagOf(2, 2, 7, 6);

		BagOfAttributeExp bag3 = bag0.intersection(bag1);
		assertTrue(bag3.contains(INTEGER.create(2)));
		assertTrue(bag3.contains(INTEGER.create(6)));
		assertEquals(2, bag3.size());
	}

	@Test
	public void testBuilder()
	{
		BagOfAttributeExp bag0 = INTEGER.bag().value(1,  2, 3).build();
		BagOfAttributeExp bag1 = INTEGER.bag().value(INTEGER.create(2),  INTEGER.create(1), INTEGER.create(3)).build();
		BagOfAttributeExp bag2 = INTEGER.bag().value(INTEGER.create(1),  2, 3).build();
		Iterable<AttributeExp> values = ImmutableList.<AttributeExp>of(INTEGER.create(2),  INTEGER.create(1), INTEGER.create(3));
		BagOfAttributeExp bag3 = INTEGER.bag().values(values).build();
		BagOfAttributeExp bag4 = INTEGER.bag().attributes(values).build();
		assertEquals(bag0, bag1);
		assertEquals(bag1, bag2);
		assertEquals(bag2, bag3);
		assertEquals(bag2, bag4);
	}
}
