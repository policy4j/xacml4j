package org.xacml4j.v30.types;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;

import org.junit.Test;

public class TimeTypeTest
{
	@Test
	public void testCompare()
	{
		TimeExp t1 = TimeExp.valueOf("08:23:47-05:00");
		TimeExp t2 = TimeExp.valueOf("08:23:48-05:00");
		assertEquals(-1, t1.compareTo(t2));
		t2 = TimeExp.valueOf("08:23:47-05:00");
		assertEquals(0, t1.compareTo(t2));
		t2 = TimeExp.valueOf("08:23:46-05:00");
		assertEquals(1, t1.compareTo(t2));
	}

	@Test
	public void testCreateFromCalendar()
	{
		Calendar now = Calendar.getInstance();
		TimeExp t1 = TimeExp.valueOf(now);
		TimeExp t2 = TimeExp.valueOf(now);
		assertEquals(t1, t2);
	}
}
