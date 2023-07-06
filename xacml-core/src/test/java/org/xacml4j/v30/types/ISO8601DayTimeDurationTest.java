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
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

public class ISO8601DayTimeDurationTest
{
	private DatatypeFactory f;

	@Before
	public void init() throws Exception{
		this.f = DatatypeFactory.newInstance();
	}

	@Test
	public void testCreateValidDurationFromLexicalRepresentation()
	{
		ISO8601DayTimeDuration v1 = ISO8601DayTimeDuration.of("P3DT10H30M");
		assertEquals(3, v1.getDays());
		assertEquals(10, v1.getHours());
		assertEquals(30, v1.getMinutes());
		assertEquals(0, v1.getSeconds());
		assertEquals("P3DT10H30M", v1.toXacmlString());
		ISO8601DayTimeDuration v2 = ISO8601DayTimeDuration.of("PT10H10S");
		assertEquals(0, v2.getDays());
		assertEquals(10, v2.getHours());
		assertEquals(0, v2.getMinutes());
		assertEquals(10, v2.getSeconds());
		assertEquals("PT10H10S", v2.toXacmlString());
	}

	@Test
	public void testCreateValidDurationViaExplicitCtor()
	{
		ISO8601DayTimeDuration v1 = ISO8601DayTimeDuration.of(3, 10, 30, 0);
		assertEquals(3, v1.getDays());
		assertEquals(10, v1.getHours());
		assertEquals(30, v1.getMinutes());
		assertEquals(0, v1.getSeconds());
		assertTrue(v1.isPositive());
		ISO8601DayTimeDuration v2 = ISO8601DayTimeDuration.of("P3DT10H30M");
		assertEquals(v1, v2);
	}

	@Test
	public void testAddDurations()
	{
		ISO8601DayTimeDuration v1 = ISO8601DayTimeDuration.of("P3DT10H30M");
		ISO8601DayTimeDuration v2 = ISO8601DayTimeDuration.of("PT10H10S");
		ISO8601DayTimeDuration v3 = v1.plus(v2);
		assertEquals(3, v3.getDays());
		assertEquals(20, v3.getHours());
		assertEquals(30, v3.getMinutes());
		assertEquals(10, v3.getSeconds());
		assertEquals("P3DT10H30M", v1.toXacmlString());
	}

	@Test
	public void testSubstractDurations()
	{
		ISO8601DayTimeDuration v1 = ISO8601DayTimeDuration.of("P3DT10H30M");
		ISO8601DayTimeDuration v2 = ISO8601DayTimeDuration.of("PT10H10S");
		ISO8601DayTimeDuration v3 = v1.minus(v2);
		assertEquals(3, v3.getDays());
		assertEquals(0, v3.getHours());
		assertEquals(29, v3.getMinutes());
		assertEquals(50, v3.getSeconds());
	}

	@Test
	public void testNegate()
	{
		ISO8601DayTimeDuration v1 = ISO8601DayTimeDuration.of("P3DT10H30M");
		ISO8601DayTimeDuration v2 = v1.negated();
		ISO8601DayTimeDuration v3 = v1.plus(v2);
		assertTrue(v3.isZero());
		assertEquals(0, v3.getDays());
		assertEquals(0, v3.getHours());
		assertEquals(0, v3.getMinutes());
		assertEquals(0, v3.getSeconds());
	}

	@Test
	public void testFromXacmlString()
	{
		ISO8601DayTimeDuration v1 = XacmlTypes.DAYTIMEDURATION.ofAny("P3DT10H30M");
		assertEquals(3, v1.getDays());
		assertEquals(10, v1.getHours());
		assertEquals(30, v1.getMinutes());
		assertEquals(0, v1.getSeconds());
		TypeToString toString = TypeToString.forType(XacmlTypes.DAYTIMEDURATION).get();
		assertEquals("P3DT10H30M", toString.toString(v1));
		v1 = (ISO8601DayTimeDuration)toString.fromString("P3DT10H30M10S");
		assertEquals(3, v1.getDays());
		assertEquals(10, v1.getHours());
		assertEquals(30, v1.getMinutes());
		assertEquals(10, v1.get().getSeconds());
	}

	@Test
	public void testToXacmlString()
	{
		Duration d = f.newDurationDayTime(true, 1, 2, 30, 10);
		ISO8601DayTimeDuration v = XacmlTypes.DAYTIMEDURATION.ofAny(d);
		TypeToString toString = TypeToString.forType(XacmlTypes.DAYTIMEDURATION).get();
		assertEquals("P1DT2H30M10S", toString.toString(v));
	}

	@Test
	public void testCompareTo()
	{
		Duration a = f.newDurationDayTime(true, 1, 2, 30, 10);
		Duration b = f.newDurationDayTime(true, 1, 2, 30, 11);
		Duration c = f.newDurationDayTime(true, 1, 2, 30, 9);

		ISO8601DayTimeDuration ad = XacmlTypes.DAYTIMEDURATION.ofAny(a);
		ISO8601DayTimeDuration bd = XacmlTypes.DAYTIMEDURATION.ofAny(b);
		ISO8601DayTimeDuration cd = XacmlTypes.DAYTIMEDURATION.ofAny(c);
		ISO8601DayTimeDuration dd = XacmlTypes.DAYTIMEDURATION.ofAny(a);
		assertTrue(ad.compareTo(bd) < 0);
		assertTrue(bd.compareTo(cd) > 0);
		assertTrue(ad.compareTo(dd) == 0);
	}

	@Test
	public void testEquals()
	{
		ISO8601DayTimeDuration v1 = XacmlTypes.DAYTIMEDURATION.ofAny("P1DT2H30M10S");
		ISO8601DayTimeDuration v2 = XacmlTypes.DAYTIMEDURATION.ofAny("P1DT2H30M10S");
		assertEquals(v1, v2);
	}
}
