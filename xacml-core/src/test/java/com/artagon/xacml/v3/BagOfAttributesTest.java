package com.artagon.xacml.v3;

import static com.artagon.xacml.v3.types.XacmlDataTypes.INTEGER;
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

import com.artagon.xacml.v3.types.IntegerType.IntegerValue;
import com.artagon.xacml.v3.types.StringType;
import com.artagon.xacml.v3.types.XacmlDataTypes;

public class BagOfAttributesTest
{
	private StringType stringType;
	private EvaluationContext context;
	
	@Before
	public void init(){
		this.stringType = XacmlDataTypes.STRING.getType();
		this.context = createStrictMock(EvaluationContext.class);
	}
	
	@Test
	public void testContains() throws Exception
	{
		BagOfAttributeValuesType<StringType.StringValue> bagType = stringType.bagOf();
		Collection<AttributeValue> content = new LinkedList<AttributeValue>();
		content.add(stringType.create("1"));
		content.add(stringType.create("2"));
		content.add(stringType.create("3"));	
		BagOfAttributeValues<StringType.StringValue> bag = bagType.create(content);
		assertTrue(bag.contains(stringType.create("1")));
		assertTrue(bag.contains(stringType.create("2")));
		assertTrue(bag.contains(stringType.create("3")));
		assertFalse(bag.contains(stringType.create("4")));
	}
	
	@Test
	public void testEqualsEmptyBags()
	{
		assertEquals(XacmlDataTypes.STRING.emptyBag(), XacmlDataTypes.STRING.emptyBag());
		assertEquals(XacmlDataTypes.STRING.emptyBag(), XacmlDataTypes.STRING.emptyBag());
	}
	
	@Test
	public void testContainsAll() throws Exception
	{
		Collection<AttributeValue> content = new LinkedList<AttributeValue>();
		content.add(INTEGER.create(1));
		content.add(INTEGER.create(2));
		content.add(INTEGER.create(1));	
		BagOfAttributeValues<?> bag = INTEGER.bag(content);
		Collection<AttributeValue> test = new LinkedList<AttributeValue>();
		test.add(INTEGER.create(1));
		test.add(INTEGER.create(2));
		assertTrue(bag.containsAll(INTEGER.bag(test)));		
		test = new LinkedList<AttributeValue>();
		test.add(INTEGER.create(1));
		test.add(INTEGER.create(3));
		assertFalse(bag.containsAll(INTEGER.bag(test)));
	}
	
	@Test
	public void testEqualsWithElementsInTheSameOrder()
	{
		Collection<AttributeValue> content1 = new LinkedList<AttributeValue>();
		content1.add(INTEGER.create(1));
		content1.add(INTEGER.create(2));
		content1.add(INTEGER.create(3));
		BagOfAttributeValues<?> bag1 = INTEGER.bag(content1);
		
		Collection<AttributeValue> content2 = new LinkedList<AttributeValue>();
		content2.add(INTEGER.create(1));
		content2.add(INTEGER.create(2));
		content2.add(INTEGER.create(3));
		BagOfAttributeValues<?> bag2 = INTEGER.bag(content2);
		
		assertEquals(bag1, bag2);
		
		Collection<AttributeValue> content3 = new LinkedList<AttributeValue>();
		content3.add(INTEGER.create(1));
		content3.add(INTEGER.create(3));
		content3.add(INTEGER.create(2));
		BagOfAttributeValues<?> bag3= INTEGER.bag(content3);
		
		assertTrue(bag1.equals(bag3));
		assertTrue(bag2.equals(bag3));
	}
	
	
	
	@Test(expected=IllegalArgumentException.class)
	public void testCreateWithDifferentAttributeTypes()
	{
		Collection<AttributeValue> attr = new LinkedList<AttributeValue>();
		attr.add(INTEGER.create(1));
		attr.add(stringType.create("aaa"));
		INTEGER.bag(attr);
	}
	
	@Test
	public void testEvaluateBag() throws EvaluationException
	{
		Collection<AttributeValue> content2 = new LinkedList<AttributeValue>();
		content2.add(INTEGER.create(3));
		content2.add(INTEGER.create(4));
		content2.add(INTEGER.create(5));
		BagOfAttributeValues<?> bag2 = INTEGER.bag(content2);
		replay(context);
		assertSame(bag2, bag2.evaluate(context));	
		verify(context);
	}
	
	@Test
	public void testUnion()
	{
		BagOfAttributeValues<IntegerValue> bag0 = INTEGER.bag(
				INTEGER.create(1),
				INTEGER.create(2),
				INTEGER.create(3),
				INTEGER.create(6));
		
		BagOfAttributeValues<IntegerValue> bag1 = INTEGER.bag(
				INTEGER.create(2),
				INTEGER.create(2),
				INTEGER.create(7),
				INTEGER.create(6));
		
		BagOfAttributeValues<IntegerValue> bag3 = bag0.union(bag1);
		
		assertTrue(bag3.contains(INTEGER.create(2)));
		assertTrue(bag3.contains(INTEGER.create(7)));
		assertTrue(bag3.contains(INTEGER.create(6)));
		assertTrue(bag3.contains(INTEGER.create(1)));
		assertTrue(bag3.contains(INTEGER.create(3)));
		assertEquals(5, bag3.size());
		
	}
	
	@Test
	public void testIntersection()
	{
		BagOfAttributeValues<IntegerValue> bag0 = INTEGER.bag(
				XacmlDataTypes.INTEGER.create(1),
				XacmlDataTypes.INTEGER.create(2),
				XacmlDataTypes.INTEGER.create(3),
				XacmlDataTypes.INTEGER.create(6));
		
		BagOfAttributeValues<IntegerValue> bag1 = INTEGER.bag(
				INTEGER.create(2),
				INTEGER.create(2),
				INTEGER.create(7),
				INTEGER.create(6));
		
		BagOfAttributeValues<IntegerValue> bag3 = bag0.intersection(bag1);
		System.out.println(bag3);
		assertTrue(bag3.contains(INTEGER.create(2)));
		assertTrue(bag3.contains(INTEGER.create(6)));
		assertEquals(2, bag3.size());
		
	}
}
