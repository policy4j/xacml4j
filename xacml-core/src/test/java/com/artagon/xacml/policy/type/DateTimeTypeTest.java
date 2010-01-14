package com.artagon.xacml.policy.type;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.artagon.xacml.policy.type.DateTimeType.DateTimeValue;

public class DateTimeTypeTest 
{
	private DateTimeType t1;
	private DateTimeType t2;
	
	@Before
	public void init() throws Exception{
		this.t1 = new DateTimeTypeImpl();
		this.t2 = new DateTimeTypeImpl();
	}
	
	@Test
	public void testFromXacmlString(){
		DateTimeValue value = t1.fromXacmlString("2002-05-30T09:30:10-06:00");
		assertEquals(2002, value.getValue().getYear());
		assertEquals(5, value.getValue().getMonth());
		assertEquals(30, value.getValue().getDay());
		assertEquals(9, value.getValue().getHour());
		assertEquals(30, value.getValue().getMinute());
		assertEquals(10, value.getValue().getSecond());
	}
	
	@Test
	public void testFromXacmlStringJustDate(){
		DateTimeValue value = t1.fromXacmlString("2002-09-24Z");
	}
}
