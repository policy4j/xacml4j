package com.artagon.xacml.v3.policy.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.spi.FunctionProvider;
import com.artagon.xacml.v3.spi.function.AnnotiationBasedFunctionProvider;
import com.artagon.xacml.v3.types.DateTimeType.DateTimeValue;
import com.artagon.xacml.v3.types.DayTimeDurationType.DayTimeDurationValue;
import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.artagon.xacml.v3.types.YearMonthDurationType.YearMonthDurationValue;

public class DateTimeArithmeticFunctionTest 
{
	private FunctionProvider p;
	
	@Before
	public void init(){
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
