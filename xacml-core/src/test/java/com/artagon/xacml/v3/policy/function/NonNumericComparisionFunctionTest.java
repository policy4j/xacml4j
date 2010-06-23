package com.artagon.xacml.v3.policy.function;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.artagon.xacml.v3.types.StringType.StringValue;
import com.artagon.xacml.v3.types.TimeType.TimeValue;



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
		StringValue a = XacmlDataTypes.STRING.create("ab");
		StringValue b = XacmlDataTypes.STRING.create("aa");
		assertEquals(XacmlDataTypes.BOOLEAN.create(true), 
				NonNumericComparisionFunctions.greatherThan(a, b));
		a = XacmlDataTypes.STRING.create("aaa");
		b = XacmlDataTypes.STRING.create("aa");
		assertEquals(XacmlDataTypes.BOOLEAN.create(true), 
				NonNumericComparisionFunctions.greatherThan(a, b));
	}
	
	@Test
	public void testTimeLessThan()
	{
		TimeValue t1 = XacmlDataTypes.TIME.create("08:23:47-05:00");
		TimeValue t2 = XacmlDataTypes.TIME.create("08:23:48-05:00");
		assertEquals(XacmlDataTypes.BOOLEAN.create(true), NonNumericComparisionFunctions.lessThan(t1, t2));
		t2 = XacmlDataTypes.TIME.create("08:23:47-05:00");
		assertEquals(XacmlDataTypes.BOOLEAN.create(false), NonNumericComparisionFunctions.lessThan(t1, t2));
		t2 = XacmlDataTypes.TIME.create("08:23:46-05:00");
		assertEquals(XacmlDataTypes.BOOLEAN.create(false), NonNumericComparisionFunctions.lessThan(t1, t2));
	}
	
	@Test
	public void testTimeInRangeNoTimeZonesAndTimeIsInRange()
	{
		expect(context.getTimeZone()).andReturn(TimeZone.getDefault());
		replay(context);
		TimeValue a = XacmlDataTypes.TIME.create("09:30:10");
		TimeValue b = XacmlDataTypes.TIME.create("08:30:10");
		TimeValue c = XacmlDataTypes.TIME.create("09:30:11");
		assertEquals(XacmlDataTypes.BOOLEAN.create(true), 
				NonNumericComparisionFunctions.timeInRange(context, a, b, c));
		verify(context);
	}
	
	@Test
	public void testTimeInRangeWithTimeZonesAndTimeIsInRange()
	{
		TimeValue a = XacmlDataTypes.TIME.create("09:30:10Z");
		TimeValue b = XacmlDataTypes.TIME.create("08:30:10Z");
		TimeValue c = XacmlDataTypes.TIME.create("09:30:11Z");
		assertEquals(XacmlDataTypes.BOOLEAN.create(true), 
				NonNumericComparisionFunctions.timeInRange(context, a, b, c));
	}

	@Test
	public void testTimeInRangeNoTimeZonesAndTimeIsNotInRange()
	{
		expect(context.getTimeZone()).andReturn(TimeZone.getDefault());
		replay(context);
		TimeValue a = XacmlDataTypes.TIME.create("09:30:10");
		TimeValue b = XacmlDataTypes.TIME.create("08:30:10");
		TimeValue c = XacmlDataTypes.TIME.create("09:30:09");
		assertEquals(XacmlDataTypes.BOOLEAN.create(false),
				NonNumericComparisionFunctions.timeInRange(context, a, b, c));
		verify(context);
	}

	@Test
	public void testTimeInRangeNoTimeZonesAndTimeIsEqualToUpperBound()
	{
		expect(context.getTimeZone()).andReturn(TimeZone.getDefault());
		replay(context);
		TimeValue a = XacmlDataTypes.TIME.create("09:30:10");
		TimeValue b = XacmlDataTypes.TIME.create("08:30:10");
		TimeValue c = XacmlDataTypes.TIME.create("09:30:10");
		assertEquals(XacmlDataTypes.BOOLEAN.create(true),
				NonNumericComparisionFunctions.timeInRange(context, a, b, c));
		verify(context);
	}
}
