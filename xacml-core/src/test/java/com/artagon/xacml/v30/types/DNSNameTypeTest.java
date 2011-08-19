package com.artagon.xacml.v30.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

public class DNSNameTypeTest 
{
	private DNSNameType t1;
	
	@Before
	public void init(){
		this.t1 = DNSNameType.DNSNAME;
	}
	
	@Test
	public void testFromXacmlString()
	{
		DNSNameValue a = t1.fromXacmlString("test.org:10-20");
		DNSName v = a.getValue();
		assertEquals("test.org", v.getName());
		assertEquals(10, v.getPortRange().getLowerBound());
		assertEquals(20, v.getPortRange().getUpperBound());
		
		a = t1.fromXacmlString("test.org:-20");
		v = a.getValue();
		assertEquals("test.org", v.getName());
		assertFalse(v.getPortRange().isLowerBounded());
		assertEquals(20, v.getPortRange().getUpperBound());
		
		a = t1.fromXacmlString("test.org");
		v = a.getValue();
		assertEquals("test.org", v.getName());
		assertFalse(v.getPortRange().isLowerBounded());
		assertFalse(v.getPortRange().isUpperBounded());
	}
	
	@Test
	public void testEquals()
	{
		DNSNameValue v1 = t1.fromXacmlString("test.org:10-20");
		DNSNameValue v2 = t1.fromXacmlString("test.org:10-20");
		assertEquals(v1, v2);
	}
	
	@Test
	public void testToXacmlString()
	{
		DNSNameValue v1 = t1.fromXacmlString("test.org:10-20");
		assertEquals("test.org:10-20", v1.toXacmlString());
		DNSNameValue v2 = t1.fromXacmlString("test.org");
		assertEquals("test.org", v2.toXacmlString());
	}
}
