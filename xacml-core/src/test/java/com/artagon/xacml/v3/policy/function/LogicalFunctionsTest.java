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
import com.artagon.xacml.v3.FunctionSpec;
import com.artagon.xacml.v3.spi.FunctionProvider;
import com.artagon.xacml.v3.spi.function.AnnotiationBasedFunctionProvider;
import com.artagon.xacml.v3.types.IntegerType;
import com.artagon.xacml.v3.types.XacmlDataTypes;

public class LogicalFunctionsTest
{
	private FunctionProvider f;
	
	FunctionSpec andFunc;
	FunctionSpec orFunc;
	FunctionSpec notFunc;
	
	private EvaluationContext context;
	
	@Before
	public void init(){
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
		assertEquals(XacmlDataTypes.BOOLEAN.create(false), 
				LogicalFunctions.and(context, XacmlDataTypes.BOOLEAN.create(false), XacmlDataTypes.BOOLEAN.create(false)));
		verify(context);
		reset(context);
		replay(context);
		assertEquals(XacmlDataTypes.BOOLEAN.create(false), 
				LogicalFunctions.and(context, XacmlDataTypes.BOOLEAN.create(true), XacmlDataTypes.BOOLEAN.create(false)));
		verify(context);
		reset(context);
		replay(context);
		assertEquals(XacmlDataTypes.BOOLEAN.create(true), 
				LogicalFunctions.and(context, XacmlDataTypes.BOOLEAN.create(true), XacmlDataTypes.BOOLEAN.create(true)));
		verify(context);
	}
	
	@Test
	public void testBasicOrFunction() throws EvaluationException
	{
		replay(context);
		assertEquals(XacmlDataTypes.BOOLEAN.create(false), 
				LogicalFunctions.or(context, XacmlDataTypes.BOOLEAN.create(false), XacmlDataTypes.BOOLEAN.create(false)));
		verify(context);
		reset(context);
		replay(context);
		assertEquals(XacmlDataTypes.BOOLEAN.create(true), 
				LogicalFunctions.or(context, XacmlDataTypes.BOOLEAN.create(true), XacmlDataTypes.BOOLEAN.create(false)));
		verify(context);
		reset(context);
		replay(context);
		assertEquals(XacmlDataTypes.BOOLEAN.create(true), 
				LogicalFunctions.or(context, XacmlDataTypes.BOOLEAN.create(true), XacmlDataTypes.BOOLEAN.create(true)));
		verify(context);
	}
	
	@Test
	public void testNOfFunction() throws EvaluationException
	{
		IntegerType.IntegerValue n = XacmlDataTypes.INTEGER.create(0);
		replay(context);
		assertEquals(XacmlDataTypes.BOOLEAN.create(true), 
				LogicalFunctions.nof(context, n));
		verify(context);
		reset(context);
		replay(context);
		n = XacmlDataTypes.INTEGER.create(1);
		assertEquals(XacmlDataTypes.BOOLEAN.create(true), 
				LogicalFunctions.nof(context, n, XacmlDataTypes.BOOLEAN.create(true)));
		verify(context);
		reset(context);
		replay(context);
		assertEquals(XacmlDataTypes.BOOLEAN.create(false), 
				LogicalFunctions.nof(context, n, XacmlDataTypes.BOOLEAN.create(false)));
		verify(context);
		reset(context);
		replay(context);
		n = XacmlDataTypes.INTEGER.create(2);
		assertEquals(XacmlDataTypes.BOOLEAN.create(true), 
				LogicalFunctions.nof(context, n, 
						XacmlDataTypes.BOOLEAN.create(true), XacmlDataTypes.BOOLEAN.create(true)));
		verify(context);
		reset(context);
		replay(context);
		assertEquals(XacmlDataTypes.BOOLEAN.create(false), 
				LogicalFunctions.nof(context, n, 
						XacmlDataTypes.BOOLEAN.create(true), XacmlDataTypes.BOOLEAN.create(false)));
		verify(context);
		reset(context);
		replay(context);
		n = XacmlDataTypes.INTEGER.create(2);
		assertEquals(XacmlDataTypes.BOOLEAN.create(true), 
				LogicalFunctions.nof(context, n, 
						XacmlDataTypes.BOOLEAN.create(true), XacmlDataTypes.BOOLEAN.create(false), XacmlDataTypes.BOOLEAN.create(true)));
		verify(context);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testNOfFunctionInderterminate() throws EvaluationException
	{
		IntegerType.IntegerValue n = XacmlDataTypes.INTEGER.create(4);
		replay(context); 
		assertEquals(XacmlDataTypes.BOOLEAN.create(false), 
				LogicalFunctions.nof(context, n, XacmlDataTypes.BOOLEAN.create(false)));
		verify(context);
	}
}
