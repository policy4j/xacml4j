package com.artagon.xacml.v3.policy.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.artagon.xacml.v3.types.DNSNameType;
import com.artagon.xacml.v3.types.XacmlDataTypes;

public class DNSNameTypeTest 
{
	private DNSNameType t1;
	
	@Before
	public void init(){
		this.t1 = XacmlDataTypes.DNSNAME.getType();
	}
	
	@Test
	@Ignore
	public void testFromXacmlString()
	{
		DNSNameType.DNSNameValue v = t1.fromXacmlString("test.org:10-20");
		assertEquals("test.org", v.getValue().getName());
		assertEquals(10, v.getValue().getPortRange().getLowerBound());
		assertEquals(20, v.getValue().getPortRange().getUpperBound());
		
		v = t1.fromXacmlString("test.org:-20");
		assertEquals("test.org", v.getValue().getName());
		assertFalse(v.getValue().getPortRange().isLowerBounded());
		assertEquals(20, v.getValue().getPortRange().getUpperBound());
		
		v = t1.fromXacmlString("test.org");
		assertEquals("test.org", v.getValue().getName());
		assertFalse(v.getValue().getPortRange().isLowerBounded());
		assertFalse(v.getValue().getPortRange().isUpperBounded());
	}
	
	@Test
	public void testEquals()
	{
		DNSNameType.DNSNameValue v1 = t1.fromXacmlString("test.org:10-20");
		DNSNameType.DNSNameValue v2 = t1.fromXacmlString("test.org:10-20");
		assertEquals(v1, v2);
	}
	
	@Test
	public void testToXacmlString()
	{
		DNSNameType.DNSNameValue v1 = t1.fromXacmlString("test.org:10-20");
		assertEquals("test.org:10-20", v1.toXacmlString());
		DNSNameType.DNSNameValue v2 = t1.fromXacmlString("test.org");
		assertEquals("test.org", v2.toXacmlString());
	}
}
