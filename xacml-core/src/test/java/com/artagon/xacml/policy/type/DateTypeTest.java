package com.artagon.xacml.policy.type;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.artagon.xacml.policy.type.DateType.DateValue;

public class DateTypeTest 
{
	private DateType t1;
	private DateType t2;
	
	@Before
	public void init() throws Exception{
		this.t1 = new DateTypeImpl();
		this.t2 = new DateTypeImpl();
	}
	
	@Test
	public void testFromXacmlString(){
		DateValue value = t1.fromXacmlString("2002-09-24Z");
		assertEquals(2002, value.getValue().getYear());
		assertEquals(9, value.getValue().getMonth());
		assertEquals(24, value.getValue().getDay());
		assertEquals("2002-09-24Z", value.toXacmlString());
	}
	
	@Test
	public void testEquals(){
		DateValue value1 = t1.fromXacmlString("2002-09-24Z");
		DateValue value2 = t2.fromXacmlString("2002-09-24Z");
		assertEquals(value1, value2);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testFromXacmlStringJustDate(){
		t1.fromXacmlString("2002-05-30T09:30:10-06:00");
	}
}

