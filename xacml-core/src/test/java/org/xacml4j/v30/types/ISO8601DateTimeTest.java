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

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.SyntaxException;


public class ISO8601DateTimeTest
{
	private TypeToString toString;

	@Before
	public void init() throws Exception{
		this.toString = TypeToString.Types.DATETIME;
	}

	@Test
	public void testFromXacmlString(){
		ISO8601DateTime value = (ISO8601DateTime)toString.fromString("2002-05-30T09:30:10-06:00");
		assertEquals(2002, value.get().getYear());
		assertEquals(5, value.get().getMonthValue());
		assertEquals(30, value.get().getDayOfMonth());
		assertEquals(9, value.get().getHour());
		assertEquals(30, value.get().getMinute());
		assertEquals(10, value.get().getSecond());
		assertEquals(-360, value.getTimeZoneOffset().getTotalSeconds() / 60);
		assertEquals("2002-05-30T09:30:10-06:00", toString.toString(value));
	}

	@Test
	public void testFromXacmlStringNoTimeZone(){
		ISO8601DateTime value = XacmlTypes.DATETIME.ofAny("2002-05-30T09:30:10");
		assertEquals(2002, value.get().getYear());
		assertEquals(5, value.get().getMonthValue());
		assertEquals(30, value.get().getDayOfMonth());
		assertEquals(9, value.get().getHour());
		assertEquals(30, value.get().getMinute());
		assertEquals(10, value.get().getSecond());
		assertEquals(0, value.get().getOffset().getTotalSeconds() * 60);
		assertEquals("2002-05-30T09:30:10Z", toString.toString(value));

	}

	@Test(expected= SyntaxException.class)
	public void testFromXacmlStringJustDate(){
		toString.fromString("2002-09-24Z");
	}

	@Test
	public void addDayTimeDurationTest()
	{
		ISO8601DateTime dateTime1 = XacmlTypes.DATETIME.ofAny("2002-03-22T08:23:47-05:00");
		ISO8601DateTime dateTime2 = XacmlTypes.DATETIME.ofAny("2002-03-27T10:23:47-05:00");
		ISO8601DayTimeDuration duration = ISO8601DayTimeDuration.of("P5DT2H0M0S");
		assertEquals(dateTime2, dateTime1.plus(duration));
	}

	@Test
	public void compareTest()
	{
		ISO8601DateTime dateTime1 = XacmlTypes.DATETIME.ofAny("2002-03-22T08:23:47-05:00");
		ISO8601DateTime dateTime2 = XacmlTypes.DATETIME.ofAny("2002-03-22T10:23:47-05:00");
		assertEquals(-1, dateTime1.compareTo(dateTime2));
		dateTime2 = ISO8601DateTime.ofAny("2002-03-22T08:23:47-05:00");
		assertEquals(0, dateTime1.compareTo(dateTime2));
		dateTime2 = ISO8601DateTime.ofAny("2002-03-22T08:22:47-05:00");
		assertEquals(1, dateTime1.compareTo(dateTime2));
	}

	@Test
	public void addYearMonthDuration()
	{
		ISO8601DateTime dateTime1 = XacmlTypes.DATETIME.ofAny("2002-03-22T08:23:47-05:00");
		ISO8601DateTime dateTime2 = XacmlTypes.DATETIME.ofAny("2001-01-22T08:23:47-05:00");
		ISO8601YearMonthDuration duration = ISO8601YearMonthDuration.of("-P1Y2M");
		assertEquals(dateTime2, dateTime1.plus(duration));
	}

	@Test
	public void substractYearMonthDuration()
	{

		ISO8601DateTime dateTime1 = XacmlTypes.DATETIME.ofAny("2002-07-22T08:23:47-05:00");
		ISO8601DateTime dateTime2 = XacmlTypes.DATETIME.ofAny("2006-08-22T08:23:47-05:00");
		ISO8601YearMonthDuration duration = ISO8601YearMonthDuration.of("-P4Y1M");
		assertEquals(dateTime2, dateTime1.minus(duration));
	}

	@Test
	public void fromZonedDateTime()
	{
		ISO8601DateTime dateTime1 = XacmlTypes.DATETIME.ofAny(ZonedDateTime.now());
	}

}
