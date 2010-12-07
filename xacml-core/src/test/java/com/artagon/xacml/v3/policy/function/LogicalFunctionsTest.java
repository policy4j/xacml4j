package com.artagon.xacml.v3.policy.function;


import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.policy.FunctionSpec;
import com.artagon.xacml.v3.spi.FunctionProvider;
import com.artagon.xacml.v3.spi.function.AnnotiationBasedFunctionProvider;
import com.artagon.xacml.v3.types.BooleanType;
import com.artagon.xacml.v3.types.IntegerType;
import com.artagon.xacml.v3.types.IntegerValue;

public class LogicalFunctionsTest
{
	private FunctionProvider f;
	
	FunctionSpec andFunc;
	FunctionSpec orFunc;
	FunctionSpec notFunc;
	
	private EvaluationContext context;
	
	@Before
	public void init() throws Exception
	{
		this.f = new AnnotiationBasedFunctionProvider(LogicalFunctions.class);
		this.andFunc = f.getFunction("urn:oasis:names:tc:xacml:1.0:function:and");
		this.orFunc = f.getFunction("urn:oasis:names:tc:xacml:1.0:function:or");
		this.notFunc = f.getFunction("urn:oasis:names:tc:xacml:1.0:function:not");
		this.context = createStrictMock(EvaluationContext.class);
	}
	
	
	@Test
	public void testAndFunction() throws EvaluationException
	{
		replay(context);
		assertEquals(BooleanType.BOOLEAN.create(false), 
				LogicalFunctions.and(context, BooleanType.BOOLEAN.create(false), BooleanType.BOOLEAN.create(false)));
		verify(context);
		reset(context);
		replay(context);
		assertEquals(BooleanType.BOOLEAN.create(false), 
				LogicalFunctions.and(context, BooleanType.BOOLEAN.create(true), BooleanType.BOOLEAN.create(false)));
		verify(context);
		reset(context);
		replay(context);
		assertEquals(BooleanType.BOOLEAN.create(true), 
				LogicalFunctions.and(context, BooleanType.BOOLEAN.create(true), BooleanType.BOOLEAN.create(true)));
		verify(context);
	}
	
	@Test
	public void testBasicOrFunction() throws EvaluationException
	{
		replay(context);
		assertEquals(BooleanType.BOOLEAN.create(false), 
				LogicalFunctions.or(context, BooleanType.BOOLEAN.create(false), BooleanType.BOOLEAN.create(false)));
		verify(context);
		reset(context);
		replay(context);
		assertEquals(BooleanType.BOOLEAN.create(true), 
				LogicalFunctions.or(context, BooleanType.BOOLEAN.create(true), BooleanType.BOOLEAN.create(false)));
		verify(context);
		reset(context);
		replay(context);
		assertEquals(BooleanType.BOOLEAN.create(true), 
				LogicalFunctions.or(context, BooleanType.BOOLEAN.create(true), BooleanType.BOOLEAN.create(true)));
		verify(context);
	}
	
	@Test
	public void testNOfFunction() throws EvaluationException
	{
		IntegerValue n = IntegerType.INTEGER.create(0);
		replay(context);
		assertEquals(BooleanType.BOOLEAN.create(true), 
				LogicalFunctions.nof(context, n));
		verify(context);
		reset(context);
		replay(context);
		n = IntegerType.INTEGER.create(1);
		assertEquals(BooleanType.BOOLEAN.create(true), 
				LogicalFunctions.nof(context, n, BooleanType.BOOLEAN.create(true)));
		verify(context);
		reset(context);
		replay(context);
		assertEquals(BooleanType.BOOLEAN.create(false), 
				LogicalFunctions.nof(context, n, BooleanType.BOOLEAN.create(false)));
		verify(context);
		reset(context);
		replay(context);
		n = IntegerType.INTEGER.create(2);
		assertEquals(BooleanType.BOOLEAN.create(true), 
				LogicalFunctions.nof(context, n, 
						BooleanType.BOOLEAN.create(true), BooleanType.BOOLEAN.create(true)));
		verify(context);
		reset(context);
		replay(context);
		assertEquals(BooleanType.BOOLEAN.create(false), 
				LogicalFunctions.nof(context, n, 
						BooleanType.BOOLEAN.create(true), BooleanType.BOOLEAN.create(false)));
		verify(context);
		reset(context);
		replay(context);
		n = IntegerType.INTEGER.create(2);
		assertEquals(BooleanType.BOOLEAN.create(true), 
				LogicalFunctions.nof(context, n, 
						BooleanType.BOOLEAN.create(true), BooleanType.BOOLEAN.create(false), BooleanType.BOOLEAN.create(true)));
		verify(context);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testNOfFunctionInderterminate() throws EvaluationException
	{
		IntegerValue n = IntegerType.INTEGER.create(4);
		replay(context); 
		assertEquals(BooleanType.BOOLEAN.create(false), 
				LogicalFunctions.nof(context, n, BooleanType.BOOLEAN.create(false)));
		verify(context);
	}
}
