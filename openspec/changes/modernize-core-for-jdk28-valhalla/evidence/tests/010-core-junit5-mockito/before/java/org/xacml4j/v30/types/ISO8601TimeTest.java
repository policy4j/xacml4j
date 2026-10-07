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

import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Test;

public class ISO8601TimeTest
{
	@Test
	public void testParse()
	{
		ISO8601Time t1 = ISO8601Time.ofAny("08:23:47-05:00");
		OffsetTime t = t1.get();
		assertEquals(8, t1.getHour());
		assertEquals(23, t1.getMinute());
		assertEquals(47, t1.getSecond());
		assertEquals(-5 * 60, t.getOffset().getTotalSeconds() / 60);
		ISO8601Time t2 = ISO8601Time.of("08:23:47");
		assertEquals(8, t2.getHour());
		assertEquals(23, t2.getMinute());
		assertEquals(47, t2.getSecond());
		assertEquals(0, t2.getTimeZone().getTotalSeconds());
	}

	@Test
	public void testParseFromZonedTime()
	{
		ZonedDateTime now = ZonedDateTime.now();
		ISO8601Time t1 = ISO8601Time.of(GregorianCalendar.from(now));
		assertEquals(now.getHour(), t1.getHour());
		assertEquals(now.getMinute(), t1.getMinute());
		assertEquals(now.getSecond(), t1.getSecond());

		ISO8601Time t2 = ISO8601Time.of("08:23:47");
		assertEquals(8, t2.getHour());
		assertEquals(23, t2.getMinute());
		assertEquals(47, t2.getSecond());
		assertEquals(0, t2.getTimeZone().getTotalSeconds());
	}

	@Test
	public void testCompare()
	{
		ISO8601Time t1 = XacmlTypes.TIME.ofAny("08:23:47-05:00");
		ISO8601Time t2 = XacmlTypes.TIME.ofAny("08:23:48-05:00");
		assertEquals(-1, t1.compareTo(t2));
		t2 = XacmlTypes.TIME.ofAny("08:23:47-05:00");
		assertEquals(0, t1.compareTo(t2));
		t2 = XacmlTypes.TIME.ofAny("08:23:46-05:00");
		assertEquals(1, t1.compareTo(t2));
	}

	@Test
	public void testCreateFromCalendar()
	{
		Calendar now = Calendar.getInstance();
		ISO8601Time t1 = XacmlTypes.TIME.ofAny(now);
		ISO8601Time t2 = XacmlTypes.TIME.ofAny(now);
		assertEquals(t1, t2);
	}
}
