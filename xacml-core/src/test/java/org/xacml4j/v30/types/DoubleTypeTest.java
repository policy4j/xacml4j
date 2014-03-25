package org.xacml4j.v30.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.xacml4j.v30.AttributeExp;


public class DoubleTypeTest
{
	@Test
	public void testToXacmlString()
	{
		assertEquals("1.0", DoubleExp.valueOf(1.0d).toStringExp().getValue());
		assertEquals("-2.0", DoubleExp.valueOf(-2.0d).toStringExp().getValue());
	}


	@Test
	public void testEquals()
	{
		AttributeExp v0 = DoubleExp.valueOf(1.0d);
		AttributeExp v1 = DoubleExp.valueOf(2.0d);
		AttributeExp v2 = DoubleExp.valueOf(1.0d);
		assertFalse(v0.equals(v1));
		assertTrue(v0.equals(v2));
		DoubleExp v0Nan = DoubleExp.valueOf(Double.NaN);
		DoubleExp v1Nan = DoubleExp.valueOf(Double.NaN);
		assertEquals("NaN", v0Nan.toStringExp().getValue());
		assertFalse(v0.equals(v0Nan));
		assertTrue(v0Nan.equals(v0Nan));
		assertFalse(v1Nan.equals(v0));
	}
}
