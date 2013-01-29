package org.xacml4j.v30.types;

import static org.junit.Assert.assertEquals;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.YearMonthDuration;
import org.xacml4j.v30.types.YearMonthDurationExp;
import org.xacml4j.v30.types.YearMonthDurationType;


public class YearMonthDurationTypeTest 
{
	private DatatypeFactory df;
	
	@Before
	public void init() throws Exception{
		this.df = DatatypeFactory.newInstance();
	}
	
	@Test
	public void testFromXacmlString()
	{
		YearMonthDurationExp v1 = YearMonthDurationType.YEARMONTHDURATION.fromXacmlString("-P1Y2M");
		YearMonthDurationExp v2 = YearMonthDurationType.YEARMONTHDURATION.fromXacmlString("-P1Y2M");
		assertEquals("-P1Y2M", v1.toXacmlString());
		assertEquals(v1, v2);
	}
		
	@Test
	public void createFromJavaDuration()
	{
		Duration d = df.newDuration("-P1Y2M");
		YearMonthDurationExp v1 = YearMonthDurationType.YEARMONTHDURATION.create(d);
		YearMonthDurationExp v2 = YearMonthDurationType.YEARMONTHDURATION.fromXacmlString("-P1Y2M");
		assertEquals(v1, v2);
	}
	
	@Test
	public void createFromXacmlDuration()
	{
		YearMonthDuration d = new YearMonthDuration(df.newDuration("-P1Y2M"));
		YearMonthDurationExp v1 = YearMonthDurationType.YEARMONTHDURATION.create(d);
		YearMonthDurationExp v2 = YearMonthDurationType.YEARMONTHDURATION.fromXacmlString("-P1Y2M");
		assertEquals(v1, v2);
	}
}
