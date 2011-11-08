package com.artagon.xacml.v30.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v30.core.DNSName;

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
		DNSNameValueExp a = t1.fromXacmlString("test.org:10-20");
		DNSName v = a.getValue();
		assertEquals("test.org", v.getDomainName());
		assertEquals(10, v.getPortRange().getLowerBound());
		assertEquals(20, v.getPortRange().getUpperBound());
		
		a = t1.fromXacmlString("test.org:-20");
		v = a.getValue();
		assertEquals("test.org", v.getDomainName());
		assertFalse(v.getPortRange().isLowerBounded());
		assertEquals(20, v.getPortRange().getUpperBound());
		
		a = t1.fromXacmlString("test.org");
		v = a.getValue();
		assertEquals("test.org", v.getDomainName());
		assertFalse(v.getPortRange().isLowerBounded());
		assertFalse(v.getPortRange().isUpperBounded());
	}
	
	@Test
	public void testEquals()
	{
		DNSNameValueExp v1 = t1.fromXacmlString("test.org:10-20");
		DNSNameValueExp v2 = t1.fromXacmlString("test.org:10-20");
		assertEquals(v1, v2);
	}
	
	@Test
	public void testToXacmlString()
	{
		DNSNameValueExp v1 = t1.fromXacmlString("test.org:10-20");
		assertEquals("test.org:10-20", v1.toXacmlString());
		DNSNameValueExp v2 = t1.fromXacmlString("test.org");
		assertEquals("test.org", v2.toXacmlString());
	}
}
