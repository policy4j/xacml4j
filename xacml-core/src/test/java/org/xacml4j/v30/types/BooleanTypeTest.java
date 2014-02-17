package org.xacml4j.v30.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.BagOfAttributeExp;


public class BooleanTypeTest
{

	private BooleanType t1;
	private Types types;
	
	@Before
	public void init(){
		this.t1 = BooleanType.BOOLEAN;
		this.types = Types.builder().defaultTypes().create();
	}

	@Test
	public void testCreate()
	{
		Object o = Boolean.FALSE;
		BooleanExp a = t1.create(o);
		assertFalse(a.getValue());
		o = "true";
		BooleanExp a1 = t1.create(o);
		assertTrue(a1.getValue());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFromAnyObjectWrongContentType()
	{
		Long a = 1l;
		assertFalse(t1.create(a).getValue());
	}

	@Test
	public void fromToXacmlString()
	{
		BooleanExp v = t1.create("True");
		assertEquals(Boolean.TRUE, v.getValue());
		v = t1.create("TRUE");
		assertEquals(Boolean.TRUE, v.getValue());
		v = t1.create("FALSE");
		assertEquals(Boolean.FALSE, v.getValue());
		v = t1.create("False");
		assertEquals(Boolean.FALSE, v.getValue());
	}

	@Test
	public void toXacmlString()
	{
		TypeToString c = types.getCapability(BooleanType.BOOLEAN, TypeToString.class);
		BooleanExp v1 = t1.create(Boolean.TRUE);
		BooleanExp v2 = t1.create(Boolean.FALSE);
		assertEquals("true", c.toString(v1));
		assertEquals("false", c.toString(v2));
	}

	@Test
	public void testEquals()
	{
		BooleanExp v1 = t1.create(Boolean.TRUE);
		BooleanExp v2 = t1.create(Boolean.FALSE);
		BooleanExp v3 = t1.create(Boolean.TRUE);
		BooleanExp v4 = t1.create(Boolean.FALSE);
		assertEquals(v1, v3);
		assertEquals(v2, v4);
	}

	@Test
	public void testBagOf()
	{
		BagOfAttributeExp b1 = t1.bagOf(BooleanType.BOOLEAN.create("true"), BooleanType.BOOLEAN.create("false"));
		BagOfAttributeExp b2 = t1.bagOf(BooleanType.BOOLEAN.create(true), BooleanType.BOOLEAN.create(false));
		assertEquals(2, b1.size());
		assertEquals(b1, b2);
		assertTrue(b1.contains(t1.create(true)));
		assertTrue(b1.contains(t1.create(false)));

	}
}
