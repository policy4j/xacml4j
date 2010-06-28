package com.artagon.xacml.v3.types;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.types.DateType.DateValue;

public class DateTypeTest 
{
	private DateType t1;
	
	@Before
	public void init() throws Exception{
		this.t1 = XacmlDataTypes.DATE.getType();
	}
	
	@Test
	public void testFromXacmlString(){
		DateValue value = t1.fromXacmlString("2002-09-24Z");
		assertEquals(2002, value.getValue().getYear());
		assertEquals(9, value.getValue().getMonth());
		assertEquals(24, value.getValue().getDay());
		assertEquals("2002-09-24Z", value.toXacmlString());
	}

	
	@Test(expected=IllegalArgumentException.class)
	public void testFromXacmlStringJustDate(){
		t1.fromXacmlString("2002-05-30T09:30:10-06:00");
	}
	
	@Test
	public void testCreateFromCalendar()
	{
		Calendar now = Calendar.getInstance();
		DateType.DateValue d1 = XacmlDataTypes.DATE.create(now);
		DateType.DateValue d2 = XacmlDataTypes.DATE.create(now);
		System.out.println(d1.toXacmlString());
		assertEquals(d1, d2);
	}
}

