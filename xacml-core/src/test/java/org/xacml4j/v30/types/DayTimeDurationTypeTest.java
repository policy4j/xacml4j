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

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.junit.Before;
import org.junit.Test;


public class DayTimeDurationTypeTest
{
	private DatatypeFactory f;
	
	@Before
	public void init() throws Exception{
		this.f = DatatypeFactory.newInstance();
	}

	@Test
	public void testFromXacmlString()
	{
		DayTimeDurationExp v1 = DayTimeDurationExp.valueOf("P3DT10H30M");
		assertEquals(3, v1.getValue().getDays());
		assertEquals(10, v1.getValue().getHours());
		assertEquals(30, v1.getValue().getMinutes());
		assertEquals(0, v1.getValue().getSeconds());
		TypeToString toString = TypeToString.Types.getIndex().get(XacmlTypes.DAYTIMEDURATION).get();
		assertEquals("P3DT10H30M", toString.toString(v1));
		v1 = (DayTimeDurationExp)toString.fromString("P3DT10H30M10S");
		assertEquals(3, v1.getValue().getDays());
		assertEquals(10, v1.getValue().getHours());
		assertEquals(30, v1.getValue().getMinutes());
		assertEquals(10, v1.getValue().getSeconds());
	}

	@Test
	public void testToXacmlString()
	{
		Duration d = f.newDurationDayTime(true, 1, 2, 30, 10);
		DayTimeDurationExp v = DayTimeDurationExp.valueOf(d);
		TypeToString toString = TypeToString.Types.getIndex().get(XacmlTypes.DAYTIMEDURATION).get();
		assertEquals("P1DT2H30M10S", toString.toString(v));
	}

	@Test
	public void testCompareTo()
	{
		Duration a = f.newDurationDayTime(true, 1, 2, 30, 10);
		Duration b = f.newDurationDayTime(true, 1, 2, 30, 11);
		Duration c = f.newDurationDayTime(true, 1, 2, 30, 9);

		DayTimeDurationExp ad = DayTimeDurationExp.valueOf(a);
		DayTimeDurationExp bd = DayTimeDurationExp.valueOf(b);
		DayTimeDurationExp cd = DayTimeDurationExp.valueOf(c);
		DayTimeDurationExp dd = DayTimeDurationExp.valueOf(a);
		assertTrue(ad.compareTo(bd) < 0);
		assertTrue(bd.compareTo(cd) > 0);
		assertTrue(ad.compareTo(dd) == 0);
	}

	@Test
	public void testEquals()
	{
		DayTimeDurationExp v1 = DayTimeDurationExp.valueOf("P1DT2H30M10S");
		DayTimeDurationExp v2 = DayTimeDurationExp.valueOf("P1DT2H30M10S");
		assertEquals(v1, v2);
	}
}
