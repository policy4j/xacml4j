package org.xacml4j.v30.types;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.assertEquals;

public class DateTypeTest
{

	@Test
	public void testFromXacmlString(){
		DateValue value = XacmlTypes.DATE.of("2002-09-24Z");
		assertEquals(2002, value.value().getYear());
		assertEquals(9, value.value().getMonth());
		assertEquals(24, value.value().getDay());
		TypeToString toString = TypeToString.Types.DATE;
		assertEquals("2002-09-24Z", toString.toString(value));
	}


	@Test(expected=IllegalArgumentException.class)
	public void testFromXacmlStringJustDate(){
		XacmlTypes.DATE.of("2002-05-30T09:30:10-06:00");
	}

	@Test
	public void testCreateFromCalendar()
	{
		Calendar now = Calendar.getInstance();
		DateValue d1 = XacmlTypes.DATE.of(now);
		DateValue d2 = XacmlTypes.DATE.of(now);
		System.out.println(d1.toStringExp());
		System.out.println(now);
		assertEquals(d1, d2);
		assertEquals(d1.value().getDay(), now.get(Calendar.DAY_OF_MONTH));
		assertEquals(d1.value().getYear(), now.get(Calendar.YEAR));
		assertEquals(d1.value().getMonth(), now.get(Calendar.MONTH) + 1);
	}
}

