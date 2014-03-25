package org.xacml4j.v30.policy.function;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.types.BooleanExp;
import org.xacml4j.v30.types.StringExp;
import org.xacml4j.v30.types.TimeExp;




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
		StringExp a = StringExp.valueOf("ab");
		StringExp b = StringExp.valueOf("aa");
		assertEquals(BooleanExp.valueOf(true),
				NonNumericComparisionFunctions.greatherThan(a, b));
		a = StringExp.valueOf("aaa");
		b = StringExp.valueOf("aa");
		assertEquals(BooleanExp.valueOf(true),
				NonNumericComparisionFunctions.greatherThan(a, b));
	}

	@Test
	public void testTimeLessThan()
	{
		TimeExp t1 = TimeExp.valueOf("08:23:47-05:00");
		TimeExp t2 = TimeExp.valueOf("08:23:48-05:00");
		assertEquals(BooleanExp.valueOf(true), NonNumericComparisionFunctions.lessThan(t1, t2));
		t2 = TimeExp.valueOf("08:23:47-05:00");
		assertEquals(BooleanExp.valueOf(false), NonNumericComparisionFunctions.lessThan(t1, t2));
		t2 = TimeExp.valueOf("08:23:46-05:00");
		assertEquals(BooleanExp.valueOf(false), NonNumericComparisionFunctions.lessThan(t1, t2));
	}

	@Test
	public void testTimeInRangeNoTimeZonesAndTimeIsInRange()
	{
		replay(context);
		TimeExp a = TimeExp.valueOf("09:30:10");
		TimeExp b = TimeExp.valueOf("08:30:10");
		TimeExp c = TimeExp.valueOf("09:30:11");
		assertEquals(BooleanExp.valueOf(true),
				NonNumericComparisionFunctions.timeInRange(context, a, b, c));
		verify(context);
	}

	@Test
	public void testTimeInRangeWithTimeZonesAndTimeIsInRange()
	{
		TimeExp a = TimeExp.valueOf("09:30:10Z");
		TimeExp b = TimeExp.valueOf("08:30:10Z");
		TimeExp c = TimeExp.valueOf("09:30:11Z");
		assertEquals(BooleanExp.valueOf(true),
				NonNumericComparisionFunctions.timeInRange(context, a, b, c));
	}

	@Test
	public void testTimeInRangeNoTimeZonesAndTimeIsNotInRange()
	{
		replay(context);
		TimeExp a = TimeExp.valueOf("09:30:10");
		TimeExp b = TimeExp.valueOf("08:30:10");
		TimeExp c = TimeExp.valueOf("09:30:09");
		assertEquals(BooleanExp.valueOf(false),
				NonNumericComparisionFunctions.timeInRange(context, a, b, c));
		verify(context);
	}

	@Test
	public void testTimeInRangeNoTimeZonesAndTimeIsEqualToUpperBound()
	{
		replay(context);
		TimeExp a = TimeExp.valueOf("09:30:10");
		TimeExp b = TimeExp.valueOf("08:30:10");
		TimeExp c = TimeExp.valueOf("09:30:10");
		assertEquals(BooleanExp.valueOf(true),
				NonNumericComparisionFunctions.timeInRange(context, a, b, c));
		verify(context);
	}
}
