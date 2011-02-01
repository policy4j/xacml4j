package com.artagon.xacml.v30.policy.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v30.spi.function.AnnotiationBasedFunctionProvider;
import com.artagon.xacml.v30.spi.function.FunctionProvider;
import com.artagon.xacml.v30.types.DateTimeType;
import com.artagon.xacml.v30.types.DateTimeValue;
import com.artagon.xacml.v30.types.DayTimeDurationType;
import com.artagon.xacml.v30.types.DayTimeDurationValue;
import com.artagon.xacml.v30.types.YearMonthDurationType;
import com.artagon.xacml.v30.types.YearMonthDurationValue;

public class DateTimeArithmeticFunctionTest 
{
	private FunctionProvider p;
	
	@Before
	public void init() throws Exception
	{
		this.p = new AnnotiationBasedFunctionProvider(DateTimeArithmeticFunctions.class);
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
		DateTimeValue dateTime1 = DateTimeType.DATETIME.create("2002-03-22T08:23:47-05:00");
		DateTimeValue dateTime2 = DateTimeType.DATETIME.create("2002-03-27T10:23:47-05:00");
		DayTimeDurationValue duration = DayTimeDurationType.DAYTIMEDURATION.create("P5DT2H0M0S");
		assertEquals(dateTime2, DateTimeArithmeticFunctions.add(dateTime1, duration));
		
	}
	
	@Test
	public void testDateTimeAddYearMonthDuration()
	{
		DateTimeValue dateTime1 = DateTimeType.DATETIME.create("2002-03-22T08:23:47-05:00");
		DateTimeValue dateTime2 = DateTimeType.DATETIME.create("2001-01-22T08:23:47-05:00");
		YearMonthDurationValue duration = YearMonthDurationType.YEARMONTHDURATION.create("-P1Y2M");
		assertEquals(dateTime2, DateTimeArithmeticFunctions.add(dateTime1, duration));

	}
}
