package org.xacml4j.v30.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.xacml4j.util.IPAddressUtils;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.IPAddress;
import org.xacml4j.v30.PortRange;


public class IPAddressTypeTest
{
	
	@Test
	public void testToXacmlStringIPV4()
	{
		AttributeExp a0 = IPAddressExp.valueOf("127.0.0.1");
		assertEquals("127.0.0.1", TypeToString.Types.IPADDRESS.toString(a0));
		AttributeExp a1 = IPAddressExp.valueOf(
				IPAddress
				.builder()
				.address("127.0.0.1")
				.mask("255.255.255.0")
				.build());
		assertEquals("127.0.0.1/255.255.255.0", TypeToString.Types.IPADDRESS.toString(a1));
		AttributeExp a2 = IPAddressExp.valueOf(
				IPAddress.builder()
				.address("127.0.0.1")
				.mask("255.255.255.0")
				.portRange("1024-2048")
				.build());
		AttributeExp a3 = IPAddressExp.valueOf("127.0.0.1/255.255.255.0:1024-2048");
		assertEquals("127.0.0.1/255.255.255.0:1024-2048", TypeToString.Types.IPADDRESS.toString(a2));
		assertEquals(a2, a3);
	}

	@Test
	public void testToXacmlStringIPV6()
	{
		AttributeExp a0 = IPAddressExp.valueOf("[2001:0db8:85a3:0000:0000:8a2e:0370:7334]");
		assertEquals("[2001:db8:85a3:0:0:8a2e:370:7334]", TypeToString.Types.IPADDRESS.toString(a0));
		AttributeExp a1 = IPAddressExp.valueOf(IPAddress
				.builder()
				.address("2001:0db8:85a3:0000:0000:8a2e:0370:7334")
				.mask("::0")
				.build());
		assertEquals("[2001:db8:85a3:0:0:8a2e:370:7334]/[0:0:0:0:0:0:0:0]", TypeToString.Types.IPADDRESS.toString(a1));
	}

	@Test
	public void testParseIPV4()
	{
		IPAddressExp v = (IPAddressExp)TypeToString.Types.IPADDRESS.fromString("127.0.0.1/127.0.0.1:80");
		assertNotNull(v);
		assertEquals(IPAddressUtils.parseAddress("127.0.0.1"), v.getAddress());
		assertEquals(IPAddressUtils.parseAddress("127.0.0.1"), v.getMask());
		assertEquals(PortRange.getSinglePort(80), v.getRange());
	}
}
