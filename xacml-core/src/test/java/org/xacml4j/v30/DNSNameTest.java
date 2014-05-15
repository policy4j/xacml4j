package org.xacml4j.v30;

/*
 * #%L
 * Artagon XACML 3.0 Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Artagon
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
