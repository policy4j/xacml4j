package com.artagon.xacml.policy;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.policy.type.IntegerType;
import com.artagon.xacml.policy.type.StringType;
import com.artagon.xacml.policy.type.XacmlDataType;

public class BagOfAttributesTest extends XacmlPolicyTestCase
{
	private StringType stringType;
	private IntegerType integerType;
	
	@Before
	public void init(){
		this.stringType = XacmlDataType.STRING.getType();
		this.integerType = XacmlDataType.INTEGER.getType();
	}
	
	@Test
	public void testContains() throws Exception
	{
		BagOfAttributesType<StringType.StringValue> bagType = stringType.bagOf();
		Collection<Attribute> content = new LinkedList<Attribute>();
		content.add(stringType.create("1"));
		content.add(stringType.create("2"));
		content.add(stringType.create("3"));	
		BagOfAttributes<StringType.StringValue> bag = bagType.createFromAttributes(content);
		assertTrue(bag.contains(stringType.create("1")));
		assertTrue(bag.contains(stringType.create("2")));
		assertTrue(bag.contains(stringType.create("3")));
		assertFalse(bag.contains(stringType.create("4")));
	}
	
	@Test
	public void testEqualsEmptyBags()
	{
		BagOfAttributesType<StringType.StringValue> bagType1 = stringType.bagOf();
		BagOfAttributesType<StringType.StringValue> bagType2 = stringType.bagOf();
		assertEquals(bagType1, bagType2);
	}
	
	@Test
	public void testContainsAll() throws Exception
	{
		
		BagOfAttributesType<?> bagType = integerType.bagOf();
		Collection<Attribute> content = new LinkedList<Attribute>();
		content.add(integerType.create(1l));
		content.add(integerType.create(2l));
		content.add(integerType.create(1l));	
		BagOfAttributes<?> bag = bagType.createFromAttributes(content);
		Collection<Attribute> test = new LinkedList<Attribute>();
		test.add(integerType.create(1l));
		test.add(integerType.create(2l));
		assertTrue(bag.containsAll(bagType.createFromAttributes(test)));		
		test = new LinkedList<Attribute>();
		test.add(integerType.create(1l));
		test.add(integerType.create(3l));
		assertFalse(bag.containsAll(bagType.createFromAttributes(test)));
	}
	
	@Test
	public void testEquals()
	{
		BagOfAttributesType<?> bagType = integerType.bagOf();
		Collection<Attribute> content1 = new LinkedList<Attribute>();
		content1.add(integerType.create(1l));
		content1.add(integerType.create(2l));
		content1.add(integerType.create(3l));
		BagOfAttributes<?> bag1 = bagType.createFromAttributes(content1);
		BagOfAttributes<?> bag2 = bagType.createFromAttributes(content1);
		assertEquals(bag1, bag2);
	}
	
	@Test
	public void testJoin() throws Exception
	{
		BagOfAttributesType<?> bagType = integerType.bagOf();
		Collection<Attribute> content1 = new LinkedList<Attribute>();
		content1.add(integerType.create(1l));
		content1.add(integerType.create(2l));
		content1.add(integerType.create(3l));
		BagOfAttributes<?> bag1 = bagType.createFromAttributes(content1);
		Collection<Attribute> content2 = new LinkedList<Attribute>();
		content2.add(integerType.create(3l));
		content2.add(integerType.create(4l));
		content2.add(integerType.create(5l));
		BagOfAttributes<?> bag2 = bagType.createFromAttributes(content2);
		BagOfAttributes<?> bag3 = bag1.join(bag2);
		BagOfAttributes<?> bag4 = bag2.join(bag1);
		assertEquals(bag3, bag4);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testCreateWithDifferentAttributeTypes()
	{
		BagOfAttributesType<?> bagType = integerType.bagOf();
		Collection<Attribute> attr = new LinkedList<Attribute>();
		attr.add(integerType.create(1l));
		attr.add(stringType.create("aaa"));
		bagType.createFromAttributes(attr);
	}
	
	@Test
	public void testEvaluateBag() throws PolicyEvaluationException
	{
		BagOfAttributesType<?> bagType = integerType.bagOf();
		Collection<Attribute> content2 = new LinkedList<Attribute>();
		content2.add(integerType.create(3l));
		content2.add(integerType.create(4l));
		content2.add(integerType.create(5l));
		BagOfAttributes<?> bag2 = bagType.createFromAttributes(content2);
		assertSame(bag2, bag2.evaluate(context));	
	}
	
}
