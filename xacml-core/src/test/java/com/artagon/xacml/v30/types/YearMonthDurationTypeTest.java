package com.artagon.xacml.v30.types;

import org.junit.Test;

public class YearMonthDurationTypeTest 
{
	@Test
	public void testFromXacmlString()
	{
		YearMonthDurationType.YEARMONTHDURATION.fromXacmlString("-P1Y2M");
	}
}
