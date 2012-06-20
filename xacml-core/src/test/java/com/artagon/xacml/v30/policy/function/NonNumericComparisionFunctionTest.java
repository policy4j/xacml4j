package com.artagon.xacml.v30.policy.function;

import static com.artagon.xacml.v30.types.BooleanType.BOOLEAN;
import static com.artagon.xacml.v30.types.StringType.STRING;
import static com.artagon.xacml.v30.types.TimeType.TIME;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.types.StringExp;
import com.artagon.xacml.v30.types.TimeExp;
import com.artagon.xacml.v30.types.TimeType;



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
		StringExp a = STRING.create("ab");
		StringExp b = STRING.create("aa");
		assertEquals(BOOLEAN.create(true), 
				NonNumericComparisionFunctions.greatherThan(a, b));
		a = STRING.create("aaa");
		b = STRING.create("aa");
		assertEquals(BOOLEAN.create(true), 
				NonNumericComparisionFunctions.greatherThan(a, b));
	}
	
	@Test
	public void testTimeLessThan()
	{
		TimeExp t1 = TIME.create("08:23:47-05:00");
		TimeExp t2 = TIME.create("08:23:48-05:00");
		assertEquals(BOOLEAN.create(true), NonNumericComparisionFunctions.lessThan(t1, t2));
		t2 = TIME.create("08:23:47-05:00");
		assertEquals(BOOLEAN.create(false), NonNumericComparisionFunctions.lessThan(t1, t2));
		t2 = TIME.create("08:23:46-05:00");
		assertEquals(BOOLEAN.create(false), NonNumericComparisionFunctions.lessThan(t1, t2));
	}
	
	@Test
	public void testTimeInRangeNoTimeZonesAndTimeIsInRange()
	{
		replay(context);
		TimeExp a = TIME.create("09:30:10");
		TimeExp b = TIME.create("08:30:10");
		TimeExp c = TIME.create("09:30:11");
		assertEquals(BOOLEAN.create(true), 
				NonNumericComparisionFunctions.timeInRange(context, a, b, c));
		verify(context);
	}
	
	@Test
	public void testTimeInRangeWithTimeZonesAndTimeIsInRange()
	{
		TimeExp a = TimeType.TIME.create("09:30:10Z");
		TimeExp b = TimeType.TIME.create("08:30:10Z");
		TimeExp c = TimeType.TIME.create("09:30:11Z");
		assertEquals(BOOLEAN.create(true), 
				NonNumericComparisionFunctions.timeInRange(context, a, b, c));
	}

	@Test
	public void testTimeInRangeNoTimeZonesAndTimeIsNotInRange()
	{
		replay(context);
		TimeExp a = TIME.create("09:30:10");
		TimeExp b = TIME.create("08:30:10");
		TimeExp c = TIME.create("09:30:09");
		assertEquals(BOOLEAN.create(false),
				NonNumericComparisionFunctions.timeInRange(context, a, b, c));
		verify(context);
	}

	@Test
	public void testTimeInRangeNoTimeZonesAndTimeIsEqualToUpperBound()
	{
		replay(context);
		TimeExp a = TIME.create("09:30:10");
		TimeExp b = TIME.create("08:30:10");
		TimeExp c = TIME.create("09:30:10");
		assertEquals(BOOLEAN.create(true),
				NonNumericComparisionFunctions.timeInRange(context, a, b, c));
		verify(context);
	}
}
