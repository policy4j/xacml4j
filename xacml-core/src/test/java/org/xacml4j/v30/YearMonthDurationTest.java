package org.xacml4j.v30;

import static org.junit.Assert.assertEquals;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.YearMonthDuration;

public class YearMonthDurationTest 
{
	private DatatypeFactory df;
	
	@Before
	public void init() throws Exception{
		this.df = DatatypeFactory.newInstance();
	}
	
	@Test
	public void testFromXacmlString()
	{
		YearMonthDuration v1 = YearMonthDuration.create("-P1Y2M");
		YearMonthDuration v2 = YearMonthDuration.create("-P1Y2M");
		assertEquals("-P1Y2M", v1.toString());
		assertEquals(v1, v2);
		assertEquals(1, v1.getYears());
		assertEquals(2, v1.getMonths());
	}
		
	@Test
	public void createFromJavaDuration()
	{
		Duration d = df.newDuration("-P1Y2M");
		YearMonthDuration v1 = YearMonthDuration.create(d);
		YearMonthDuration v2 = YearMonthDuration.create("-P1Y2M");
		assertEquals(v1, v2);
		assertEquals(1, v1.getYears());
		assertEquals(2, v1.getMonths());
	}
}

