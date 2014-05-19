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
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DayTimeDurationTest
{
	@Test
	public void testCreateValidDurationFromLexicalRepresentation()
	{
		DayTimeDuration v1 = DayTimeDuration.parse("P3DT10H30M");
		assertEquals(3, v1.getDays());
		assertEquals(10, v1.getHours());
		assertEquals(30, v1.getMinutes());
		assertEquals(0, v1.getSeconds());
		assertEquals("P3DT10H30M", v1.toString());
		DayTimeDuration v2 = DayTimeDuration.parse("PT10H10S");
		assertEquals(0, v2.getDays());
		assertEquals(10, v2.getHours());
		assertEquals(0, v2.getMinutes());
		assertEquals(10, v2.getSeconds());
		assertEquals("PT10H10S", v2.toString());
	}

	@Test
	public void testCreateValidDurationViaExplicitCtor()
	{
		DayTimeDuration v1 = new DayTimeDuration(true, 3, 10, 30, 0);
		assertEquals(3, v1.getDays());
		assertEquals(10, v1.getHours());
		assertEquals(30, v1.getMinutes());
		assertEquals(0, v1.getSeconds());
		assertTrue(v1.isPositive());
		DayTimeDuration v2 = DayTimeDuration.parse("P3DT10H30M");
		assertEquals(v1, v2);
	}

	@Test
	public void testAddDurations()
	{
		DayTimeDuration v1 = DayTimeDuration.parse("P3DT10H30M");
		DayTimeDuration v2 = DayTimeDuration.parse("PT10H10S");
		DayTimeDuration v3 = v1.add(v2);
		assertEquals(3, v3.getDays());
		assertEquals(20, v3.getHours());
		assertEquals(30, v3.getMinutes());
		assertEquals(10, v3.getSeconds());
		assertEquals("P3DT10H30M", v1.toString());
	}

	@Test
	public void testSubstractDurations()
	{
		DayTimeDuration v1 = DayTimeDuration.parse("P3DT10H30M");
		DayTimeDuration v2 = DayTimeDuration.parse("PT10H10S");
		DayTimeDuration v3 = v1.substract(v2);
		assertEquals(3, v3.getDays());
		assertEquals(0, v3.getHours());
		assertEquals(29, v3.getMinutes());
		assertEquals(50, v3.getSeconds());
	}

	@Test
	public void testNegate()
	{
		DayTimeDuration v1 = DayTimeDuration.parse("P3DT10H30M");
		DayTimeDuration v2 = v1.negate();
		DayTimeDuration v3 = v1.add(v2);
		assertTrue(v3.isZero());
		assertEquals(0, v3.getDays());
		assertEquals(0, v3.getHours());
		assertEquals(0, v3.getMinutes());
		assertEquals(0, v3.getSeconds());
	}
}
