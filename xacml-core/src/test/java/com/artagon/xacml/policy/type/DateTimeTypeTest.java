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
	
	@Test
	public void testEquals(){
		DateTimeValue value1 = t1.fromXacmlString("2002-05-30T09:30:10-06:00");
		DateTimeValue value2 = t2.fromXacmlString("2002-05-30T09:30:10-06:00");
		assertEquals(value1, value2);
	}
	
	
	
	@Test(expected=IllegalArgumentException.class)
	public void testFromXacmlStringJustDate(){
		t1.fromXacmlString("2002-09-24Z");
	}
}
