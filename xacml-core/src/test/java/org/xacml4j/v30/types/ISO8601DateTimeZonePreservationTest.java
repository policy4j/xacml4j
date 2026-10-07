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

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

import javax.xml.datatype.DatatypeFactory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ISO8601DateTimeZonePreservationTest
{
	static Stream<Arguments> factoriesAndZones()
	{
		return Stream.of("string", "string-value", "xml-calendar", "type-xml")
				.flatMap(factory -> Stream.of("", "Z", "-05:00", "+05:30").map(zone -> Arguments.of(factory, zone)));
	}

	@ParameterizedTest(name = "{0} retains timezone [{1}]")
	@MethodSource("factoriesAndZones")
	public void factoriesAndArithmeticKeepTheTimezoneOrItsAbsence(String factory, String zone) throws Exception
	{
		String input = "2002-03-22T08:00:00" + zone;
		ISO8601DateTime value = switch (factory) {
			case "string" -> ISO8601DateTime.of(input);
			case "string-value" -> ISO8601DateTime.of(StringVal.of(input));
			case "xml-calendar" -> ISO8601DateTime.of(DatatypeFactory.newInstance().newXMLGregorianCalendar(input));
			case "type-xml" -> XacmlTypes.DATETIME.ofAny(DatatypeFactory.newInstance().newXMLGregorianCalendar(input));
			default -> throw new IllegalArgumentException(factory);
		};
		assertEquals(input, value.toXacmlString());
		assertEquals(input, value.toStringExp().get());
		assertEquals(input, value.toStringExp(DateTimeFormatter.ISO_DATE_TIME).get());
		assertEquals("2002-03-22T09:00:00" + zone, value.plus(ISO8601DayTimeDuration.of("PT1H")).toXacmlString());
		assertEquals("2002-03-22T07:00:00" + zone, value.minus(ISO8601DayTimeDuration.of("PT1H")).toXacmlString());
		assertEquals("2002-04-22T08:00:00" + zone, value.plus(ISO8601YearMonthDuration.of("P1M")).toXacmlString());
		assertEquals("2002-02-22T08:00:00" + zone, value.minus(ISO8601YearMonthDuration.of("P1M")).toXacmlString());
	}

	static Stream<Arguments> signedDurationsAndZones()
	{
		return Stream.of("", "Z", "-05:00", "+05:30").flatMap(zone -> Stream.of(
				Arguments.of(zone, "PT1H", "09:00:00", "07:00:00"),
				Arguments.of(zone, "-PT1H", "07:00:00", "09:00:00")));
	}

	@ParameterizedTest
	@MethodSource("signedDurationsAndZones")
	public void javaDurationOverloadsAlsoKeepTheTimezone(String zone, String duration, String add, String subtract)
	{
		ISO8601DateTime start = ISO8601DateTime.of("2002-03-22T08:00:00" + zone);
		assertEquals("2002-03-22T" + add + zone, start.plus(Duration.parse(duration)).toXacmlString());
		assertEquals("2002-03-22T" + subtract + zone, start.minus(Duration.parse(duration)).toXacmlString());
	}

	@Test
	public void aNegativeYearDoesNotBecomeATimezone()
	{
		assertEquals("-0001-03-22T09:00:00", ISO8601DateTime.of("-0001-03-22T08:00:00")
				.plus(ISO8601DayTimeDuration.of("PT1H")).toXacmlString());
	}

	@Test
	public void undefinedTimezoneKeepsTheExistingUtcComparisonDefault()
	{
		ISO8601DateTime local = ISO8601DateTime.of("2002-03-22T08:00:00");
		ISO8601DateTime utc = ISO8601DateTime.of("2002-03-22T08:00:00Z");
		assertEquals(ZoneOffset.UTC, local.getTimeZoneOffset());
		assertEquals(local, utc);
		assertEquals(utc, local);
		assertEquals(local.hashCode(), utc.hashCode());
		assertEquals(0, local.compareTo(utc));
	}

	@Test
	public void explicitlyZonedJavaValuesKeepTheirZone()
	{
		OffsetDateTime input = OffsetDateTime.parse("2002-03-22T08:00:00Z");
		assertEquals("2002-03-22T08:00:00Z", ISO8601DateTime.of(input).toXacmlString());
		assertEquals("2002-03-22T08:00:00Z", ISO8601DateTime.of(input.toZonedDateTime()).toXacmlString());
		assertEquals("2002-03-22T08:00:00Z", ISO8601DateTime.of(input.toInstant()).toXacmlString());
	}
}
