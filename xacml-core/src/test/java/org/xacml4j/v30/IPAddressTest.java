package org.xacml4j.v30;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.xacml4j.util.IPAddressUtils;


public class IPAddressTest
{
	@Test
	public void testIpv4Address()
	{
		IPAddress a = IPAddress.builder().address("127.0.0.1").build();
		assertEquals(IPAddressUtils.parseAddress("127.0.0.1"),  a.getAddress());
		assertNull(a.getMask());
		assertEquals(PortRange.getAnyPort(), a.getRange());
		assertEquals("127.0.0.1", a.toString());
		assertEquals("127.0.0.1", a.toXacmlString());

		a = IPAddress.builder().address("127.0.0.1").portRange("1024-2048").build();
		assertEquals(IPAddressUtils.parseAddress("127.0.0.1"),  a.getAddress());
		assertNull(a.getMask());
		assertEquals(PortRange.getRange(1024, 2048), a.getRange());
		assertEquals("127.0.0.1:1024-2048", a.toString());
		assertEquals("127.0.0.1:1024-2048", a.toXacmlString());


		a = IPAddress.builder().address("127.0.0.1").mask("255.255.255.0").portRange("1024-2048").build();
		assertEquals(IPAddressUtils.parseAddress("127.0.0.1"),  a.getAddress());
		assertEquals(IPAddressUtils.parseAddress("255.255.255.0"),  a.getMask());
		assertEquals(PortRange.getRange(1024, 2048), a.getRange());
		assertEquals("127.0.0.1/255.255.255.0:1024-2048", a.toString());
		assertEquals("127.0.0.1/255.255.255.0:1024-2048", a.toXacmlString());

	}

	@Test
	public void testIpv6Address()
	{
		IPAddress a = IPAddress.builder().address("2001:0db8:85a3:0000:0000:8a2e:0370:7334").build();
		assertEquals(IPAddressUtils.parseAddress("2001:0db8:85a3:0000:0000:8a2e:0370:7334"),  a.getAddress());
		assertNull(a.getMask());
		assertEquals(PortRange.getAnyPort(), a.getRange());
		assertEquals("[2001:db8:85a3:0:0:8a2e:370:7334]", a.toString());
		assertEquals("[2001:db8:85a3:0:0:8a2e:370:7334]", a.toXacmlString());

		a = IPAddress.builder().address("2001:0db8:85a3:0000:0000:8a2e:0370:7334").portRange("1024-2048").build();
		assertEquals(IPAddressUtils.parseAddress("2001:0db8:85a3:0000:0000:8a2e:0370:7334"),  a.getAddress());
		assertNull(a.getMask());
		assertEquals(PortRange.getRange(1024, 2048), a.getRange());
		assertEquals("[2001:db8:85a3:0:0:8a2e:370:7334]:1024-2048", a.toString());
		assertEquals("[2001:db8:85a3:0:0:8a2e:370:7334]:1024-2048", a.toXacmlString());


		a = IPAddress.builder().address("2001:0db8:85a3:0000:0000:8a2e:0370:7334").mask("0:0:0:0:0:0:0:0").portRange("1024-2048").build();
		assertEquals(IPAddressUtils.parseAddress("2001:0db8:85a3:0000:0000:8a2e:0370:7334"),  a.getAddress());
		assertEquals(IPAddressUtils.parseAddress("0:0:0:0:0:0:0:0"),  a.getMask());
		assertEquals(PortRange.getRange(1024, 2048), a.getRange());
		assertEquals("[2001:db8:85a3:0:0:8a2e:370:7334]/[0:0:0:0:0:0:0:0]:1024-2048", a.toString());
		assertEquals("[2001:db8:85a3:0:0:8a2e:370:7334]/[0:0:0:0:0:0:0:0]:1024-2048", a.toXacmlString());

	}

	@Test
	public void testV6MultipleRepresentationsParse()
	{
		IPAddress a1 = IPAddress.builder().address("2001:db8:0:0:1:0:0:1").build();
		IPAddress a2 = IPAddress.builder().address("2001:db8::1:0:0:1").build();
		IPAddress a3 = IPAddress.builder().address("2001:0db8::1:0:0:1").build();
		IPAddress a4 = IPAddress.builder().address("2001:db8:0:0:1::1").build();
		IPAddress a5 = IPAddress.builder().address("2001:db8:0000:0:1::1").build();
		IPAddress a6 = IPAddress.builder().address("2001:DB8:0:0:1::1").build();
		assertEquals(a1, a2);
		assertEquals(a2, a3);
		assertEquals(a3, a4);
		assertEquals(a4, a5);
		assertEquals(a5, a6);
	}
}
