package com.artagon.xacml.v3.policy.type;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import com.artagon.xacml.util.IPAddressUtils;

public class IPAddressTest 
{
	@Test
	public void testCreate()
	{
	   IPAddress addr = new IPAddress(IPAddressUtils.parseAddress("127.0.0.1"));
	   assertEquals("127.0.0.1", addr.toString());
	   addr = new IPAddress(IPAddressUtils.parseAddress("127.0.0.1"), IPAddressUtils.parseAddress("127.0.0.1"));
	   assertEquals("127.0.0.1/127.0.0.1", addr.toString());
	   addr = new IPAddress(IPAddressUtils.parseAddress("127.0.0.1"),  PortRange.getSinglePort(80));
	   assertEquals("127.0.0.1:80", addr.toString());
	   addr = new IPAddress(IPAddressUtils.parseAddress("127.0.0.1"), IPAddressUtils.parseAddress("127.0.0.1"), PortRange.getSinglePort(80));
	   assertEquals("127.0.0.1/127.0.0.1:80", addr.toString());
	}
}
