package com.artagon.xacml.v3.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.marshall.XacmlDataTypesRegistry;

public class AnyURITypeTest 
{
	private AnyURIType t1;
	
	@Before
	public void init(){
		this.t1 = AnyURIType.ANYURI;
	}
	
	@Test
	public void testEquals()
	{
		AttributeValue v0 = t1.create("http://www.test.org");
		AttributeValue v1 = t1.create("http://www.test.org");
		assertEquals(v0, v1);
		assertEquals(XacmlDataTypesRegistry.ANYURI.getDataType(), AnyURIType.ANYURI);
		assertSame(XacmlDataTypesRegistry.ANYURI.getDataType(), t1);
		
		assertEquals(AnyURIType.ANYURI, new AnyURIValue(URI.create("test")).getType());
	}
}
