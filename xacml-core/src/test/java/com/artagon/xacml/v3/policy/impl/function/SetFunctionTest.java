package com.artagon.xacml.v3.policy.impl.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.policy.type.DataTypes;
import com.artagon.xacml.v3.policy.type.BooleanType.BooleanValue;

public class SetFunctionTest 
{
	@Test
	public void testBooleanUnion()
	{
		BagOfAttributeValues<BooleanValue> a = DataTypes.BOOLEAN.bag(DataTypes.BOOLEAN.create(true), DataTypes.BOOLEAN.create(true));
		BagOfAttributeValues<BooleanValue> b = DataTypes.BOOLEAN.bag(DataTypes.BOOLEAN.create(true), DataTypes.BOOLEAN.create(false));
		BagOfAttributeValues<BooleanValue> c = SetFunctions.booleanUnion(a, b);
		assertEquals(2, c.size());
		assertTrue(c.contains(DataTypes.BOOLEAN.create(true)));
		assertTrue(c.contains(DataTypes.BOOLEAN.create(false)));
	}
	
	@Test
	public void testBooleanIntercetion()
	{
		BagOfAttributeValues<BooleanValue> a = DataTypes.BOOLEAN.bag(DataTypes.BOOLEAN.create(true), DataTypes.BOOLEAN.create(true));
		BagOfAttributeValues<BooleanValue> b = DataTypes.BOOLEAN.bag(DataTypes.BOOLEAN.create(false), DataTypes.BOOLEAN.create(false));
		BagOfAttributeValues<BooleanValue> c = SetFunctions.booleanIntersection(a, b);
		assertEquals(0, c.size());
		
		b = DataTypes.BOOLEAN.bag(DataTypes.BOOLEAN.create(true), DataTypes.BOOLEAN.create(false));
		c = SetFunctions.booleanIntersection(a, b);
		assertEquals(1, c.size());
		assertTrue(c.contains(DataTypes.BOOLEAN.create(true)));
	}
}
