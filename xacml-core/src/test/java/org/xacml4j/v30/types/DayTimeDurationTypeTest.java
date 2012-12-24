package org.xacml4j.v30.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.types.DayTimeDurationExp;
import org.xacml4j.v30.types.DayTimeDurationType;


public class DayTimeDurationTypeTest 
{
	private DatatypeFactory f;
	private DayTimeDurationType t1;
	
	@Before
	public void init() throws Exception{
		this.t1 = DayTimeDurationType.DAYTIMEDURATION;
		this.f = DatatypeFactory.newInstance();
	}
	
	@Test
	public void testFromXacmlString()
	{
		DayTimeDurationExp v1 = t1.fromXacmlString("P3DT10H30M");
		assertEquals(3, v1.getValue().getDays());
		assertEquals(10, v1.getValue().getHours());
		assertEquals(30, v1.getValue().getMinutes());
		assertEquals(00, v1.getValue().getSeconds());
		assertEquals("P3DT10H30M", v1.toXacmlString());
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
		DayTimeDurationExp v = t1.create(d);
		assertEquals("P1DT2H30M10S", v.toXacmlString());
	}
	
	@Test
	public void testCompareTo()
	{
		Duration a = f.newDurationDayTime(true, 1, 2, 30, 10);
		Duration b = f.newDurationDayTime(true, 1, 2, 30, 11);
		Duration c = f.newDurationDayTime(true, 1, 2, 30, 9);
		
		DayTimeDurationExp ad = t1.create(a);
		DayTimeDurationExp bd = t1.create(b);
		DayTimeDurationExp cd = t1.create(c);
		DayTimeDurationExp dd = t1.create(a);
		assertTrue(ad.compareTo(bd) < 0);
		assertTrue(bd.compareTo(cd) > 0);
		assertTrue(ad.compareTo(dd) == 0);
	}
	
	@Test
	public void testEquals()
	{
		DayTimeDurationExp v1 = t1.create("P1DT2H30M10S");
		DayTimeDurationExp v2 = t1.create("P1DT2H30M10S");
		assertEquals(v1, v2);
	}
}
