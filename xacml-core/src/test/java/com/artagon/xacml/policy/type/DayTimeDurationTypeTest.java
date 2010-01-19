package com.artagon.xacml.policy.type;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.policy.type.DayTimeDurationType.DayTimeDurationValue;

public class DayTimeDurationTypeTest 
{
	private DayTimeDurationType t1;
	
	@Before
	public void init() throws Exception{
		this.t1 = new DayTimeDurationTypeImpl();
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
}
