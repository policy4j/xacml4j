package org.xacml4j.v30.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.DNSName;
import org.xacml4j.v30.types.DNSNameExp;
import org.xacml4j.v30.types.DNSNameType;


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
		DNSNameExp a = t1.fromXacmlString("test.org:10-20");
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
		DNSNameExp v1 = t1.fromXacmlString("test.org:10-20");
		DNSNameExp v2 = t1.fromXacmlString("test.org:10-20");
		assertEquals(v1, v2);
	}
	
	@Test
	public void testToXacmlString()
	{
		DNSNameExp v1 = t1.fromXacmlString("test.org:10-20");
		assertEquals("test.org:10-20", v1.toXacmlString());
		DNSNameExp v2 = t1.fromXacmlString("test.org");
		assertEquals("test.org", v2.toXacmlString());
	}
}
