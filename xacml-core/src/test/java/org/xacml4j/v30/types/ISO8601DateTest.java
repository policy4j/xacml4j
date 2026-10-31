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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.GregorianCalendar;

import org.junit.jupiter.api.Test;

public class ISO8601DateTest
{

	@Test
	public void testFromXacmlString(){
		ISO8601Date value = XacmlTypes.DATE.ofAny("2002-09-24Z");
		assertEquals(2002, value.get().getYear());
		assertEquals(9, value.getMonthValue());
		assertEquals(24, value.get().getDayOfMonth());
		TypeToString toString = TypeToString.Types.DATE;
		assertEquals("2002-09-24Z", toString.toString(value));
	}


	@Test
	public void testFromXacmlStringJustDate() {
		assertThrows(DateTimeParseException.class, () ->
			XacmlTypes.DATE.ofAny("2002-05-30T09:30:10-06:00"));
	}

	@Test
	public void testCreateFromJanuaryCalendar()
	{
		assertCalendarDate(new GregorianCalendar(2024, GregorianCalendar.JANUARY, 31),
				2024, 1, 31);
	}

	@Test
	public void testCreateFromOctoberCalendar()
	{
		assertCalendarDate(new GregorianCalendar(2024, GregorianCalendar.OCTOBER, 31),
				2024, 10, 31);
	}

	@Test
	public void testCreateFromDecemberCalendar()
	{
		assertCalendarDate(new GregorianCalendar(2024, GregorianCalendar.DECEMBER, 31),
				2024, 12, 31);
	}

	@Test
	public void testCreateFromLeapDayCalendar()
	{
		assertCalendarDate(new GregorianCalendar(2024, GregorianCalendar.FEBRUARY, 29),
				2024, 2, 29);
	}

	private void assertCalendarDate(GregorianCalendar calendar,
			int expectedYear, int expectedMonth, int expectedDay)
	{
		ISO8601Date date = XacmlTypes.DATE.ofAny(calendar);
		assertEquals(expectedYear, date.getYear());
		assertEquals(expectedMonth, date.getMonthValue());
		assertEquals(expectedDay, date.getDayOfMonth());
	}

	@Test
	public void testCreateFromZonedDateTime()
	{
		ZonedDateTime now = ZonedDateTime.now();
		XacmlTypes.DATE.ofAny(now);
	}
}
