package com.artagon.xacml.v3.policy.impl.function;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.type.DataTypes;
import com.artagon.xacml.v3.policy.type.StringType.StringValue;
import com.artagon.xacml.v3.policy.type.TimeType.TimeValue;



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
		StringValue a = DataTypes.STRING.create("ab");
		StringValue b = DataTypes.STRING.create("aa");
		assertEquals(DataTypes.BOOLEAN.create(true), 
				NonNumericComparisionFunctions.greatherThan(a, b));
		a = DataTypes.STRING.create("aaa");
		b = DataTypes.STRING.create("aa");
		assertEquals(DataTypes.BOOLEAN.create(true), 
				NonNumericComparisionFunctions.greatherThan(a, b));
	}
	
	@Test
	public void testTimeInRangeNoTimeZonesAndTimeIsInRange()
	{
		expect(context.getTimeZone()).andReturn(TimeZone.getDefault());
		replay(context);
		TimeValue a = DataTypes.TIME.create("09:30:10");
		TimeValue b = DataTypes.TIME.create("08:30:10");
		TimeValue c = DataTypes.TIME.create("09:30:11");
		assertEquals(DataTypes.BOOLEAN.create(true), 
				NonNumericComparisionFunctions.timeInRange(context, a, b, c));
		verify(context);
	}
	
	@Test
	public void testTimeInRangeWithTimeZonesAndTimeIsInRange()
	{
		TimeValue a = DataTypes.TIME.create("09:30:10Z");
		TimeValue b = DataTypes.TIME.create("08:30:10Z");
		TimeValue c = DataTypes.TIME.create("09:30:11Z");
		assertEquals(DataTypes.BOOLEAN.create(true), 
				NonNumericComparisionFunctions.timeInRange(context, a, b, c));
	}

	@Test
	public void testTimeInRangeNoTimeZonesAndTimeIsNotInRange()
	{
		expect(context.getTimeZone()).andReturn(TimeZone.getDefault());
		replay(context);
		TimeValue a = DataTypes.TIME.create("09:30:10");
		TimeValue b = DataTypes.TIME.create("08:30:10");
		TimeValue c = DataTypes.TIME.create("09:30:09");
		assertEquals(DataTypes.BOOLEAN.create(false),
				NonNumericComparisionFunctions.timeInRange(context, a, b, c));
		verify(context);
	}

	@Test
	public void testTimeInRangeNoTimeZonesAndTimeIsEqualToUpperBound()
	{
		expect(context.getTimeZone()).andReturn(TimeZone.getDefault());
		replay(context);
		TimeValue a = DataTypes.TIME.create("09:30:10");
		TimeValue b = DataTypes.TIME.create("08:30:10");
		TimeValue c = DataTypes.TIME.create("09:30:10");
		assertEquals(DataTypes.BOOLEAN.create(true),
				NonNumericComparisionFunctions.timeInRange(context, a, b, c));
		verify(context);
	}
}
