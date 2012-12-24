package org.xacml4j.v30.types;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.types.DateExp;
import org.xacml4j.v30.types.DateType;

public class DateTypeTest 
{
	private DateType t1;
	
	@Before
	public void init() throws Exception{
		this.t1 = DateType.DATE;
	}
	
	@Test
	public void testFromXacmlString(){
		DateExp value = t1.fromXacmlString("2002-09-24Z");
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
		DateExp d1 = t1.create(now);
		DateExp d2 = t1.create(now);
		System.out.println(d1.toXacmlString());
		assertEquals(d1, d2);
	}
}

