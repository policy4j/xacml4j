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

import org.junit.Test;

public class ISO8601DateTimeArithmeticTest
{
	private final ISO8601DateTime start = ISO8601DateTime.of("2002-03-22T08:23:47-05:00");

	@Test
	public void addPositiveDayTimeDuration()
	{
		assertEquals("2002-03-27T10:23:47-05:00",
				start.plus(ISO8601DayTimeDuration.of("P5DT2H0M0S")).toXacmlString());
	}

	@Test
	public void addNegativeDayTimeDuration()
	{
		assertEquals("2002-03-17T06:23:47-05:00",
				start.plus(ISO8601DayTimeDuration.of("-P5DT2H0M0S")).toXacmlString());
	}

	@Test
	public void addZeroDayTimeDuration()
	{
		assertEquals("2002-03-22T08:23:47-05:00",
				start.plus(ISO8601DayTimeDuration.of("PT0S")).toXacmlString());
	}

	@Test
	public void subtractPositiveDayTimeDuration()
	{
		// Operands from OASIS IIC104; expected date computed independently.
		assertEquals("2002-03-17T06:23:47-05:00",
				start.minus(ISO8601DayTimeDuration.of("P5DT2H0M0S")).toXacmlString());
	}

	@Test
	public void subtractNegativeDayTimeDuration()
	{
		assertEquals("2002-03-27T10:23:47-05:00",
				start.minus(ISO8601DayTimeDuration.of("-P5DT2H0M0S")).toXacmlString());
	}

	@Test
	public void subtractZeroDayTimeDuration()
	{
		assertEquals("2002-03-22T08:23:47-05:00",
				start.minus(ISO8601DayTimeDuration.of("PT0S")).toXacmlString());
	}

	@Test
	public void addPositiveYearMonthDuration()
	{
		assertEquals("2003-05-22T08:23:47-05:00",
				start.plus(ISO8601YearMonthDuration.of("P1Y2M")).toXacmlString());
	}

	@Test
	public void addNegativeYearMonthDuration()
	{
		assertEquals("2001-01-22T08:23:47-05:00",
				start.plus(ISO8601YearMonthDuration.of("-P1Y2M")).toXacmlString());
	}

	@Test
	public void addZeroYearMonthDuration()
	{
		assertEquals("2002-03-22T08:23:47-05:00",
				start.plus(ISO8601YearMonthDuration.of("P0M")).toXacmlString());
	}

	@Test
	public void subtractPositiveYearMonthDuration()
	{
		assertEquals("2001-01-22T08:23:47-05:00",
				start.minus(ISO8601YearMonthDuration.of("P1Y2M")).toXacmlString());
	}

	@Test
	public void subtractNegativeYearMonthDuration()
	{
		assertEquals("2003-05-22T08:23:47-05:00",
				start.minus(ISO8601YearMonthDuration.of("-P1Y2M")).toXacmlString());
	}

	@Test
	public void subtractZeroYearMonthDuration()
	{
		assertEquals("2002-03-22T08:23:47-05:00",
				start.minus(ISO8601YearMonthDuration.of("P0M")).toXacmlString());
	}
}
