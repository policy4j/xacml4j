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
import com.artagon.xacml.v30.types.StringValueExp;
import com.artagon.xacml.v30.types.TimeType;
import com.artagon.xacml.v30.types.TimeValueExp;



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
		StringValueExp a = STRING.create("ab");
		StringValueExp b = STRING.create("aa");
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
		TimeValueExp t1 = TIME.create("08:23:47-05:00");
		TimeValueExp t2 = TIME.create("08:23:48-05:00");
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
		TimeValueExp a = TIME.create("09:30:10");
		TimeValueExp b = TIME.create("08:30:10");
		TimeValueExp c = TIME.create("09:30:11");
		assertEquals(BOOLEAN.create(true), 
				NonNumericComparisionFunctions.timeInRange(context, a, b, c));
		verify(context);
	}
	
	@Test
	public void testTimeInRangeWithTimeZonesAndTimeIsInRange()
	{
		TimeValueExp a = TimeType.TIME.create("09:30:10Z");
		TimeValueExp b = TimeType.TIME.create("08:30:10Z");
		TimeValueExp c = TimeType.TIME.create("09:30:11Z");
		assertEquals(BOOLEAN.create(true), 
				NonNumericComparisionFunctions.timeInRange(context, a, b, c));
	}

	@Test
	public void testTimeInRangeNoTimeZonesAndTimeIsNotInRange()
	{
		replay(context);
		TimeValueExp a = TIME.create("09:30:10");
		TimeValueExp b = TIME.create("08:30:10");
		TimeValueExp c = TIME.create("09:30:09");
		assertEquals(BOOLEAN.create(false),
				NonNumericComparisionFunctions.timeInRange(context, a, b, c));
		verify(context);
	}

	@Test
	public void testTimeInRangeNoTimeZonesAndTimeIsEqualToUpperBound()
	{
		replay(context);
		TimeValueExp a = TIME.create("09:30:10");
		TimeValueExp b = TIME.create("08:30:10");
		TimeValueExp c = TIME.create("09:30:10");
		assertEquals(BOOLEAN.create(true),
				NonNumericComparisionFunctions.timeInRange(context, a, b, c));
		verify(context);
	}
}
