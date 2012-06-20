package com.artagon.xacml.v30;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;


public class DNSNameTest 
{
	@Test
	public void testCreateFromString()
	{
		
		DNSName v = DNSName.parse("test.org:10-20");
		assertEquals("test.org", v.getDomainName());
		assertEquals(10, v.getPortRange().getLowerBound());
		assertEquals(20, v.getPortRange().getUpperBound());
		
		v = DNSName.parse("test.org:-20");
		assertEquals("test.org", v.getDomainName());
		assertFalse(v.getPortRange().isLowerBounded());
		assertEquals(20, v.getPortRange().getUpperBound());
		
		v = DNSName.parse("test.org");
		assertEquals("test.org", v.getDomainName());
		assertFalse(v.getPortRange().isLowerBounded());
		assertFalse(v.getPortRange().isUpperBounded());
		
		v = new DNSName("test.org", new PortRange(10, 20));
		assertEquals("test.org", v.getDomainName());
		assertEquals(10, v.getPortRange().getLowerBound());
		assertEquals(20, v.getPortRange().getUpperBound());
	}
	
	@Test
	public void testEquals()
	{
		assertEquals(DNSName.parse("test.org:10-20"), DNSName.parse("test.org:10-20"));
		assertEquals(DNSName.parse("test.org"), DNSName.parse("test.org"));
	}
}
