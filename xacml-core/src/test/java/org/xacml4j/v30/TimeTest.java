package org.xacml4j.v30;

/*
 * #%L
 * Artagon XACML 3.0 Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Artagon
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
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TimeTest
{
	@Test
	public void testParse()
	{
		Time t1 = Time.valueOf("08:23:47-05:00");
		assertEquals(8, t1.getHour());
		assertEquals(23, t1.getMinute());
		assertEquals(47, t1.getSecond());
		assertTrue(t1.isTimezoneSet());
		assertEquals(-5 * 60, t1.getTimezoneOffset());
		Time t2 = Time.valueOf("08:23:47");
		assertEquals(8, t2.getHour());
		assertEquals(23, t2.getMinute());
		assertEquals(47, t2.getSecond());
		assertTrue(t2.isTimezoneSet());
		assertEquals(0, t2.getTimezoneOffset());
	}
}
