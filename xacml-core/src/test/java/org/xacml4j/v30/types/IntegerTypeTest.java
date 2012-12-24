package org.xacml4j.v30.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.types.IntegerExp;
import org.xacml4j.v30.types.IntegerType;


public class IntegerTypeTest
{
	private IntegerType t;

	@Before
	public void init(){
		this.t = IntegerType.INTEGER;
	}

	@Test
	public void testCreate()
	{
		AttributeExp v0 = t.create((short)2);
		AttributeExp v1 = t.create((byte)2);
		AttributeExp v2 = t.create(2);
		AttributeExp v3 = t.create(2l);
		assertEquals(v3, v0);
		assertEquals(v3, v1);
		assertEquals(v3, v2);
	}

	@Test
	public void testEquals()
	{
		IntegerExp v0 = t.create(3l);
		IntegerExp v1 = t.create(2l);
		IntegerExp v2 = t.create(3l);
		assertEquals(v0, v2);
		assertFalse(v1.equals(v2));
	}

	@Test
	public void multipleConstructors()
	{
		BagOfAttributeExp bag1 = t.bagOf(1, 2, 3, 4);
		BagOfAttributeExp bag2 = t.bagOf(t.create(1), t.create(2), t.create(3), t.create(4));
	}
}
