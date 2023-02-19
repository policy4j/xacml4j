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
import org.xacml4j.v30.policy.function.impl.NonNumericComparisonFunctions;
import org.xacml4j.v30.types.StringValue;
import org.xacml4j.v30.types.TimeValue;
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
		StringValue a = XacmlTypes.STRING.of("ab");
		StringValue b = XacmlTypes.STRING.of("aa");
		assertEquals(XacmlTypes.BOOLEAN.of(true),
		             NonNumericComparisonFunctions.greaterThan(a, b));
		a = XacmlTypes.STRING.of("aaa");
		b = XacmlTypes.STRING.of("aa");
		assertEquals(XacmlTypes.BOOLEAN.of(true),
				NonNumericComparisonFunctions.greaterThan(a, b));
	}

	@Test
	public void testTimeLessThan()
	{
		TimeValue t1 = XacmlTypes.TIME.of("08:23:47-05:00");
		TimeValue t2 = XacmlTypes.TIME.of("08:23:48-05:00");
		assertEquals(XacmlTypes.BOOLEAN.of(true), NonNumericComparisonFunctions.lessThan(t1, t2));
		t2 = XacmlTypes.TIME.of("08:23:47-05:00");
		assertEquals(XacmlTypes.BOOLEAN.of(false), NonNumericComparisonFunctions.lessThan(t1, t2));
		t2 = XacmlTypes.TIME.of("08:23:46-05:00");
		assertEquals(XacmlTypes.BOOLEAN.of(false), NonNumericComparisonFunctions.lessThan(t1, t2));
	}

	@Test
	public void testTimeInRangeNoTimeZonesAndTimeIsInRange()
	{
		replay(context);
		TimeValue a = XacmlTypes.TIME.of("09:30:10");
		TimeValue b = XacmlTypes.TIME.of("08:30:10");
		TimeValue c = XacmlTypes.TIME.of("09:30:11");
		assertEquals(XacmlTypes.BOOLEAN.of(true),
				NonNumericComparisonFunctions.timeInRange(context, a, b, c));
		verify(context);
	}

	@Test
	public void testTimeInRangeWithTimeZonesAndTimeIsInRange()
	{
		TimeValue a = XacmlTypes.TIME.of("09:30:10Z");
		TimeValue b = XacmlTypes.TIME.of("08:30:10Z");
		TimeValue c = XacmlTypes.TIME.of("09:30:11Z");
		assertEquals(XacmlTypes.BOOLEAN.of(true),
				NonNumericComparisonFunctions.timeInRange(context, a, b, c));
	}

	@Test
	public void testTimeInRangeNoTimeZonesAndTimeIsNotInRange()
	{
		replay(context);
		TimeValue a = XacmlTypes.TIME.of("09:30:10");
		TimeValue b = XacmlTypes.TIME.of("08:30:10");
		TimeValue c = XacmlTypes.TIME.of("09:30:09");
		assertEquals(XacmlTypes.BOOLEAN.of(false),
				NonNumericComparisonFunctions.timeInRange(context, a, b, c));
		verify(context);
	}

	@Test
	public void testTimeInRangeNoTimeZonesAndTimeIsEqualToUpperBound()
	{
		replay(context);
		TimeValue a = XacmlTypes.TIME.of("09:30:10");
		TimeValue b = XacmlTypes.TIME.of("08:30:10");
		TimeValue c = XacmlTypes.TIME.of("09:30:10");
		assertEquals(XacmlTypes.BOOLEAN.of(true),
				NonNumericComparisonFunctions.timeInRange(context, a, b, c));
		verify(context);
	}
}
