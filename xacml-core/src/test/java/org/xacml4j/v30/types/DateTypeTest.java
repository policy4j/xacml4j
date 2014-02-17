package org.xacml4j.v30.types;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;

public class DateTypeTest
{
	private DateType t1;
	private Types types;
	@Before
	public void init() throws Exception{
		this.t1 = DateType.DATE;
		this.types = Types.builder().defaultTypes().create();
	}

	@Test
	public void testFromXacmlString(){
		DateExp value = t1.create("2002-09-24Z");
		assertEquals(2002, value.getValue().getYear());
		assertEquals(9, value.getValue().getMonth());
		assertEquals(24, value.getValue().getDay());
		TypeToString toString = types.getCapability(DateType.DATE, TypeToString.class);
		assertEquals("2002-09-24Z", toString.toString(value));
	}


	@Test(expected=IllegalArgumentException.class)
	public void testFromXacmlStringJustDate(){
		t1.create("2002-05-30T09:30:10-06:00");
	}

	@Test
	public void testCreateFromCalendar()
	{
		Calendar now = Calendar.getInstance();
		DateExp d1 = t1.create(now);
		DateExp d2 = t1.create(now);
		assertEquals(d1, d2);
	}
}

