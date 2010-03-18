package com.artagon.xacml.v3.policy.type;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.util.IPAddressUtils;
import com.artagon.xacml.v3.AttributeValue;

public class IPAddressTypeTest 
{
	private IPAddressType t;
	
	@Before
	public void test(){
		this.t = DataTypes.IPADDRESS.getType();
	}
	
	@Test
	public void testToXacmlStringIPV4()
	{
		AttributeValue a0 = t.create(IPAddressUtils.parseAddress("127.0.0.1"));
		assertEquals("127.0.0.1", a0.toXacmlString());
		AttributeValue a1 = t.create(IPAddressUtils.parseAddress("127.0.0.1"), 
				IPAddressUtils.parseAddress("255.255.255.0"));
		assertEquals("127.0.0.1/255.255.255.0", a1.toXacmlString());
		AttributeValue a2 = t.create(IPAddressUtils.parseAddress("127.0.0.1"), 
				IPAddressUtils.parseAddress("255.255.255.0"), PortRange.getRange(1024, 2048));
		assertEquals("127.0.0.1/255.255.255.0:1024-2048", a2.toXacmlString());
	}
	
	@Test
	public void testToXacmlStringIPV6()
	{
		AttributeValue a0 = t.create(IPAddressUtils.parseAddress("2001:0db8:85a3:0000:0000:8a2e:0370:7334"));
		assertEquals("[2001:db8:85a3:0:0:8a2e:370:7334]", a0.toString());
		AttributeValue a1 = t.create(IPAddressUtils.parseAddress("2001:0db8:85a3:0000:0000:8a2e:0370:7334"), 
				IPAddressUtils.parseAddress("255.255.255.0"));
		assertEquals("127.0.0.1/255.255.255.0", a1.toString());
	}
}
