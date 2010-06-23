package com.artagon.xacml.v3.policy.function;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.artagon.xacml.v3.types.DateTimeType.DateTimeValue;
import com.artagon.xacml.v3.types.DayTimeDurationType.DayTimeDurationValue;
import com.artagon.xacml.v3.types.YearMonthDurationType.YearMonthDurationValue;

public class DateTimeArithmeticFunctionTest 
{
	@Test
	public void testDateTimeAddDayTimeDuration()
	{
		DateTimeValue dateTime1 = XacmlDataTypes.DATETIME.create("2002-03-22T08:23:47-05:00");
		DateTimeValue dateTime2 = XacmlDataTypes.DATETIME.create("2002-03-27T10:23:47-05:00");
		DayTimeDurationValue duration = XacmlDataTypes.DAYTIMEDURATION.create("P5DT2H0M0S");
		assertEquals(dateTime2, DateTimeArithmeticFunctions.add(dateTime1, duration));
		
	}
	
	@Test
	public void testDateTimeAddYearMonthDuration()
	{
		DateTimeValue dateTime1 = XacmlDataTypes.DATETIME.create("2002-03-22T08:23:47-05:00");
		DateTimeValue dateTime2 = XacmlDataTypes.DATETIME.create("2001-01-22T08:23:47-05:00");
		YearMonthDurationValue duration = XacmlDataTypes.YEARMONTHDURATION.create("-P1Y2M");
		assertEquals(dateTime2, DateTimeArithmeticFunctions.add(dateTime1, duration));

	}
	

}
