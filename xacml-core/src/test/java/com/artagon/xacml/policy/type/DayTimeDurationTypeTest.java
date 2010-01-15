package com.artagon.xacml.policy.type;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import com.artagon.xacml.policy.type.DayTimeDurationType.DayTimeDurationValue;

public class DayTimeDurationTypeTest 
{
	private DayTimeDurationType t1;
	private DayTimeDurationType t2;
	
	@Before
	public void init() throws Exception{
		this.t1 = new DayTimeDurationTypeImpl();
		this.t2 = new DayTimeDurationTypeImpl();
	}
	
	@Test
	public void testFromXacmlString()
	{
		DayTimeDurationValue v1 = t1.fromXacmlString("P3DT10H30M");
		DayTimeDurationValue v2 = t2.fromXacmlString("P3DT10H30M");
		assertEquals(v1, v2);
		assertEquals("P3DT10H30M", v1.toXacmlString());
	}
}
