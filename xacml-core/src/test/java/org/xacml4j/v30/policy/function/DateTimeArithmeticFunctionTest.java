package org.xacml4j.v30.policy.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.spi.function.AnnotiationBasedFunctionProvider;
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
		this.p = new AnnotiationBasedFunctionProvider(
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
