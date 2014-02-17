package org.xacml4j.v30.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.util.IPAddressUtils;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.PortRange;
import static org.xacml4j.v30.types.IPAddressType.IPADDRESS;


public class IPAddressTypeTest
{
	private IPAddressType t;

	@Before
	public void test(){
		this.t = IPAddressType.IPADDRESS;
	}

	@Test
	public void testToXacmlStringIPV4()
	{
		AttributeExp a0 = t.create(IPAddressUtils.parseAddress("127.0.0.1"));
		assertEquals("127.0.0.1", IPADDRESS.toString(a0));
		AttributeExp a1 = t.create(IPAddressUtils.parseAddress("127.0.0.1"),
				IPAddressUtils.parseAddress("255.255.255.0"));
		assertEquals("127.0.0.1/255.255.255.0", IPADDRESS.toString(a1));
		AttributeExp a2 = t.create(IPAddressUtils.parseAddress("127.0.0.1"),
				IPAddressUtils.parseAddress("255.255.255.0"), PortRange.getRange(1024, 2048));
		assertEquals("127.0.0.1/255.255.255.0:1024-2048", IPADDRESS.toString(a2));
	}

	@Test
	public void testToXacmlStringIPV6()
	{
		AttributeExp a0 = t.create(IPAddressUtils.parseAddress("2001:0db8:85a3:0000:0000:8a2e:0370:7334"));
		assertEquals("[2001:db8:85a3:0:0:8a2e:370:7334]", IPADDRESS.toString(a0));
		AttributeExp a1 = t.create(IPAddressUtils.parseAddress("2001:0db8:85a3:0000:0000:8a2e:0370:7334"),
				IPAddressUtils.parseAddress("::0"));
		assertEquals("[2001:db8:85a3:0:0:8a2e:370:7334]/[0:0:0:0:0:0:0:0]", IPADDRESS.toString(a1));
	}

	@Test
	public void testParseIPV4()
	{
		IPAddressExp v = IPADDRESS.fromString("127.0.0.1/127.0.0.1:80");
		assertNotNull(v);
		assertEquals(IPAddressUtils.parseAddress("127.0.0.1"), v.getAddress());
		assertEquals(IPAddressUtils.parseAddress("127.0.0.1"), v.getMask());
		assertEquals(PortRange.getSinglePort(80), v.getRange());
	}
}
