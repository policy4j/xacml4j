package com.artagon.xacml.v3.types;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;

import org.junit.Test;

public class TimeTypeTest 
{
	@Test
	public void testCompare()
	{
		TimeValue t1 = TimeType.TIME.create("08:23:47-05:00");
		TimeValue t2 = TimeType.TIME.create("08:23:48-05:00");
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
		TimeValue t1 = TimeType.TIME.create(now);
		TimeValue t2 = TimeType.TIME.create(now);
		System.out.println(t1.toXacmlString());
		assertEquals(t1, t2);
	}
}
