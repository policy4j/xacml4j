package com.artagon.xacml.v30.types;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v30.types.DateTimeType;
import com.artagon.xacml.v30.types.DateTimeValue;
import com.artagon.xacml.v30.types.DayTimeDurationType;
import com.artagon.xacml.v30.types.DayTimeDurationValue;
import com.artagon.xacml.v30.types.YearMonthDurationType;
import com.artagon.xacml.v30.types.YearMonthDurationValue;


public class DateTimeTypeTest 
{
	private DateTimeType t1;
	
	@Before
	public void init() throws Exception{
		this.t1 = DateTimeType.DATETIME;
	}
	
	@Test
	public void testCreateFromCalendar()
	{
		Calendar c = new GregorianCalendar();
		DateTimeValue v = t1.create(c);
		System.out.println(v.toXacmlString());
	}
	
	@Test
	public void testFromXacmlString(){
		DateTimeValue value = t1.fromXacmlString("2002-05-30T09:30:10-06:00");
		assertEquals(2002, value.getValue().getYear());
		assertEquals(5, value.getValue().getMonth());
		assertEquals(30, value.getValue().getDay());
		assertEquals(9, value.getValue().getHour());
		assertEquals(30, value.getValue().getMinute());
		assertEquals(10, value.getValue().getSecond());
		assertEquals(-360, value.getValue().getTimezone());
		assertEquals("2002-05-30T09:30:10-06:00", value.toXacmlString());
	}
	
	@Test
	public void testFromXacmlStringNoTimeZone(){
		DateTimeValue value = t1.fromXacmlString("2002-05-30T09:30:10");
		assertEquals(2002, value.getValue().getYear());
		assertEquals(5, value.getValue().getMonth());
		assertEquals(30, value.getValue().getDay());
		assertEquals(9, value.getValue().getHour());
		assertEquals(30, value.getValue().getMinute());
		assertEquals(10, value.getValue().getSecond());
		assertEquals(0, value.getValue().getTimezone());
		assertEquals("2002-05-30T09:30:10Z", value.toXacmlString());
		
	}	
	
	@Test(expected=IllegalArgumentException.class)
	public void testFromXacmlStringJustDate(){
		t1.fromXacmlString("2002-09-24Z");
	}
	
	@Test
	public void addDayTimeDurationTest()
	{
		DateTimeValue dateTime1 = t1.create("2002-03-22T08:23:47-05:00");
		DateTimeValue dateTime2 = t1.create("2002-03-27T10:23:47-05:00");
		DayTimeDurationValue duration = DayTimeDurationType.DAYTIMEDURATION.create("P5DT2H0M0S");
		assertEquals(dateTime2, dateTime1.add(duration));
	}
	
	@Test
	public void compareTest()
	{
		DateTimeValue dateTime1 = t1.create("2002-03-22T08:23:47-05:00");
		DateTimeValue dateTime2 = t1.create("2002-03-22T10:23:47-05:00");
		assertEquals(-1, dateTime1.compareTo(dateTime2));
		dateTime2 = t1.create("2002-03-22T08:23:47-05:00");
		assertEquals(0, dateTime1.compareTo(dateTime2));
		dateTime2 = t1.create("2002-03-22T08:22:47-05:00");
		assertEquals(1, dateTime1.compareTo(dateTime2));
	}
	
	@Test
	public void addYearMonthDuration()
	{
		DateTimeValue dateTime1 = t1.create("2002-03-22T08:23:47-05:00");
		DateTimeValue dateTime2 = t1.create("2001-01-22T08:23:47-05:00");
		YearMonthDurationValue duration = YearMonthDurationType.YEARMONTHDURATION.create("-P1Y2M");
		assertEquals(dateTime2, dateTime1.add(duration));
	}
	
	@Test
	public void substractYearMonthDuration()
	{
		
		DateTimeValue dateTime1 = t1.create("2002-07-22T08:23:47-05:00");
		DateTimeValue dateTime2 = t1.create("2006-08-22T08:23:47-05:00");
		YearMonthDurationValue duration = YearMonthDurationType.YEARMONTHDURATION.create("-P4Y1M");
		assertEquals(dateTime2, dateTime1.subtract(duration));
	}
	
}
