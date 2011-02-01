package com.artagon.xacml.v30;

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

import com.artagon.xacml.v30.types.IntegerType;
import com.artagon.xacml.v30.types.StringType;

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
		BagOfAttributeValuesType bagType = stringType.bagType();
		Collection<AttributeValue> content = new LinkedList<AttributeValue>();
		content.add(stringType.create("1"));
		content.add(stringType.create("2"));
		content.add(stringType.create("3"));	
		BagOfAttributeValues bag = bagType.create(content);
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
		BagOfAttributeValues b = StringType.STRING.bagOf("1", "aaa", "BB");
		assertTrue(b.contains(StringType.STRING.create("1")));
		assertTrue(b.contains(StringType.STRING.create("aaa")));
		assertTrue(b.contains(StringType.STRING.create("BB")));
		assertFalse(b.contains(StringType.STRING.create("aaaa")));
	}
	
	@Test
	public void testContainsAll() throws Exception
	{
		Collection<AttributeValue> content = new LinkedList<AttributeValue>();
		content.add(intType.create(1));
		content.add(intType.create(2));
		content.add(intType.create(1));	
		BagOfAttributeValues bag = intType.bagOf(content);
		Collection<AttributeValue> test = new LinkedList<AttributeValue>();
		test.add(intType.create(1));
		test.add(intType.create(2));
		assertTrue(bag.containsAll(intType.bagOf(test)));		
		test = new LinkedList<AttributeValue>();
		test.add(intType.create(1));
		test.add(intType.create(3));
		assertFalse(bag.containsAll(intType.bagOf(test)));
	}
	
	@Test
	public void testEqualsWithElementsInTheSameOrder()
	{
		Collection<AttributeValue> content1 = new LinkedList<AttributeValue>();
		content1.add(intType.create(1));
		content1.add(intType.create(2));
		content1.add(intType.create(3));
		BagOfAttributeValues bag1 = intType.bagOf(content1);
		
		Collection<AttributeValue> content2 = new LinkedList<AttributeValue>();
		content2.add(intType.create(1));
		content2.add(intType.create(2));
		content2.add(intType.create(3));
		BagOfAttributeValues bag2 = intType.bagOf(content2);
		
		assertEquals(bag1, bag2);
		
		Collection<AttributeValue> content3 = new LinkedList<AttributeValue>();
		content3.add(intType.create(1));
		content3.add(intType.create(3));
		content3.add(intType.create(2));
		BagOfAttributeValues bag3= intType.bagOf(content3);
		
		assertTrue(bag1.equals(bag3));
		assertTrue(bag2.equals(bag3));
	}
	
	
	
	@Test(expected=IllegalArgumentException.class)
	public void testCreateWithDifferentAttributeTypes()
	{
		Collection<AttributeValue> attr = new LinkedList<AttributeValue>();
		attr.add(intType.create(1));
		attr.add(stringType.create("aaa"));
		intType.bagOf(attr);
	}
	
	@Test
	public void testEvaluateBag() throws EvaluationException
	{
		Collection<AttributeValue> content2 = new LinkedList<AttributeValue>();
		content2.add(intType.create(3));
		content2.add(intType.create(4));
		content2.add(intType.create(5));
		BagOfAttributeValues bag2 = intType.bagOf(content2);
		replay(context);
		assertSame(bag2, bag2.evaluate(context));	
		verify(context);
	}
	
	@Test
	public void testUnion()
	{
		BagOfAttributeValues bag0 = intType.bagOf(
				intType.create(1),
				intType.create(2),
				intType.create(3),
				intType.create(6));
		
		BagOfAttributeValues bag1 = intType.bagOf(
				intType.create(2),
				intType.create(2),
				intType.create(7),
				intType.create(6));
		
		BagOfAttributeValues bag3 = bag0.union(bag1);
		
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
		BagOfAttributeValues bag0 = intType.bagOf(1, 2, 3, 6);
		
		BagOfAttributeValues bag1 = intType.bagOf(2, 2, 7, 6);
		
		BagOfAttributeValues bag3 = bag0.intersection(bag1);
		assertTrue(bag3.contains(INTEGER.create(2)));
		assertTrue(bag3.contains(INTEGER.create(6)));
		assertEquals(2, bag3.size());
		
	}
}
