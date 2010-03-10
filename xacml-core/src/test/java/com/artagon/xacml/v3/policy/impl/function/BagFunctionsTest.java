package com.artagon.xacml.v3.policy.impl.function;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.artagon.xacml.v3.policy.BagOfAttributeValues;
import com.artagon.xacml.v3.policy.EvaluationException;
import com.artagon.xacml.v3.policy.type.DataTypes;

import com.artagon.xacml.v3.policy.type.StringType.StringValue;

public class BagFunctionsTest 
{
	@Test
	public void testStringOneAndOnly() throws EvaluationException
	{
		BagOfAttributeValues<StringValue> bag = DataTypes.STRING.bag(DataTypes.STRING.create("a"));
		assertEquals(DataTypes.STRING.create("a"), BagFunctions.oneAndOnly(bag));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testStringOneAndOnlyEmptyBag() throws EvaluationException
	{
		BagOfAttributeValues<StringValue> bag = DataTypes.STRING.emptyBag();
		BagFunctions.oneAndOnly(bag);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testStringOneAndOnlyMoreThanOne() throws EvaluationException
	{
		BagOfAttributeValues<StringValue> bag = DataTypes.STRING.bag(DataTypes.STRING.create("a"), DataTypes.STRING.create("c"));
		BagFunctions.oneAndOnly(bag);
	}
}
