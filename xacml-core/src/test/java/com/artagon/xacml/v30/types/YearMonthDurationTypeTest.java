package com.artagon.xacml.v30.types;

import org.junit.Test;

import com.artagon.xacml.v30.types.YearMonthDurationType;

public class YearMonthDurationTypeTest 
{
	@Test
	public void testFromXacmlString()
	{
		YearMonthDurationType.YEARMONTHDURATION.fromXacmlString("-P1Y2M");
	}
}
