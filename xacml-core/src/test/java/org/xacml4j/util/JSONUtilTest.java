package org.xacml4j.util;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2023 Xacml4J.org
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

import static org.junit.Assert.*;
import static org.xacml4j.util.JSONUtil.*;

public class JSONUtilTest
{
	@Test
	public void testUnescape() throws Exception {

		String testShort = "Eating a piece of \u03c0 (pi)";
		assertEquals("Eating a piece of \\u03c0 (pi)", escape(testShort));

		String testLong = "I stole this guy from wikipedia: \ud83d\ude02"; // emoji "face with tears of joy"
		assertEquals("I stole this guy from wikipedia: \\ud83d\\ude02", escape(testLong));

		String testQuote = "here it comes \" to wreck the day...";
		assertEquals("here it comes \\\" to wreck the day...", escape(testQuote));
		String testNewline = "here it comes \n to wreck the day...";
		assertEquals("here it comes \\n to wreck the day...", escape(testNewline));
		String testBackslash = "here it comes \\ to wreck the day...";
		assertEquals("here it comes \\\\ to wreck the day...", escape(testBackslash));

		String testAsciiEscapes = "\\\r\n\t\b\f \\hmm\\ \f\b\n\r\\";
		assertEquals(testAsciiEscapes, unescape(escape(testAsciiEscapes)));
		assertEquals(testLong, unescape(escape(testLong)));
		assertEquals(testShort, unescape(escape(testShort)));

	}
}