package org.xacml4j.v30.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.xacml4j.v30.BagOfAttributeExp;

import com.google.common.base.Optional;


public class BooleanTypeTest
{
	@Test
	public void testValueOf()
	{
		BooleanExp a = BooleanExp.valueOf("false");
		assertFalse(a.getValue());
		BooleanExp a1 = BooleanExp.valueOf("true");
		assertTrue(a1.getValue());
	}

	@Test
	public void fromToXacmlString()
	{
		BooleanExp v = BooleanExp.valueOf("True");
		assertEquals(Boolean.TRUE, v.getValue());
		v = BooleanExp.valueOf("TRUE");
		assertEquals(Boolean.TRUE, v.getValue());
		v = BooleanExp.valueOf("FALSE");
		assertEquals(Boolean.FALSE, v.getValue());
		v = BooleanExp.valueOf("False");
		assertEquals(Boolean.FALSE, v.getValue());
	}

	@Test
	public void toXacmlString()
	{
		Optional<TypeToString> c = TypeToString.Types.getIndex().get(XacmlTypes.BOOLEAN);
		BooleanExp v1 = BooleanExp.valueOf(Boolean.TRUE);
		BooleanExp v2 = BooleanExp.valueOf(Boolean.FALSE);
		assertEquals("true", c.get().toString(v1));
		assertEquals("false", c.get().toString(v2));
	}

	@Test
	public void testEquals()
	{
		BooleanExp v1 = BooleanExp.valueOf(Boolean.TRUE);
		BooleanExp v2 = BooleanExp.valueOf(Boolean.FALSE);
		BooleanExp v3 = BooleanExp.valueOf(Boolean.TRUE);
		BooleanExp v4 = BooleanExp.valueOf(Boolean.FALSE);
		assertEquals(v1, v3);
		assertEquals(v2, v4);
	}

	@Test
	public void testBagOf()
	{
		BagOfAttributeExp b1 = BooleanExp.bag().value("true", "false").build();
		BagOfAttributeExp b2 = BooleanExp.bag().value(true, false).build();
		assertEquals(2, b1.size());
		assertEquals(b1, b2);
		assertTrue(b1.contains(BooleanExp.valueOf(true)));
		assertTrue(b1.contains(BooleanExp.valueOf(false)));

	}
}
