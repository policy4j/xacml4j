package org.xacml4j.v30.policy.function.impl;

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

import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.TimeZone;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.policy.function.FunctionProviderBuilder;
import org.xacml4j.v30.types.BooleanVal;
import org.xacml4j.v30.types.StringVal;
import org.xacml4j.v30.types.ISO8601Time;
import org.xacml4j.v30.types.Value;
import org.xacml4j.v30.types.XacmlTypes;


@ExtendWith(MockitoExtension.class)
public class NonNumericComparisionFunctionTest
{
	private EvaluationContext context;
	private FunctionProvider functions;

	@BeforeEach
	public void init(){
		this.context = mock(EvaluationContext.class);
		lenient().when(context.getTimeZone()).thenReturn(TimeZone.getTimeZone("UTC"));
		lenient().when(context.getClock()).thenReturn(Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
		this.functions = FunctionProviderBuilder.builder().build();
	}

	@Test
	public void testStringGreaterThan()
	{
		StringVal a = XacmlTypes.STRING.ofAny("ab");
		StringVal b = XacmlTypes.STRING.ofAny("aa");
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true),
		             NonNumericComparisonFunctions.greaterThan(a, b));
		a = XacmlTypes.STRING.ofAny("aaa");
		b = XacmlTypes.STRING.ofAny("aa");
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true),
				NonNumericComparisonFunctions.greaterThan(a, b));
	}

	@Test
	public void testTimeLessThan()
	{
		ISO8601Time t1 = XacmlTypes.TIME.ofAny("08:23:47-05:00");
		ISO8601Time t2 = XacmlTypes.TIME.ofAny("08:23:48-05:00");
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true), NonNumericComparisonFunctions.lessThan(t1, t2));
		t2 = XacmlTypes.TIME.ofAny("08:23:47-05:00");
		assertEquals(XacmlTypes.BOOLEAN.ofAny(false), NonNumericComparisonFunctions.lessThan(t1, t2));
		t2 = XacmlTypes.TIME.ofAny("08:23:46-05:00");
		assertEquals(XacmlTypes.BOOLEAN.ofAny(false), NonNumericComparisonFunctions.lessThan(t1, t2));
	}

	@Test
	public void testTimeLessThanKeepsReferenceDayAcrossOffsets()
	{
		ISO8601Time earlier = XacmlTypes.TIME.ofAny("08:00:00+09:00");
		ISO8601Time later = XacmlTypes.TIME.ofAny("17:00:00-06:00");
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true),
				NonNumericComparisonFunctions.lessThan(earlier, later));
	}

	@Test
	public void testTimeInRangeNoTimeZonesAndTimeIsInRange()
	{
		ISO8601Time a = XacmlTypes.TIME.ofAny("09:30:10");
		ISO8601Time b = XacmlTypes.TIME.ofAny("08:30:10");
		ISO8601Time c = XacmlTypes.TIME.ofAny("09:30:11");
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true),
				NonNumericComparisonFunctions.timeInRange(context, a, b, c));
		verify(context).getTimeZone();
		verify(context).getClock();
		verifyNoMoreInteractions(context);
	}

	@Test
	public void testTimeInRangeWithTimeZonesAndTimeIsInRange()
	{
		ISO8601Time a = XacmlTypes.TIME.ofAny("09:30:10Z");
		ISO8601Time b = XacmlTypes.TIME.ofAny("08:30:10Z");
		ISO8601Time c = XacmlTypes.TIME.ofAny("09:30:11Z");
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true),
				NonNumericComparisonFunctions.timeInRange(context, a, b, c));
	}

	@Test
	public void testTimeInRangeNoTimeZonesAndTimeIsNotInRange()
	{
		ISO8601Time a = XacmlTypes.TIME.ofAny("09:30:10");
		ISO8601Time b = XacmlTypes.TIME.ofAny("08:30:10");
		ISO8601Time c = XacmlTypes.TIME.ofAny("09:30:09");
		assertEquals(XacmlTypes.BOOLEAN.ofAny(false),
				NonNumericComparisonFunctions.timeInRange(context, a, b, c));
		verify(context).getTimeZone();
		verify(context).getClock();
		verifyNoMoreInteractions(context);
	}

	@Test
	public void testTimeInRangeNoTimeZonesAndTimeIsEqualToUpperBound()
	{
		ISO8601Time a = XacmlTypes.TIME.ofAny("09:30:10");
		ISO8601Time b = XacmlTypes.TIME.ofAny("08:30:10");
		ISO8601Time c = XacmlTypes.TIME.ofAny("09:30:10");
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true),
				NonNumericComparisonFunctions.timeInRange(context, a, b, c));
		verify(context).getTimeZone();
		verify(context).getClock();
		verifyNoMoreInteractions(context);
	}

	@Test
	public void testCanonicalTimeInRangeFunctionIdentifierIsRegistered() throws Exception
	{
		FunctionSpec canonical = functions.getFunction("urn:oasis:names:tc:xacml:2.0:function:time-in-range").orElseThrow();
		assertTrue(functions.getFunction("urn:oasis:names:tc:xacml:1.0:function:time-in-range").isPresent());
		assertRange(canonical, "09:00:00", "08:30:00", "10:00:00", true);
	}

	@Test
	public void testTimeInRangeSpecCases() throws Exception
	{
		FunctionSpec legacy = functions.getFunction("urn:oasis:names:tc:xacml:1.0:function:time-in-range").orElseThrow();

		assertRange(legacy, "08:30:00", "08:30:00", "10:00:00", true);
		assertRange(legacy, "09:00:00", "08:30:00", "10:00:00", true);
		assertRange(legacy, "10:00:00", "08:30:00", "10:00:00", true);
		assertRange(legacy, "08:29:59", "08:30:00", "10:00:00", false);
		assertRange(legacy, "10:00:01", "08:30:00", "10:00:00", false);
		assertRange(legacy, "00:30:00", "23:00:00", "02:00:00", true);
		assertRange(legacy, "12:00:00", "23:00:00", "02:00:00", false);
		assertRange(legacy, "10:00:00+02:00", "08:00:00Z", "09:00:00Z", true);
	}

	@Test
	public void testTimeInRangeKeepsReferenceDayAcrossOffsets() throws Exception
	{
		FunctionSpec legacy = functions.getFunction("urn:oasis:names:tc:xacml:1.0:function:time-in-range").orElseThrow();
		// These UTC instants straddle the reference-day boundary; folding to time-of-day falsely includes midnight.
		assertRange(legacy, "12:00:00Z", "08:00:00+09:00", "17:00:00-06:00", true);
	}

	@Test
	public void testTimeInRangeWithPositiveOffsetAcrossReferenceDayBoundary() throws Exception
	{
		FunctionSpec legacy = functions.getFunction("urn:oasis:names:tc:xacml:1.0:function:time-in-range").orElseThrow();
		assertRange(legacy, "08:00:00+09:00", "07:00:00+09:00", "09:00:00+09:00", true);
		assertRange(legacy, "07:00:00+09:00", "07:00:00+09:00", "09:00:00+09:00", true);
		assertRange(legacy, "09:00:00+09:00", "07:00:00+09:00", "09:00:00+09:00", true);
		assertRange(legacy, "06:59:59+09:00", "07:00:00+09:00", "09:00:00+09:00", false);
	}

	@Test
	public void testTimeInRangeCrossingMidnightWithPositiveAndNegativeOffsets() throws Exception
	{
		FunctionSpec legacy = functions.getFunction("urn:oasis:names:tc:xacml:1.0:function:time-in-range").orElseThrow();
		assertRange(legacy, "00:30:00+02:00", "23:00:00+02:00", "01:00:00+02:00", true);
		assertRange(legacy, "23:30:00-02:00", "22:00:00-02:00", "01:00:00-02:00", true);
	}

	@Test
	public void testTimeInRangeUsesContextZoneForUnzonedValues() throws Exception
	{
		when(context.getTimeZone()).thenReturn(TimeZone.getTimeZone("GMT-08:00"));
		FunctionSpec legacy = functions.getFunction("urn:oasis:names:tc:xacml:1.0:function:time-in-range").orElseThrow();
		ISO8601Time time = ISO8601Time.of("23:00:00");
		ISO8601Time restored = ISO8601Time.of(time.toXacmlString());
		assertAll(
				() -> assertEquals("23:00:00", time.toXacmlString()),
				() -> assertFalse(restored.isTimeZoneDefined()),
				() -> assertEquals(XacmlTypes.BOOLEAN.ofAny(true),
						legacy.<Value>invoke(context, restored, XacmlTypes.TIME.ofAny("06:00:00Z"),
								XacmlTypes.TIME.ofAny("08:00:00Z"))));
		assertRange(legacy, "00:30:00", "23:00:00", "02:00:00", true);
	}

	@Test
	public void testZonedTimeSuppliesZoneForUnzonedTimeInRangeBounds() throws Exception
	{
		FunctionSpec legacy = functions.getFunction("urn:oasis:names:tc:xacml:2.0:function:time-in-range").orElseThrow();
		assertRange(legacy, "01:30:00-08:00", "01:00:00", "02:00:00", true);
	}

	private void assertRange(FunctionSpec function, String time, String start, String end, boolean expected) throws Exception
	{
		assertEquals(XacmlTypes.BOOLEAN.ofAny(expected), function.<Value>invoke(context,
				XacmlTypes.TIME.ofAny(time), XacmlTypes.TIME.ofAny(start), XacmlTypes.TIME.ofAny(end)));
	}
}
