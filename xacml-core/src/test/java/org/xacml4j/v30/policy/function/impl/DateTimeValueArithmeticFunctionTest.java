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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.policy.function.FunctionProviderBuilder;
import org.xacml4j.v30.types.DateTimeValue;
import org.xacml4j.v30.types.DayTimeDurationValue;
import org.xacml4j.v30.types.XacmlTypes;
import org.xacml4j.v30.types.YearMonthDurationValue;


public class DateTimeValueArithmeticFunctionTest
{
	private FunctionProvider p;

	@Before
	public void init() throws Exception
	{
		this.p = FunctionProviderBuilder
				.builder()
				.withDefaultFunctions()
				.build();
	}

	@Test
	public void testProvidedFunctions()
	{
		assertTrue(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:dateTime-add-dayTimeDuration").isPresent());
		assertTrue(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:dateTime-add-yearMonthDuration").isPresent());
		assertTrue(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:dateTime-subtract-dayTimeDuration").isPresent());
		assertTrue(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:dateTime-subtract-yearMonthDuration").isPresent());

		assertTrue(p.getFunction("urn:oasis:names:tc:xacml:3.0:function:dateTime-add-dayTimeDuration").isPresent());
		assertTrue(p.getFunction("urn:oasis:names:tc:xacml:3.0:function:dateTime-add-yearMonthDuration").isPresent());
		assertTrue(p.getFunction("urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-dayTimeDuration").isPresent());
		assertTrue(p.getFunction("urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-yearMonthDuration").isPresent());



		assertTrue(p.getFunction("urn:oasis:names:tc:xacml:3.0:function:date-add-yearMonthDuration").isPresent());
		assertTrue(p.getFunction("urn:oasis:names:tc:xacml:3.0:function:date-subtract-yearMonthDuration").isPresent());

		assertTrue(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:date-add-yearMonthDuration").isPresent());
		assertTrue(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:date-subtract-yearMonthDuration").isPresent());
	}

	@Test
	public void testDateTimeAddDayTimeDuration()
	{
		DateTimeValue dateTime1 = XacmlTypes.DATETIME.of("2002-03-22T08:23:47-05:00");
		DateTimeValue dateTime2 = XacmlTypes.DATETIME.of("2002-03-27T10:23:47-05:00");
		DayTimeDurationValue duration = XacmlTypes.DAYTIMEDURATION.of("P5DT2H0M0S");
		assertEquals(dateTime2, DateTimeArithmeticFunctions.add(dateTime1, duration));

	}

	@Test
	public void testDateTimeAddYearMonthDuration()
	{
		DateTimeValue dateTime1 = XacmlTypes.DATETIME.of("2002-03-22T08:23:47-05:00");
		DateTimeValue dateTime2 = XacmlTypes.DATETIME.of("2001-01-22T08:23:47-05:00");
		YearMonthDurationValue duration = YearMonthDurationValue.of("-P1Y2M");
		assertEquals(dateTime2, DateTimeArithmeticFunctions.add(dateTime1, duration));

	}
}
