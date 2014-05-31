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

import static org.junit.Assert.assertEquals;

import java.util.Calendar;

import org.junit.Test;

public class TimeTypeTest
{
	@Test
	public void testCompare()
	{
		TimeExp t1 = TimeExp.valueOf("08:23:47-05:00");
		TimeExp t2 = TimeExp.valueOf("08:23:48-05:00");
		assertEquals(-1, t1.compareTo(t2));
		t2 = TimeExp.valueOf("08:23:47-05:00");
		assertEquals(0, t1.compareTo(t2));
		t2 = TimeExp.valueOf("08:23:46-05:00");
		assertEquals(1, t1.compareTo(t2));
	}

	@Test
	public void testCreateFromCalendar()
	{
		Calendar now = Calendar.getInstance();
		TimeExp t1 = TimeExp.valueOf(now);
		TimeExp t2 = TimeExp.valueOf(now);
		assertEquals(t1, t2);
	}
}
