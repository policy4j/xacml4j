package org.xacml4j.v30.types;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2019 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.policy.function.impl.NonNumericComparisonFunctions;

public class TemporalEqualityTest
{
	@Test
	public void dateEqualityAndFormattingPreserveTimezone()
	{
		ISO8601Date east = ISO8601Date.of("2002-10-10+14:00");
		ISO8601Date west = ISO8601Date.of("2002-10-10-12:00");
		ISO8601Date sameInstant = ISO8601Date.of("2002-10-09-10:00");

		assertNotEquals(east, west);
		assertTrue(east.compareTo(west) < 0);
		assertEquals(east, sameInstant);
		assertEquals(east.hashCode(), sameInstant.hashCode());
		assertEquals("2002-10-10+14:00", east.toXacmlString());
	}

	@Test
	public void dateTimeEqualityAndOrderingUseTheSameInstant()
	{
		ISO8601DateTime utc = ISO8601DateTime.of("2002-10-10T12:00:00Z");
		ISO8601DateTime offset = ISO8601DateTime.of("2002-10-10T13:00:00+01:00");

		assertEquals(utc, offset);
		assertEquals(utc.hashCode(), offset.hashCode());
		assertEquals(0, utc.compareTo(offset));
	}

	@Test
	public void unzonedTimeUsesUtcForEqualityButKeepsItsLexicalForm()
	{
		ISO8601Time unzoned = ISO8601Time.of("23:00:00");
		ISO8601Time utc = ISO8601Time.of("23:00:00Z");

		assertEquals(unzoned, utc);
		assertEquals(unzoned.hashCode(), utc.hashCode());
		assertEquals("23:00:00", unzoned.toXacmlString());
	}

	@Test
	public void orderingFunctionsUseUtcNormalizedTemporalValues()
	{
		assertTrue(NonNumericComparisonFunctions.lessThan(
				ISO8601Date.of("2002-10-10+14:00"), ISO8601Date.of("2002-10-10-12:00")).get());
		assertTrue(NonNumericComparisonFunctions.lessThan(
				ISO8601DateTime.of("2002-10-10T12:00:00Z"),
				ISO8601DateTime.of("2002-10-10T12:00:01Z")).get());
		assertTrue(NonNumericComparisonFunctions.lessThan(
				ISO8601Time.of("08:23:47-05:00"), ISO8601Time.of("13:23:48")).get());
	}
}
