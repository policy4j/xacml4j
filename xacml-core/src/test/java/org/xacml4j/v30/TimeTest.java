package org.xacml4j.v30;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TimeTest
{
	@Test
	public void testParse()
	{
		Time t1 = Time.valueOf("08:23:47-05:00");
		assertEquals(8, t1.getHour());
		assertEquals(23, t1.getMinute());
		assertEquals(47, t1.getSecond());
		assertTrue(t1.isTimezoneSet());
		assertEquals(-5 * 60, t1.getTimezoneOffset());
		Time t2 = Time.valueOf("08:23:47");
		assertEquals(8, t2.getHour());
		assertEquals(23, t2.getMinute());
		assertEquals(47, t2.getSecond());
		assertTrue(t2.isTimezoneSet());
		assertEquals(0, t2.getTimezoneOffset());
	}
}
