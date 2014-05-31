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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PortRangeTest
{
	@Test
	public void testCreateIPPortRange()
	{
		PortRange range = PortRange.getAnyPort();
		range.isLowerBounded();
		assertFalse(range.isLowerBounded());
		assertFalse(range.isUpperBounded());
		assertTrue(range.isUnbound());
	}

	@Test
	public void testContains()
	{
		PortRange range = PortRange.getSinglePort(1024);
		assertTrue(range.contains(1024));
		assertFalse(range.contains(10));
		range = PortRange.getRange(1024, 1200);
		assertTrue(range.contains(1024));
		assertTrue(range.contains(1025));
		assertTrue(range.contains(1200));
		assertFalse(range.contains(1201));
		range = PortRange.getRangeFrom(1024);
		assertTrue(range.contains(1024));
		assertTrue(range.contains(6000));
		assertFalse(range.contains(1023));

		range = PortRange.getRangeUntil(1024);
		assertTrue(range.contains(1023));
		assertTrue(range.contains(600));
		assertFalse(range.contains(1025));
	}

	@Test
	public void testToString()
	{
		PortRange range = PortRange.getSinglePort(1024);
		assertEquals("1024", range.toString());
		range = PortRange.getRange(1024, 1200);
		assertEquals("1024-1200", range.toString());
		range = PortRange.getRangeFrom(1024);
		assertEquals("1024-", range.toString());
		range = PortRange.getRangeUntil(1024);
		assertEquals("-1024", range.toString());
	}

	@Test
	public void testValueOf()
	{
		PortRange r = PortRange.valueOf("1024");
		assertEquals(1024, r.getLowerBound());
		assertEquals(1024, r.getUpperBound());

		r = PortRange.valueOf("1024-");
		assertEquals(1024, r.getLowerBound());
		assertFalse(r.isUpperBounded());

		r = PortRange.valueOf("-1024");
		assertEquals(1024, r.getUpperBound());
		assertFalse(r.isLowerBounded());
	}

	@Test
	public void testValueOfWithIndex()
	{
		PortRange r = PortRange.valueOf(4, "aaa:1024");
		assertEquals(1024, r.getLowerBound());
		assertEquals(1024, r.getUpperBound());

		r = PortRange.valueOf(4, "aaa:1024-");
		assertEquals(1024, r.getLowerBound());
		assertFalse(r.isUpperBounded());

		r = PortRange.valueOf(4, "aaa:-1024");
		assertEquals(1024, r.getUpperBound());
		assertFalse(r.isLowerBounded());
	}

	@Test
	public void testContainsRange()
	{
		PortRange r = PortRange.getRange(1024, 1200);
		assertTrue(r.contains(PortRange.getSinglePort(1024)));
		assertTrue(r.contains(PortRange.getSinglePort(1200)));
		assertTrue(r.contains(PortRange.getSinglePort(1100)));
		assertFalse(r.contains(PortRange.getSinglePort(1023)));
		assertFalse(r.contains(PortRange.getSinglePort(1201)));
	}

	@Test
	public void testEquals()
	{
		PortRange r0 = PortRange.getAnyPort();
		PortRange r1 = PortRange.getSinglePort(80);
		PortRange r2 = PortRange.getRange(80, 80);
		assertFalse(r0.equals(r1));
		assertTrue(r1.equals(r2));

		PortRange r3 = PortRange.getRange(80, 100);
		PortRange r4 = PortRange.getRange(80, 100);
		assertTrue(r3.equals(r4));
	}

}
