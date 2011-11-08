package com.artagon.xacml.v30.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.artagon.xacml.v30.core.DayTimeDuration;

public class DayTimeDurationTest 
{
	@Test
	public void testCreateValidDurationFromLexicalRepresentation()
	{
		DayTimeDuration v1 = DayTimeDuration.create("P3DT10H30M");
		assertEquals(3, v1.getDays());
		assertEquals(10, v1.getHours());
		assertEquals(30, v1.getMinutes());
		assertEquals(0, v1.getSeconds());
		assertEquals("P3DT10H30M", v1.toString());
		DayTimeDuration v2 = DayTimeDuration.create("PT10H10S");
		assertEquals(0, v2.getDays());
		assertEquals(10, v2.getHours());
		assertEquals(0, v2.getMinutes());
		assertEquals(10, v2.getSeconds());
		assertEquals("PT10H10S", v2.toString());
	}
	
	@Test
	public void testCreateValidDurationViaExplicitCtor()
	{
		DayTimeDuration v1 = new DayTimeDuration(true, 3, 10, 30, 0);
		assertEquals(3, v1.getDays());
		assertEquals(10, v1.getHours());
		assertEquals(30, v1.getMinutes());
		assertEquals(0, v1.getSeconds());
		assertTrue(v1.isPositive());
		DayTimeDuration v2 = DayTimeDuration.create("P3DT10H30M");
		assertEquals(v1, v2);
	}
	
	@Test
	public void testAddDurations()
	{
		DayTimeDuration v1 = DayTimeDuration.create("P3DT10H30M");
		DayTimeDuration v2 = DayTimeDuration.create("PT10H10S");
		DayTimeDuration v3 = v1.add(v2);
		assertEquals(3, v3.getDays());
		assertEquals(20, v3.getHours());
		assertEquals(30, v3.getMinutes());
		assertEquals(10, v3.getSeconds());
		assertEquals("P3DT10H30M", v1.toString());
	}
	
	@Test
	public void testSubstractDurations()
	{
		DayTimeDuration v1 = DayTimeDuration.create("P3DT10H30M");
		DayTimeDuration v2 = DayTimeDuration.create("PT10H10S");
		DayTimeDuration v3 = v1.substract(v2);
		assertEquals(3, v3.getDays());
		assertEquals(0, v3.getHours());
		assertEquals(29, v3.getMinutes());
		assertEquals(50, v3.getSeconds());
	}
	
	@Test
	public void testNegate()
	{
		DayTimeDuration v1 = DayTimeDuration.create("P3DT10H30M");
		DayTimeDuration v2 = v1.negate();
		DayTimeDuration v3 = v1.add(v2);
		assertTrue(v3.isZero());
		assertEquals(0, v3.getDays());
		assertEquals(0, v3.getHours());
		assertEquals(0, v3.getMinutes());
		assertEquals(0, v3.getSeconds());
	}
}
