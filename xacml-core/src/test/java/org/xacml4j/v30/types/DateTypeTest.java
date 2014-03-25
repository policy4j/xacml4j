package org.xacml4j.v30.types;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;

import org.junit.Test;

public class DateTypeTest
{

	@Test
	public void testFromXacmlString(){
		DateExp value = DateExp.valueOf("2002-09-24Z");
		assertEquals(2002, value.getValue().getYear());
		assertEquals(9, value.getValue().getMonth());
		assertEquals(24, value.getValue().getDay());
		TypeToString toString = TypeToString.Types.getIndex().get(XacmlTypes.DATE).get();
		assertEquals("2002-09-24Z", toString.toString(value));
	}


	@Test(expected=IllegalArgumentException.class)
	public void testFromXacmlStringJustDate(){
		DateExp.valueOf("2002-05-30T09:30:10-06:00");
	}

	@Test
	public void testCreateFromCalendar()
	{
		Calendar now = Calendar.getInstance();
		DateExp d1 = DateExp.valueOf(now);
		DateExp d2 = DateExp.valueOf(now);
		System.out.println(d1.toStringExp());
		System.out.println(now);
		assertEquals(d1, d2);
		assertEquals(d1.getValue().getDay(), now.get(Calendar.DAY_OF_MONTH));
		assertEquals(d1.getValue().getYear(), now.get(Calendar.YEAR));
		assertEquals(d1.getValue().getMonth(), now.get(Calendar.MONTH) + 1);
	}
}

