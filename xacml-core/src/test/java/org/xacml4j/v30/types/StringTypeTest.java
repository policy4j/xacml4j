package org.xacml4j.v30.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.oasis.xacml.v30.jaxb.AttributeValueType;
import org.xacml4j.v30.AttributeExp;


public class StringTypeTest
{
	private StringType t;
	private Types types;
	
	@Before
	public void init(){
		this.t = StringType.STRING;
		this.types = Types.builder().defaultTypes().create();
	}

	@Test
	public void testEquals()
	{
		AttributeExp v0 = t.create("v0");
		AttributeExp v1 = t.create("v1");
		assertFalse(v0.equals(v1));
		AttributeExp v2 = t.create("v0");
		assertFalse(v1.equals(v2));
		assertTrue(v0.equals(v2));
	}
	
	@Test
	public void typeToXacml30Capability(){
			
		AttributeExp v0 = new StringExp("Test");
		AttributeValueType xml = StringType.STRING.toXacml30(types, v0);
		assertEquals(StringType.STRING.getDataTypeId(), xml.getDataType());
		assertEquals("Test", xml.getContent().get(0));
		AttributeExp v1 = StringType.STRING.fromXacml30(types, xml);
		assertEquals(v0, v1);
	}
}
