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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.policy.function.impl.NonNumericComparisonFunctions;

public class TemporalEqualityTest
{
	@Test
	public void dateAndDateTimeKeepInstantHashAndOrdering()
	{
		List<OffsetDateTime> inputs = List.of(
				OffsetDateTime.of(LocalDate.MIN, LocalTime.MIDNIGHT, ZoneOffset.MAX),
				OffsetDateTime.parse("1969-12-31T23:59:59.000000001Z"),
				OffsetDateTime.parse("1970-01-01T00:00:00Z"),
				OffsetDateTime.parse("1970-01-01T00:00:00.999999999Z"),
				OffsetDateTime.parse("2002-10-10T00:00:00+14:00"),
				OffsetDateTime.parse("2002-10-09T00:00:00-10:00"),
				OffsetDateTime.parse("2002-10-10T01:00:00+01:00"),
				OffsetDateTime.parse("2002-10-10T00:00:00Z"),
				OffsetDateTime.of(LocalDate.MAX, LocalTime.MAX, ZoneOffset.MIN));
		for (OffsetDateTime input : inputs) {
			ISO8601Date date = ISO8601Date.of(input);
			Instant midnight = input.toLocalDate().atStartOfDay().toInstant(input.getOffset());
			ISO8601DateTime dateTime = ISO8601DateTime.of(input);
			Instant instant = input.toInstant();
			assertEquals(Objects.hash(midnight, date.getEvaluatesTo()), date.hashCode());
			assertEquals(Objects.hash(instant, dateTime.getEvaluatesTo()), dateTime.hashCode());
			for (OffsetDateTime other : inputs) {
				Instant otherMidnight = other.toLocalDate().atStartOfDay().toInstant(other.getOffset());
				ISO8601Date otherDate = ISO8601Date.of(other);
				ISO8601DateTime otherDateTime = ISO8601DateTime.of(other);
				assertEquals(midnight.equals(otherMidnight), date.equals(otherDate));
				assertEquals(midnight.compareTo(otherMidnight), date.compareTo(otherDate));
				assertEquals(instant.equals(other.toInstant()), dateTime.equals(otherDateTime));
				assertEquals(instant.compareTo(other.toInstant()), dateTime.compareTo(otherDateTime));
			}
		}
	}

	@Test
	public void timeKeepsBoxedNanosHash()
	{
		for (String input : List.of("00:00:00", "00:00:00Z", "00:00:00+14:00",
				"12:34:56.987654321-12:00", "23:59:59.999999999Z")) {
			ISO8601Time time = ISO8601Time.of(input);
			assertEquals(Objects.hash(time.toUtcReferenceNanos(ZoneOffset.UTC), time.getEvaluatesTo()),
					time.hashCode());
		}
	}

	@Test
	public void largeDateBagGroupsEqualInstantsInFirstOccurrenceOrder()
	{
		ISO8601Date first = ISO8601Date.of("2002-10-10+14:00");
		ISO8601Date equivalent = ISO8601Date.of("2002-10-09-10:00");
		BagOfValues.Builder builder = XacmlTypes.DATE.bagBuilder().attribute(first);
		List<ISO8601Date> expected = new ArrayList<>(List.of(first, equivalent));
		for (int day = 1; day <= 10; day++) {
			ISO8601Date date = ISO8601Date.of(LocalDate.of(2003, 1, day));
			builder.attribute(date);
			expected.add(date);
		}
		BagOfValues bag = builder.attribute(equivalent).build();
		assertEquals(expected, bag.stream().toList());
		assertEquals(2L, bag.stream().filter(equivalent::equals).count());
	}

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
