package org.xacml4j.v30.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.junit.Before;
import org.junit.Test;


public class DayTimeDurationTypeTest
{
	private DatatypeFactory f;
	private DayTimeDurationType t1;
	private Types types;
	
	@Before
	public void init() throws Exception{
		this.t1 = DayTimeDurationType.DAYTIMEDURATION;
		this.f = DatatypeFactory.newInstance();
		this.types = Types.builder().defaultTypes().create();
	}

	@Test
	public void testFromXacmlString()
	{
		DayTimeDurationExp v1 = t1.create("P3DT10H30M");
		assertEquals(3, v1.getValue().getDays());
		assertEquals(10, v1.getValue().getHours());
		assertEquals(30, v1.getValue().getMinutes());
		assertEquals(0, v1.getValue().getSeconds());
		TypeToString toString = types.getCapability(t1, TypeToString.class);
		assertNotNull(toString);
		assertEquals("P3DT10H30M", toString.toString(v1));
		v1 = (DayTimeDurationExp)toString.fromString("P3DT10H30M10S");
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
		TypeToString toString = types.getCapability(t1, TypeToString.class);
		assertEquals("P1DT2H30M10S", toString.toString(v));
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
