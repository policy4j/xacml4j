package com.artagon.xacml.policy.type;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import com.artagon.xacml.policy.Attribute;

public class AnyURITypeTest 
{
	private AnyURIType t1;
	private AnyURIType t2;
	
	@Before
	public void init(){
		this.t1 = new AnyURITypeImpl();
		this.t2 = new AnyURITypeImpl();
	}
	
	@Test
	public void testEquals(){
		Attribute v0 = t1.create("http://www.test.org");
		Attribute v1 = t2.create("http://www.test.org");
		assertEquals(v0, v1);
		assertEquals(t1, t2);
	}
}
