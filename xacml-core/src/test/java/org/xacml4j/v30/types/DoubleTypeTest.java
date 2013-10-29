package org.xacml4j.v30.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.AttributeExp;


public class DoubleTypeTest
{
	private DoubleType t1;

	@Before
	public void init(){
		this.t1 = DoubleType.DOUBLE;
	}

	@Test
	public void testCreateDouble()
	{
		assertEquals(t1.create(1), t1.create(Short.valueOf("1")));
		assertEquals(t1.create(1), t1.create(Byte.valueOf("1")));
		assertEquals(t1.create(1), t1.create(Integer.valueOf("1")));
		assertEquals(t1.create(1), t1.create(Long.valueOf("1")));
		assertEquals(t1.create(1), t1.create(Float.valueOf("1")));
		assertEquals(t1.create(1), t1.create(Double.valueOf("1")));
	}

	@Test
	public void testToXacmlString()
	{
		AttributeExp v0 = t1.create(1.0d);
		AttributeExp v1 = t1.create(-2.0d);
		assertEquals("1.0", v0.toXacmlString());
		assertEquals("-2.0", v1.toXacmlString());
	}

	@Test
	public void testFromAnyObject()
	{
		Object o = 0.2d;
		DoubleExp a = t1.create(o);
		assertEquals(o, a.getValue());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFromAnyObjectWrongContentType()
	{
		Object o = new byte[10];
		t1.create(o);
	}


	@Test
	public void testEquals()
	{
		AttributeExp v0 = t1.create(1.0d);
		AttributeExp v1 = t1.create(2.0d);
		AttributeExp v2 = t1.create(1.0d);
		assertFalse(v0.equals(v1));
		assertTrue(v0.equals(v2));
		AttributeExp v0Nan = t1.create(Double.NaN);
		AttributeExp v1Nan = t1.create(Double.NaN);
		assertFalse(v0.equals(v0Nan));
		assertTrue(v0Nan.equals(v0Nan));
		assertFalse(v1Nan.equals(v0));
	}
}
