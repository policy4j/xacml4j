package com.artagon.xacml.v30.types;

import org.junit.Test;
import static org.junit.Assert.*;

import com.artagon.xacml.v30.Time;

public class TimeTest 
{
	@Test
	public void testParse()
	{
		Time t1 = Time.create("08:23:47-05:00");
		assertEquals(8, t1.getHour());
		assertEquals(23, t1.getMinute());
		assertEquals(47, t1.getSecond());
		assertTrue(t1.isTimezoneSet());
		assertEquals(-5 * 60, t1.getTimezoneOffset());
		Time t2 = Time.create("08:23:47");
		assertEquals(8, t2.getHour());
		assertEquals(23, t2.getMinute());
		assertEquals(47, t2.getSecond());
		assertTrue(t2.isTimezoneSet());
		assertEquals(0, t2.getTimezoneOffset());
	}
}
