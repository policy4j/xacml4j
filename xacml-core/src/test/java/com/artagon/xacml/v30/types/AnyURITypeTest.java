package com.artagon.xacml.v30.types;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v30.AttributeExp;

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
		AttributeExp v0 = t1.create("http://www.test.org");
		AttributeExp v1 = t1.create("http://www.test.org");
		assertEquals(v0, v1);
		assertEquals(AnyURIType.ANYURI, new AnyURIExp(URI.create("test")).getType());
	}
	
	@Test
	public void toStringTest(){
		assertEquals("http://www.w3.org/2001/XMLSchema#anyURI", t1.toString());
	}
}
