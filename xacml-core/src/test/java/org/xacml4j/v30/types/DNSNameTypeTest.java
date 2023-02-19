package org.xacml4j.v30.types;

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
import static org.junit.Assert.assertFalse;

import org.junit.Test;


public class DNSNameTypeTest
{

	@Test
	public void testFromXacmlString()
	{
		DNSNameValue a = XacmlTypes.DNSNAME.of("test.org:10-20");
		DNSName v = a.value();
		assertEquals("test.org", v.getDomainName());
		assertEquals(10, v.getPortRange().getLowerBound());
		assertEquals(20, v.getPortRange().getUpperBound());

		a = DNSNameValue.of("test.org:-20");
		v = a.value();
		assertEquals("test.org", v.getDomainName());
		assertFalse(v.getPortRange().isLowerBounded());
		assertEquals(20, v.getPortRange().getUpperBound());

		a = DNSNameValue.of("test.org");
		v = a.value();
		assertEquals("test.org", v.getDomainName());
		assertFalse(v.getPortRange().isLowerBounded());
		assertFalse(v.getPortRange().isUpperBounded());
	}

	@Test
	public void testEquals()
	{
		DNSNameValue v1 = XacmlTypes.DNSNAME.of("test.org:10-20");
		DNSNameValue v2 = XacmlTypes.DNSNAME.of("test.org:10-20");
		assertEquals(v1, v2);
	}

	@Test
	public void testToXacmlString()
	{
		DNSNameValue v1 = XacmlTypes.DNSNAME.of("test.org:10-20");
		assertEquals("test.org:10-20", v1.toStringExp().value());
		DNSNameValue v2 = XacmlTypes.DNSNAME.of("test.org");
		assertEquals("test.org", v2.toStringExp().value());
	}
}
