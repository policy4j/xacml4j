package org.xacml4j.v30.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.BagOfAttributeExp;


public class IntegerTypeTest
{
	@Test
	public void testCreate()
	{
		AttributeExp v0 = IntegerExp.valueOf((short)2);
		AttributeExp v1 = IntegerExp.valueOf((byte)2);
		AttributeExp v2 = IntegerExp.valueOf(2);
		AttributeExp v3 = IntegerExp.valueOf(2l);
		assertEquals(v3, v0);
		assertEquals(v3, v1);
		assertEquals(v3, v2);
	}

	@Test
	public void testEquals()
	{
		IntegerExp v0 = IntegerExp.valueOf(3l);
		IntegerExp v1 = IntegerExp.valueOf(2l);
		IntegerExp v2 = IntegerExp.valueOf(3l);
		assertEquals(v0, v2);
		assertFalse(v1.equals(v2));
	}
	
	
	@Test
	public void testBag()
	{
		IntegerExp v0 = IntegerExp.valueOf(3l);
		BagOfAttributeExp bag = IntegerExp.bag().value(1, 4).attribute(v0).build();
		assertTrue(bag.contains(IntegerExp.valueOf(3l)));
		assertTrue(bag.contains(IntegerExp.valueOf(1)));
		assertTrue(bag.contains(IntegerExp.valueOf(4)));
		assertEquals(IntegerExp.emptyBag(), IntegerExp.emptyBag());
	}
}
