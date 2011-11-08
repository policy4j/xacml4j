package com.artagon.xacml.v30.types;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;

import org.junit.Test;

public class TimeTypeTest 
{
	@Test
	public void testCompare()
	{
		TimeValueExp t1 = TimeType.TIME.create("08:23:47-05:00");
		TimeValueExp t2 = TimeType.TIME.create("08:23:48-05:00");
		assertEquals(-1, t1.compareTo(t2));
		t2 = TimeType.TIME.create("08:23:47-05:00");
		assertEquals(0, t1.compareTo(t2));
		t2 = TimeType.TIME.create("08:23:46-05:00");
		assertEquals(1, t1.compareTo(t2));
	}
	
	@Test
	public void testCreateFromCalendar()
	{
		Calendar now = Calendar.getInstance();
		TimeValueExp t1 = TimeType.TIME.create(now);
		TimeValueExp t2 = TimeType.TIME.create(now);
		System.out.println(t1.toXacmlString());
		assertEquals(t1, t2);
	}
}
