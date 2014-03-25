package org.xacml4j.v30.types;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;


public class DateTimeTypeTest
{
	private TypeToString toString;

	@Before
	public void init() throws Exception{
		this.toString = TypeToString.Types.getIndex().get(XacmlTypes.DATETIME).get();
	}
	@Test
	public void testFromXacmlString(){
		DateTimeExp value = (DateTimeExp)toString.fromString("2002-05-30T09:30:10-06:00");
		assertEquals(2002, value.getValue().getYear());
		assertEquals(5, value.getValue().getMonth());
		assertEquals(30, value.getValue().getDay());
		assertEquals(9, value.getValue().getHour());
		assertEquals(30, value.getValue().getMinute());
		assertEquals(10, value.getValue().getSecond());
		assertEquals(-360, value.getValue().getTimezoneOffset());
		assertEquals("2002-05-30T09:30:10-06:00", toString.toString(value));
	}

	@Test
	public void testFromXacmlStringNoTimeZone(){
		DateTimeExp value = DateTimeExp.valueOf("2002-05-30T09:30:10");
		assertEquals(2002, value.getValue().getYear());
		assertEquals(5, value.getValue().getMonth());
		assertEquals(30, value.getValue().getDay());
		assertEquals(9, value.getValue().getHour());
		assertEquals(30, value.getValue().getMinute());
		assertEquals(10, value.getValue().getSecond());
		assertEquals(0, value.getValue().getTimezoneOffset());
		assertEquals("2002-05-30T09:30:10Z", toString.toString(value));

	}

	@Test(expected=IllegalArgumentException.class)
	public void testFromXacmlStringJustDate(){
		toString.fromString("2002-09-24Z");
	}

	@Test
	public void addDayTimeDurationTest()
	{
		DateTimeExp dateTime1 = DateTimeExp.valueOf("2002-03-22T08:23:47-05:00");
		DateTimeExp dateTime2 = DateTimeExp.valueOf("2002-03-27T10:23:47-05:00");
		DayTimeDurationExp duration = DayTimeDurationExp.valueOf("P5DT2H0M0S");
		assertEquals(dateTime2, dateTime1.add(duration));
	}

	@Test
	public void compareTest()
	{
		DateTimeExp dateTime1 = DateTimeExp.valueOf("2002-03-22T08:23:47-05:00");
		DateTimeExp dateTime2 = DateTimeExp.valueOf("2002-03-22T10:23:47-05:00");
		assertEquals(-1, dateTime1.compareTo(dateTime2));
		dateTime2 = DateTimeExp.valueOf("2002-03-22T08:23:47-05:00");
		assertEquals(0, dateTime1.compareTo(dateTime2));
		dateTime2 = DateTimeExp.valueOf("2002-03-22T08:22:47-05:00");
		assertEquals(1, dateTime1.compareTo(dateTime2));
	}

	@Test
	public void addYearMonthDuration()
	{
		DateTimeExp dateTime1 = DateTimeExp.valueOf("2002-03-22T08:23:47-05:00");
		DateTimeExp dateTime2 = DateTimeExp.valueOf("2001-01-22T08:23:47-05:00");
		YearMonthDurationExp duration = YearMonthDurationExp.valueOf("-P1Y2M");
		assertEquals(dateTime2, dateTime1.add(duration));
	}

	@Test
	public void substractYearMonthDuration()
	{

		DateTimeExp dateTime1 = DateTimeExp.valueOf("2002-07-22T08:23:47-05:00");
		DateTimeExp dateTime2 = DateTimeExp.valueOf("2006-08-22T08:23:47-05:00");
		YearMonthDurationExp duration = YearMonthDurationExp.valueOf("-P4Y1M");
		assertEquals(dateTime2, dateTime1.subtract(duration));
	}

}
