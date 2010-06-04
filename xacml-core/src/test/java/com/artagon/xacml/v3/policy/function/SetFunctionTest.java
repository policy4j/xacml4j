package com.artagon.xacml.v3.policy.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.policy.function.SetFunctions;
import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.artagon.xacml.v3.types.BooleanType.BooleanValue;

public class SetFunctionTest 
{
	@Test
	public void testBooleanUnion()
	{
		BagOfAttributeValues<BooleanValue> a = XacmlDataTypes.BOOLEAN.bag(XacmlDataTypes.BOOLEAN.create(true), XacmlDataTypes.BOOLEAN.create(true));
		BagOfAttributeValues<BooleanValue> b = XacmlDataTypes.BOOLEAN.bag(XacmlDataTypes.BOOLEAN.create(true), XacmlDataTypes.BOOLEAN.create(false));
		BagOfAttributeValues<BooleanValue> c = SetFunctions.booleanUnion(a, b);
		assertEquals(2, c.size());
		assertTrue(c.contains(XacmlDataTypes.BOOLEAN.create(true)));
		assertTrue(c.contains(XacmlDataTypes.BOOLEAN.create(false)));
	}
	
	@Test
	public void testBooleanIntercetion()
	{
		BagOfAttributeValues<BooleanValue> a = XacmlDataTypes.BOOLEAN.bag(XacmlDataTypes.BOOLEAN.create(true), XacmlDataTypes.BOOLEAN.create(true));
		BagOfAttributeValues<BooleanValue> b = XacmlDataTypes.BOOLEAN.bag(XacmlDataTypes.BOOLEAN.create(false), XacmlDataTypes.BOOLEAN.create(false));
		BagOfAttributeValues<BooleanValue> c = SetFunctions.booleanIntersection(a, b);
		assertEquals(0, c.size());
		
		b = XacmlDataTypes.BOOLEAN.bag(XacmlDataTypes.BOOLEAN.create(true), XacmlDataTypes.BOOLEAN.create(false));
		c = SetFunctions.booleanIntersection(a, b);
		assertEquals(1, c.size());
		assertTrue(c.contains(XacmlDataTypes.BOOLEAN.create(true)));
	}
}
