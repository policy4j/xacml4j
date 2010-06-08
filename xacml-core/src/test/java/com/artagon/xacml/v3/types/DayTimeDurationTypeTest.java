package com.artagon.xacml.v3.types;

import static org.junit.Assert.assertEquals;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.types.DayTimeDurationType.DayTimeDurationValue;

public class DayTimeDurationTypeTest 
{
	private DatatypeFactory f;
	private DayTimeDurationType t1;
	
	@Before
	public void init() throws Exception{
		this.t1 = XacmlDataTypes.DAYTIMEDURATION.getType();
		this.f = DatatypeFactory.newInstance();
	}
	
	@Test
	public void testFromXacmlString()
	{
		DayTimeDurationValue v1 = t1.fromXacmlString("P3DT10H30M");
		assertEquals(3, v1.getValue().getDays());
		assertEquals(10, v1.getValue().getHours());
		assertEquals(30, v1.getValue().getMinutes());
		assertEquals(00, v1.getValue().getSeconds());
		v1 = t1.fromXacmlString("P3DT10H30M10S");
		assertEquals(3, v1.getValue().getDays());
		assertEquals(10, v1.getValue().getHours());
		assertEquals(30, v1.getValue().getMinutes());
		assertEquals(10, v1.getValue().getSeconds());
	}
	
	@Test
	public void testToXacmlString()
	{
		Duration d = f.newDurationDayTime(true, 1, 2, 30, 10);
		DayTimeDurationValue v = t1.create(d);
		assertEquals("P1DT2H30M10S", v.toXacmlString());
	}
	
	@Test
	public void testEquals()
	{
		DayTimeDurationValue v1 = t1.create("P1DT2H30M10S");
		DayTimeDurationValue v2 = t1.create("P1DT2H30M10S");
		assertEquals(v1, v2);
	}
}
