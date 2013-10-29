package org.xacml4j.v30.types;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Before;
import org.junit.Test;


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
		DateTimeExp v = t1.create(c);
		System.out.println(v.toXacmlString());
	}

	@Test
	public void testFromXacmlString(){
		DateTimeExp value = t1.fromXacmlString("2002-05-30T09:30:10-06:00");
		assertEquals(2002, value.getValue().getYear());
		assertEquals(5, value.getValue().getMonth());
		assertEquals(30, value.getValue().getDay());
		assertEquals(9, value.getValue().getHour());
		assertEquals(30, value.getValue().getMinute());
		assertEquals(10, value.getValue().getSecond());
		assertEquals(-360, value.getValue().getTimezoneOffset());
		assertEquals("2002-05-30T09:30:10-06:00", value.toXacmlString());
	}

	@Test
	public void testFromXacmlStringNoTimeZone(){
		DateTimeExp value = t1.fromXacmlString("2002-05-30T09:30:10");
		assertEquals(2002, value.getValue().getYear());
		assertEquals(5, value.getValue().getMonth());
		assertEquals(30, value.getValue().getDay());
		assertEquals(9, value.getValue().getHour());
		assertEquals(30, value.getValue().getMinute());
		assertEquals(10, value.getValue().getSecond());
		assertEquals(0, value.getValue().getTimezoneOffset());
		assertEquals("2002-05-30T09:30:10Z", value.toXacmlString());

	}

	@Test(expected=IllegalArgumentException.class)
	public void testFromXacmlStringJustDate(){
		t1.fromXacmlString("2002-09-24Z");
	}

	@Test
	public void addDayTimeDurationTest()
	{
		DateTimeExp dateTime1 = t1.create("2002-03-22T08:23:47-05:00");
		DateTimeExp dateTime2 = t1.create("2002-03-27T10:23:47-05:00");
		DayTimeDurationExp duration = DayTimeDurationType.DAYTIMEDURATION.create("P5DT2H0M0S");
		assertEquals(dateTime2, dateTime1.add(duration));
	}

	@Test
	public void compareTest()
	{
		DateTimeExp dateTime1 = t1.create("2002-03-22T08:23:47-05:00");
		DateTimeExp dateTime2 = t1.create("2002-03-22T10:23:47-05:00");
		assertEquals(-1, dateTime1.compareTo(dateTime2));
		dateTime2 = t1.create("2002-03-22T08:23:47-05:00");
		assertEquals(0, dateTime1.compareTo(dateTime2));
		dateTime2 = t1.create("2002-03-22T08:22:47-05:00");
		assertEquals(1, dateTime1.compareTo(dateTime2));
	}

	@Test
	public void addYearMonthDuration()
	{
		DateTimeExp dateTime1 = t1.create("2002-03-22T08:23:47-05:00");
		DateTimeExp dateTime2 = t1.create("2001-01-22T08:23:47-05:00");
		YearMonthDurationExp duration = YearMonthDurationType.YEARMONTHDURATION.create("-P1Y2M");
		assertEquals(dateTime2, dateTime1.add(duration));
	}

	@Test
	public void substractYearMonthDuration()
	{

		DateTimeExp dateTime1 = t1.create("2002-07-22T08:23:47-05:00");
		DateTimeExp dateTime2 = t1.create("2006-08-22T08:23:47-05:00");
		YearMonthDurationExp duration = YearMonthDurationType.YEARMONTHDURATION.create("-P4Y1M");
		assertEquals(dateTime2, dateTime1.subtract(duration));
	}

}
