package com.artagon.xacml.v3.policy;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.policy.AttributeValue;
import com.artagon.xacml.v3.policy.BagOfAttributeValues;
import com.artagon.xacml.v3.policy.BagOfAttributeValuesType;
import com.artagon.xacml.v3.policy.EvaluationException;
import com.artagon.xacml.v3.policy.type.DataTypes;
import com.artagon.xacml.v3.policy.type.IntegerType;
import com.artagon.xacml.v3.policy.type.StringType;

public class BagOfAttributesTest extends XacmlPolicyTestCase
{
	private StringType stringType;
	private IntegerType integerType;
	
	@Before
	public void init(){
		this.stringType = DataTypes.STRING.getType();
		this.integerType = DataTypes.INTEGER.getType();
	}
	
	@Test
	public void testContains() throws Exception
	{
		BagOfAttributeValuesType<StringType.StringValue> bagType = stringType.bagOf();
		Collection<AttributeValue> content = new LinkedList<AttributeValue>();
		content.add(stringType.create("1"));
		content.add(stringType.create("2"));
		content.add(stringType.create("3"));	
		BagOfAttributeValues<StringType.StringValue> bag = bagType.createFromAttributes(content);
		assertTrue(bag.contains(stringType.create("1")));
		assertTrue(bag.contains(stringType.create("2")));
		assertTrue(bag.contains(stringType.create("3")));
		assertFalse(bag.contains(stringType.create("4")));
	}
	
	@Test
	public void testEqualsEmptyBags()
	{
		BagOfAttributeValuesType<StringType.StringValue> bagType1 = stringType.bagOf();
		BagOfAttributeValuesType<StringType.StringValue> bagType2 = stringType.bagOf();
		assertEquals(bagType1, bagType2);
	}
	
	@Test
	public void testContainsAll() throws Exception
	{
		
		BagOfAttributeValuesType<?> bagType = integerType.bagOf();
		Collection<AttributeValue> content = new LinkedList<AttributeValue>();
		content.add(integerType.create(1l));
		content.add(integerType.create(2l));
		content.add(integerType.create(1l));	
		BagOfAttributeValues<?> bag = bagType.createFromAttributes(content);
		Collection<AttributeValue> test = new LinkedList<AttributeValue>();
		test.add(integerType.create(1l));
		test.add(integerType.create(2l));
		assertTrue(bag.containsAll(bagType.createFromAttributes(test)));		
		test = new LinkedList<AttributeValue>();
		test.add(integerType.create(1l));
		test.add(integerType.create(3l));
		assertFalse(bag.containsAll(bagType.createFromAttributes(test)));
	}
	
	@Test
	public void testEquals()
	{
		BagOfAttributeValuesType<?> bagType = integerType.bagOf();
		Collection<AttributeValue> content1 = new LinkedList<AttributeValue>();
		content1.add(integerType.create(1l));
		content1.add(integerType.create(2l));
		content1.add(integerType.create(3l));
		BagOfAttributeValues<?> bag1 = bagType.createFromAttributes(content1);
		BagOfAttributeValues<?> bag2 = bagType.createFromAttributes(content1);
		assertEquals(bag1, bag2);
	}
	
	@Test
	public void testJoin() throws Exception
	{
		BagOfAttributeValuesType<?> bagType = integerType.bagOf();
		Collection<AttributeValue> content1 = new LinkedList<AttributeValue>();
		content1.add(integerType.create(1l));
		content1.add(integerType.create(2l));
		content1.add(integerType.create(3l));
		BagOfAttributeValues<?> bag1 = bagType.createFromAttributes(content1);
		Collection<AttributeValue> content2 = new LinkedList<AttributeValue>();
		content2.add(integerType.create(3l));
		content2.add(integerType.create(4l));
		content2.add(integerType.create(5l));
		BagOfAttributeValues<?> bag2 = bagType.createFromAttributes(content2);
		BagOfAttributeValues<?> bag3 = bag1.join(bag2);
		BagOfAttributeValues<?> bag4 = bag2.join(bag1);
		assertEquals(bag3, bag4);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testCreateWithDifferentAttributeTypes()
	{
		BagOfAttributeValuesType<?> bagType = integerType.bagOf();
		Collection<AttributeValue> attr = new LinkedList<AttributeValue>();
		attr.add(integerType.create(1l));
		attr.add(stringType.create("aaa"));
		bagType.createFromAttributes(attr);
	}
	
	@Test
	public void testEvaluateBag() throws EvaluationException
	{
		BagOfAttributeValuesType<?> bagType = integerType.bagOf();
		Collection<AttributeValue> content2 = new LinkedList<AttributeValue>();
		content2.add(integerType.create(3l));
		content2.add(integerType.create(4l));
		content2.add(integerType.create(5l));
		BagOfAttributeValues<?> bag2 = bagType.createFromAttributes(content2);
		assertSame(bag2, bag2.evaluate(context));	
	}
	
}
