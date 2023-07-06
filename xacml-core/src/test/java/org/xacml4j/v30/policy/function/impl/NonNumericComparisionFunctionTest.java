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

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.types.StringVal;
import org.xacml4j.v30.types.ISO8601Time;
import org.xacml4j.v30.types.XacmlTypes;


public class NonNumericComparisionFunctionTest
{
	private EvaluationContext context;

	@Before
	public void init(){
		this.context = createStrictMock(EvaluationContext.class);
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
	public void testTimeInRangeNoTimeZonesAndTimeIsInRange()
	{
		replay(context);
		ISO8601Time a = XacmlTypes.TIME.ofAny("09:30:10");
		ISO8601Time b = XacmlTypes.TIME.ofAny("08:30:10");
		ISO8601Time c = XacmlTypes.TIME.ofAny("09:30:11");
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true),
				NonNumericComparisonFunctions.timeInRange(context, a, b, c));
		verify(context);
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
		replay(context);
		ISO8601Time a = XacmlTypes.TIME.ofAny("09:30:10");
		ISO8601Time b = XacmlTypes.TIME.ofAny("08:30:10");
		ISO8601Time c = XacmlTypes.TIME.ofAny("09:30:09");
		assertEquals(XacmlTypes.BOOLEAN.ofAny(false),
				NonNumericComparisonFunctions.timeInRange(context, a, b, c));
		verify(context);
	}

	@Test
	public void testTimeInRangeNoTimeZonesAndTimeIsEqualToUpperBound()
	{
		replay(context);
		ISO8601Time a = XacmlTypes.TIME.ofAny("09:30:10");
		ISO8601Time b = XacmlTypes.TIME.ofAny("08:30:10");
		ISO8601Time c = XacmlTypes.TIME.ofAny("09:30:10");
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true),
				NonNumericComparisonFunctions.timeInRange(context, a, b, c));
		verify(context);
	}
}
