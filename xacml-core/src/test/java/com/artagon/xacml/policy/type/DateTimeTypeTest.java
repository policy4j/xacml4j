package com.artagon.xacml.policy.type;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.policy.type.DateTimeType.DateTimeValue;

public class DateTimeTypeTest 
{
	private DateTimeType t1;
	
	@Before
	public void init() throws Exception{
		this.t1 = XacmlDataType.DATETIME.getType();
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
		assertEquals(-360, value.getValue().getTimezone());
		assertEquals("2002-05-30T09:30:10-06:00", value.toXacmlString());
	}
	
	@Test
	public void testFromXacmlStringNoTimeZone(){
		DateTimeValue value = t1.fromXacmlString("2002-05-30T09:30:10");
		assertEquals(2002, value.getValue().getYear());
		assertEquals(5, value.getValue().getMonth());
		assertEquals(30, value.getValue().getDay());
		assertEquals(9, value.getValue().getHour());
		assertEquals(30, value.getValue().getMinute());
		assertEquals(10, value.getValue().getSecond());
		assertEquals(0, value.getValue().getTimezone());
		assertEquals("2002-05-30T09:30:10Z", value.toXacmlString());
		
	}	
	
	@Test(expected=IllegalArgumentException.class)
	public void testFromXacmlStringJustDate(){
		t1.fromXacmlString("2002-09-24Z");
	}
}
