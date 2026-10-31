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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;


public class StringValTest
{
	@Test
	public void testEquals()
	{
		Value v0 = XacmlTypes.STRING.ofAny("v0");
		Value v1 = XacmlTypes.STRING.ofAny("v1");
		assertFalse(v0.equals(v1));
		Value v2 = XacmlTypes.STRING.ofAny("v0");
		assertFalse(v1.equals(v2));
		assertTrue(v0.equals(v2));
	}

	@Test
	public void longHashCollisionsDoNotMakeDifferentStringsEqual()
	{
		String left = "Aa".repeat(20);
		String right = "BB".repeat(20);
		assertEquals(left.hashCode(), right.hashCode());
		assertNotEquals(StringVal.of(left), StringVal.of(right));
		assertNotEquals(StringVal.of(right), StringVal.of(left));
	}

	@Test
	public void equalDistinctStringsAndUnicodeKeepTheirEquality()
	{
		for(String text : new String[]{"a".repeat(31), "a".repeat(32), "a".repeat(64),
				"\u00e9".repeat(40), "\u4e2d".repeat(40), "\ud83d\ude00".repeat(20)}){
			StringVal value = StringVal.of(text);
			StringVal equal = StringVal.of(new String(text.toCharArray()));
			assertEquals(value, value);
			assertEquals(value, equal);
			assertEquals(equal, value);
			assertEquals(value.hashCode(), equal.hashCode());
			assertNotEquals(value, StringVal.of("b" + text.substring(1)));
			assertNotEquals(value, StringVal.of(text.substring(0, text.length() - 1) + "b"));
			assertNotEquals(value, StringVal.of(text + "b"));
		}
	}

	@Test
	public void equalityWithOtherValueKeepsTheTypeCheck()
	{
		String text = "external string value with more than thirty-two characters";
		StringVal value = StringVal.of(text);
		Value<?> equal = new Value.OtherValue<String>(XacmlTypes.STRING, new String(text.toCharArray())) {};
		assertEquals(value, equal);
		assertEquals(equal, value);
		assertEquals(value.hashCode(), equal.hashCode());
		assertNotEquals(value, new Value.OtherValue<String>(XacmlTypes.ANYURI, text) {});
		assertNotEquals(value, text);
		assertNotEquals(value, null);
	}
}
