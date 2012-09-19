package com.artagon.xacml.v30.policy.function;


import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.EvaluationException;
import com.artagon.xacml.v30.Expression;
import com.artagon.xacml.v30.pdp.FunctionSpec;
import com.artagon.xacml.v30.spi.function.AnnotiationBasedFunctionProvider;
import com.artagon.xacml.v30.spi.function.FunctionProvider;
import com.artagon.xacml.v30.types.BooleanType;
import com.artagon.xacml.v30.types.IntegerExp;
import com.artagon.xacml.v30.types.IntegerType;

public class LogicalFunctionsTest
{
	private FunctionProvider f;
	
	FunctionSpec andFunc;
	FunctionSpec orFunc;
	FunctionSpec notFunc;
	
	private IMocksControl control;
	
	private EvaluationContext context;
	
	@Before
	public void init() throws Exception
	{
		this.control = createControl();
		this.f = new AnnotiationBasedFunctionProvider(LogicalFunctions.class);
		this.andFunc = f.getFunction("urn:oasis:names:tc:xacml:1.0:function:and");
		this.orFunc = f.getFunction("urn:oasis:names:tc:xacml:1.0:function:or");
		this.notFunc = f.getFunction("urn:oasis:names:tc:xacml:1.0:function:not");
		this.context = control.createMock(EvaluationContext.class);
	}
	
	
	@Test
	public void testAndFunction() throws EvaluationException
	{
		control.replay();
		assertEquals(BooleanType.BOOLEAN.create(false), 
				LogicalFunctions.and(context, BooleanType.BOOLEAN.create(false), BooleanType.BOOLEAN.create(false)));
		control.verify();
		
		control.reset();
		control.replay();
		assertEquals(BooleanType.BOOLEAN.create(false), 
				LogicalFunctions.and(context, BooleanType.BOOLEAN.create(true), BooleanType.BOOLEAN.create(false)));
		control.verify();
		
		control.reset();
		control.replay();
		assertEquals(BooleanType.BOOLEAN.create(true), 
				LogicalFunctions.and(context, BooleanType.BOOLEAN.create(true), BooleanType.BOOLEAN.create(true)));
		control.verify();
		
		control.reset();
		control.replay();
		assertEquals(BooleanType.BOOLEAN.create(false), 
				LogicalFunctions.and(context, BooleanType.BOOLEAN.create(false), BooleanType.BOOLEAN.create(true)));
		control.verify();
	}
	
	@Test
	public void testLazyAndFunctionParamEvaluation() throws EvaluationException
	{
		Expression p1 = control.createMock(Expression.class);
		Expression p2 = control.createMock(Expression.class);
		Expression p3 = control.createMock(Expression.class);
		
		expect(p1.evaluate(context)).andReturn(BooleanType.BOOLEAN.create(false));
		control.replay();
		
		assertEquals(BooleanType.BOOLEAN.create(false), 
				LogicalFunctions.and(context, p1, p2, p3));
		control.verify();
		
		control.reset();
		expect(p1.evaluate(context)).andReturn(BooleanType.BOOLEAN.create(true));
		expect(p2.evaluate(context)).andReturn(BooleanType.BOOLEAN.create(false));
		control.replay();
		
		assertEquals(BooleanType.BOOLEAN.create(false), 
				LogicalFunctions.and(context, p1, p2, p3));
		control.verify();
		
		control.reset();
		expect(p1.evaluate(context)).andReturn(BooleanType.BOOLEAN.create(true));
		expect(p2.evaluate(context)).andReturn(BooleanType.BOOLEAN.create(true));
		expect(p3.evaluate(context)).andReturn(BooleanType.BOOLEAN.create(false));
		control.replay();
		
		assertEquals(BooleanType.BOOLEAN.create(false), 
				LogicalFunctions.and(context, p1, p2, p3));
		control.verify();

	}
	
	@Test
	public void testOrFunction() throws EvaluationException
	{
		control.replay();
		assertEquals(BooleanType.BOOLEAN.create(false), 
				LogicalFunctions.or(context, BooleanType.BOOLEAN.create(false), BooleanType.BOOLEAN.create(false)));
		control.verify();
		control.reset();
		control.replay();
		assertEquals(BooleanType.BOOLEAN.create(true), 
				LogicalFunctions.or(context, BooleanType.BOOLEAN.create(true), BooleanType.BOOLEAN.create(false)));
		control.verify();
		control.reset();
		control.replay();
		assertEquals(BooleanType.BOOLEAN.create(true), 
				LogicalFunctions.or(context, BooleanType.BOOLEAN.create(true), BooleanType.BOOLEAN.create(true)));
		control.verify();
		control.reset();
		control.replay();
		assertEquals(BooleanType.BOOLEAN.create(true), 
				LogicalFunctions.or(context, BooleanType.BOOLEAN.create(false), BooleanType.BOOLEAN.create(true)));
		verify(context);
	}
	
	@Test
	public void testLazyOrFunctionParamEvaluation() throws EvaluationException
	{
		Expression p1 = control.createMock(Expression.class);
		Expression p2 = control.createMock(Expression.class);
		Expression p3 = control.createMock(Expression.class);
		
		expect(p1.evaluate(context)).andReturn(BooleanType.BOOLEAN.create(true));
		control.replay();
		
		assertEquals(BooleanType.BOOLEAN.create(true), 
				LogicalFunctions.or(context, p1, p2, p3));
		control.verify();
		
		control.reset();
		expect(p1.evaluate(context)).andReturn(BooleanType.BOOLEAN.create(false));
		expect(p2.evaluate(context)).andReturn(BooleanType.BOOLEAN.create(true));
		control.replay();
		
		assertEquals(BooleanType.BOOLEAN.create(true), 
				LogicalFunctions.or(context, p1, p2, p3));
		control.verify();
		
		control.reset();
		expect(p1.evaluate(context)).andReturn(BooleanType.BOOLEAN.create(false));
		expect(p2.evaluate(context)).andReturn(BooleanType.BOOLEAN.create(false));
		control.replay();
		
		assertEquals(BooleanType.BOOLEAN.create(false), 
				LogicalFunctions.or(context, p1, p2));
		control.verify();

	}
	
	@Test
	public void testNOfFunction() throws EvaluationException
	{
		IntegerExp n = IntegerType.INTEGER.create(0);
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
		IntegerExp n = IntegerType.INTEGER.create(4);
		replay(context); 
		assertEquals(BooleanType.BOOLEAN.create(false), 
				LogicalFunctions.nof(context, n, BooleanType.BOOLEAN.create(false)));
		verify(context);
	}
}
