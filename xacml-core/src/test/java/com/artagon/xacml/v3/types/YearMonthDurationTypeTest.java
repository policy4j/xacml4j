package com.artagon.xacml.v3.types;

import org.junit.Test;

public class YearMonthDurationTypeTest 
{
	@Test
	public void testFromXacmlString()
	{
		YearMonthDurationType.YEARMONTHDURATION.fromXacmlString("-P1Y2M");
	}
}
