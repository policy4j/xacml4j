package org.xacml4j.v30.policy.function.impl;

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


import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.xacml4j.v30.types.ISO8601Date;
import org.xacml4j.v30.types.ISO8601DateTime;
import org.xacml4j.v30.types.ISO8601DayTimeDuration;
import org.xacml4j.v30.types.ISO8601YearMonthDuration;

public class DateTimeArithmeticBoundaryTest
{
	private enum Operation
	{
		DATE_ADD_YEAR_MONTH, DATE_SUBTRACT_YEAR_MONTH,
		DATETIME_ADD_YEAR_MONTH, DATETIME_SUBTRACT_YEAR_MONTH,
		DATETIME_ADD_DAY_TIME, DATETIME_SUBTRACT_DAY_TIME
	}

	public static Collection<Object[]> cases()
	{
		return Arrays.asList(new Object[][] {
			{Operation.DATE_ADD_YEAR_MONTH, "2002-03-22", "P1Y2M", "2003-05-22"},
			{Operation.DATE_ADD_YEAR_MONTH, "2002-03-22", "-P1Y2M", "2001-01-22"},
			{Operation.DATE_ADD_YEAR_MONTH, "2002-03-22", "P0M", "2002-03-22"},
			{Operation.DATE_ADD_YEAR_MONTH, "2020-01-31", "P1M", "2020-02-29"},
			{Operation.DATE_ADD_YEAR_MONTH, "2021-01-31", "P1M", "2021-02-28"},
			{Operation.DATE_ADD_YEAR_MONTH, "2020-03-31", "-P1M", "2020-02-29"},
			{Operation.DATE_ADD_YEAR_MONTH, "2021-03-31", "-P1M", "2021-02-28"},
			{Operation.DATE_ADD_YEAR_MONTH, "2020-02-29", "P1Y1M", "2021-03-29"},
			{Operation.DATE_ADD_YEAR_MONTH, "2020-02-29", "-P1Y1M", "2019-01-29"},
			{Operation.DATE_SUBTRACT_YEAR_MONTH, "2002-03-22", "P1Y2M", "2001-01-22"},
			{Operation.DATE_SUBTRACT_YEAR_MONTH, "2002-03-22", "-P1Y2M", "2003-05-22"},
			{Operation.DATE_SUBTRACT_YEAR_MONTH, "2002-03-22", "P0M", "2002-03-22"},
			{Operation.DATE_SUBTRACT_YEAR_MONTH, "2020-01-31", "-P1M", "2020-02-29"},
			{Operation.DATE_SUBTRACT_YEAR_MONTH, "2021-01-31", "-P1M", "2021-02-28"},
			{Operation.DATE_SUBTRACT_YEAR_MONTH, "2020-03-31", "P1M", "2020-02-29"},
			{Operation.DATE_SUBTRACT_YEAR_MONTH, "2021-03-31", "P1M", "2021-02-28"},
			{Operation.DATE_SUBTRACT_YEAR_MONTH, "2020-02-29", "-P1Y1M", "2021-03-29"},
			{Operation.DATE_SUBTRACT_YEAR_MONTH, "2020-02-29", "P1Y1M", "2019-01-29"},
			{Operation.DATETIME_ADD_YEAR_MONTH, "2002-03-22T08:23:47-05:00", "P1Y2M", "2003-05-22T08:23:47-05:00"},
			{Operation.DATETIME_ADD_YEAR_MONTH, "2002-03-22T08:23:47-05:00", "-P1Y2M", "2001-01-22T08:23:47-05:00"},
			{Operation.DATETIME_ADD_YEAR_MONTH, "2002-03-22T08:23:47-05:00", "P0M", "2002-03-22T08:23:47-05:00"},
			{Operation.DATETIME_ADD_YEAR_MONTH, "2020-01-31T08:23:47-05:00", "P1M", "2020-02-29T08:23:47-05:00"},
			{Operation.DATETIME_ADD_YEAR_MONTH, "2021-01-31T08:23:47-05:00", "P1M", "2021-02-28T08:23:47-05:00"},
			{Operation.DATETIME_ADD_YEAR_MONTH, "2020-03-31T08:23:47-05:00", "-P1M", "2020-02-29T08:23:47-05:00"},
			{Operation.DATETIME_ADD_YEAR_MONTH, "2021-03-31T08:23:47-05:00", "-P1M", "2021-02-28T08:23:47-05:00"},
			{Operation.DATETIME_ADD_YEAR_MONTH, "2020-02-29T08:23:47-05:00", "P1Y1M", "2021-03-29T08:23:47-05:00"},
			{Operation.DATETIME_ADD_YEAR_MONTH, "2020-02-29T08:23:47-05:00", "-P1Y1M", "2019-01-29T08:23:47-05:00"},
			{Operation.DATETIME_SUBTRACT_YEAR_MONTH, "2002-03-22T08:23:47-05:00", "P1Y2M", "2001-01-22T08:23:47-05:00"},
			{Operation.DATETIME_SUBTRACT_YEAR_MONTH, "2002-03-22T08:23:47-05:00", "-P1Y2M", "2003-05-22T08:23:47-05:00"},
			{Operation.DATETIME_SUBTRACT_YEAR_MONTH, "2002-03-22T08:23:47-05:00", "P0M", "2002-03-22T08:23:47-05:00"},
			{Operation.DATETIME_SUBTRACT_YEAR_MONTH, "2020-01-31T08:23:47-05:00", "-P1M", "2020-02-29T08:23:47-05:00"},
			{Operation.DATETIME_SUBTRACT_YEAR_MONTH, "2021-01-31T08:23:47-05:00", "-P1M", "2021-02-28T08:23:47-05:00"},
			{Operation.DATETIME_SUBTRACT_YEAR_MONTH, "2020-03-31T08:23:47-05:00", "P1M", "2020-02-29T08:23:47-05:00"},
			{Operation.DATETIME_SUBTRACT_YEAR_MONTH, "2021-03-31T08:23:47-05:00", "P1M", "2021-02-28T08:23:47-05:00"},
			{Operation.DATETIME_SUBTRACT_YEAR_MONTH, "2020-02-29T08:23:47-05:00", "-P1Y1M", "2021-03-29T08:23:47-05:00"},
			{Operation.DATETIME_SUBTRACT_YEAR_MONTH, "2020-02-29T08:23:47-05:00", "P1Y1M", "2019-01-29T08:23:47-05:00"},
			{Operation.DATETIME_ADD_DAY_TIME, "2002-03-22T08:23:47-05:00", "P5DT2H", "2002-03-27T10:23:47-05:00"},
			{Operation.DATETIME_ADD_DAY_TIME, "2002-03-22T08:23:47-05:00", "-P5DT2H", "2002-03-17T06:23:47-05:00"},
			{Operation.DATETIME_ADD_DAY_TIME, "2002-03-22T08:23:47-05:00", "PT0S", "2002-03-22T08:23:47-05:00"},
			{Operation.DATETIME_ADD_DAY_TIME, "2020-01-31T23:30:00-05:00", "PT2H", "2020-02-01T01:30:00-05:00"},
			{Operation.DATETIME_ADD_DAY_TIME, "2020-03-01T00:30:00-05:00", "-PT2H", "2020-02-29T22:30:00-05:00"},
			{Operation.DATETIME_ADD_DAY_TIME, "2020-02-28T08:23:47-05:00", "P1D", "2020-02-29T08:23:47-05:00"},
			{Operation.DATETIME_ADD_DAY_TIME, "2020-03-01T08:23:47-05:00", "-P1D", "2020-02-29T08:23:47-05:00"},
			{Operation.DATETIME_SUBTRACT_DAY_TIME, "2002-03-22T08:23:47-05:00", "P5DT2H", "2002-03-17T06:23:47-05:00"},
			{Operation.DATETIME_SUBTRACT_DAY_TIME, "2002-03-22T08:23:47-05:00", "-P5DT2H", "2002-03-27T10:23:47-05:00"},
			{Operation.DATETIME_SUBTRACT_DAY_TIME, "2002-03-22T08:23:47-05:00", "PT0S", "2002-03-22T08:23:47-05:00"},
			{Operation.DATETIME_SUBTRACT_DAY_TIME, "2020-01-31T23:30:00-05:00", "-PT2H", "2020-02-01T01:30:00-05:00"},
			{Operation.DATETIME_SUBTRACT_DAY_TIME, "2020-03-01T00:30:00-05:00", "PT2H", "2020-02-29T22:30:00-05:00"},
			{Operation.DATETIME_SUBTRACT_DAY_TIME, "2020-02-28T08:23:47-05:00", "-P1D", "2020-02-29T08:23:47-05:00"},
			{Operation.DATETIME_SUBTRACT_DAY_TIME, "2020-03-01T08:23:47-05:00", "P1D", "2020-02-29T08:23:47-05:00"}
		});
	}

	private Operation operation;
	private String start;
	private String duration;
	private String expected;

	public void initDateTimeArithmeticBoundaryTest(Operation operation, String start,
			String duration, String expected)
	{
		this.operation = operation;
		this.start = start;
		this.duration = duration;
		this.expected = expected;
	}

	@MethodSource("cases")
	@ParameterizedTest(name = "{0}: {1}, {2} -> {3}")
	public void arithmeticPreservesSignedCalendarBoundaries(Operation operation, String start, String duration, String expected)
	{
		initDateTimeArithmeticBoundaryTest(operation, start, duration, expected);
		String actual;
		switch (operation) {
		case DATE_ADD_YEAR_MONTH:
			actual = DateTimeArithmeticFunctions.add(ISO8601Date.of(start),
					ISO8601YearMonthDuration.of(duration)).toXacmlString();
			break;
		case DATE_SUBTRACT_YEAR_MONTH:
			actual = DateTimeArithmeticFunctions.subtract(ISO8601Date.of(start),
					ISO8601YearMonthDuration.of(duration)).toXacmlString();
			break;
		case DATETIME_ADD_YEAR_MONTH:
			actual = DateTimeArithmeticFunctions.add(ISO8601DateTime.of(start),
					ISO8601YearMonthDuration.of(duration)).toXacmlString();
			break;
		case DATETIME_SUBTRACT_YEAR_MONTH:
			actual = DateTimeArithmeticFunctions.subtract(ISO8601DateTime.of(start),
					ISO8601YearMonthDuration.of(duration)).toXacmlString();
			break;
		case DATETIME_ADD_DAY_TIME:
			actual = DateTimeArithmeticFunctions.add(ISO8601DateTime.of(start),
					ISO8601DayTimeDuration.of(duration)).toXacmlString();
			break;
		case DATETIME_SUBTRACT_DAY_TIME:
			actual = DateTimeArithmeticFunctions.subtract(ISO8601DateTime.of(start),
					ISO8601DayTimeDuration.of(duration)).toXacmlString();
			break;
		default:
			throw new AssertionError(operation);
		}
		assertEquals(expected, actual);
	}
}
