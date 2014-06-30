package org.xacml4j.v30.policy.function;

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
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.spi.function.AnnotationBasedFunctionProvider;
import org.xacml4j.v30.spi.function.FunctionProvider;
import org.xacml4j.v30.types.DateTimeExp;
import org.xacml4j.v30.types.DayTimeDurationExp;
import org.xacml4j.v30.types.YearMonthDurationExp;


public class DateTimeArithmeticFunctionTest
{
	private FunctionProvider p;

	@Before
	public void init() throws Exception
	{
		this.p = new AnnotationBasedFunctionProvider(
				DateTimeArithmeticFunctions.class);
	}

	@Test
	public void testProvidedFunctions()
	{
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:3.0:function:dateTime-add-dayTimeDuration"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:3.0:function:dateTime-add-yearMonthDuration"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-dayTimeDuration"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-yearMonthDuration"));

		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:dateTime-add-dayTimeDuration"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:dateTime-add-yearMonthDuration"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:dateTime-subtract-dayTimeDuration"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:dateTime-subtract-yearMonthDuration"));

		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:3.0:function:date-add-yearMonthDuration"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:3.0:function:date-subtract-yearMonthDuration"));

		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:date-add-yearMonthDuration"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:date-subtract-yearMonthDuration"));
	}

	@Test
	public void testDateTimeAddDayTimeDuration()
	{
		DateTimeExp dateTime1 = DateTimeExp.valueOf("2002-03-22T08:23:47-05:00");
		DateTimeExp dateTime2 = DateTimeExp.valueOf("2002-03-27T10:23:47-05:00");
		DayTimeDurationExp duration = DayTimeDurationExp.valueOf("P5DT2H0M0S");
		assertEquals(dateTime2, DateTimeArithmeticFunctions.add(dateTime1, duration));

	}

	@Test
	public void testDateTimeAddYearMonthDuration()
	{
		DateTimeExp dateTime1 = DateTimeExp.valueOf("2002-03-22T08:23:47-05:00");
		DateTimeExp dateTime2 = DateTimeExp.valueOf("2001-01-22T08:23:47-05:00");
		YearMonthDurationExp duration = YearMonthDurationExp.valueOf("-P1Y2M");
		assertEquals(dateTime2, DateTimeArithmeticFunctions.add(dateTime1, duration));

	}
}
