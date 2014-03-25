package org.xacml4j.v30.policy.function;


import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.pdp.FunctionSpec;
import org.xacml4j.v30.spi.function.AnnotiationBasedFunctionProvider;
import org.xacml4j.v30.spi.function.FunctionProvider;
import org.xacml4j.v30.types.BooleanExp;
import org.xacml4j.v30.types.IntegerExp;


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
		assertEquals(BooleanExp.valueOf(false),
				LogicalFunctions.and(context, BooleanExp.valueOf(false), BooleanExp.valueOf(false)));
		control.verify();

		control.reset();
		control.replay();
		assertEquals(BooleanExp.valueOf(false),
				LogicalFunctions.and(context, BooleanExp.valueOf(true), BooleanExp.valueOf(false)));
		control.verify();

		control.reset();
		control.replay();
		assertEquals(BooleanExp.valueOf(true),
				LogicalFunctions.and(context, BooleanExp.valueOf(true), BooleanExp.valueOf(true)));
		control.verify();

		control.reset();
		control.replay();
		assertEquals(BooleanExp.valueOf(false),
				LogicalFunctions.and(context, BooleanExp.valueOf(false), BooleanExp.valueOf(true)));
		control.verify();
	}

	@Test
	public void testLazyAndFunctionParamEvaluation() throws EvaluationException
	{
		Expression p1 = control.createMock(Expression.class);
		Expression p2 = control.createMock(Expression.class);
		Expression p3 = control.createMock(Expression.class);

		expect(p1.evaluate(context)).andReturn(BooleanExp.valueOf(false));
		control.replay();

		assertEquals(BooleanExp.valueOf(false),
				LogicalFunctions.and(context, p1, p2, p3));
		control.verify();

		control.reset();
		expect(p1.evaluate(context)).andReturn(BooleanExp.valueOf(true));
		expect(p2.evaluate(context)).andReturn(BooleanExp.valueOf(false));
		control.replay();

		assertEquals(BooleanExp.valueOf(false),
				LogicalFunctions.and(context, p1, p2, p3));
		control.verify();

		control.reset();
		expect(p1.evaluate(context)).andReturn(BooleanExp.valueOf(true));
		expect(p2.evaluate(context)).andReturn(BooleanExp.valueOf(true));
		expect(p3.evaluate(context)).andReturn(BooleanExp.valueOf(false));
		control.replay();

		assertEquals(BooleanExp.valueOf(false),
				LogicalFunctions.and(context, p1, p2, p3));
		control.verify();

	}

	@Test
	public void testOrFunction() throws EvaluationException
	{
		control.replay();
		assertEquals(BooleanExp.valueOf(false),
				LogicalFunctions.or(context, BooleanExp.valueOf(false), 
						BooleanExp.valueOf(false)));
		control.verify();
		control.reset();
		control.replay();
		assertEquals(BooleanExp.valueOf(true),
				LogicalFunctions.or(context, BooleanExp.valueOf(true), BooleanExp.valueOf(false)));
		control.verify();
		control.reset();
		control.replay();
		assertEquals(BooleanExp.valueOf(true),
				LogicalFunctions.or(context, BooleanExp.valueOf(true), BooleanExp.valueOf(true)));
		control.verify();
		control.reset();
		control.replay();
		assertEquals(BooleanExp.valueOf(true),
				LogicalFunctions.or(context,BooleanExp.valueOf(false), BooleanExp.valueOf(true)));
		verify(context);
	}

	@Test
	public void testLazyOrFunctionParamEvaluation() throws EvaluationException
	{
		Expression p1 = control.createMock(Expression.class);
		Expression p2 = control.createMock(Expression.class);
		Expression p3 = control.createMock(Expression.class);

		expect(p1.evaluate(context)).andReturn(BooleanExp.valueOf(true));
		control.replay();

		assertEquals(BooleanExp.valueOf(true),
				LogicalFunctions.or(context, p1, p2, p3));
		control.verify();

		control.reset();
		expect(p1.evaluate(context)).andReturn(BooleanExp.valueOf(false));
		expect(p2.evaluate(context)).andReturn(BooleanExp.valueOf(true));
		control.replay();

		assertEquals(BooleanExp.valueOf(true),
				LogicalFunctions.or(context, p1, p2, p3));
		control.verify();

		control.reset();
		expect(p1.evaluate(context)).andReturn(BooleanExp.valueOf(false));
		expect(p2.evaluate(context)).andReturn(BooleanExp.valueOf(false));
		control.replay();

		assertEquals(BooleanExp.valueOf(false),
				LogicalFunctions.or(context, p1, p2));
		control.verify();

	}

	@Test
	public void testNOfFunction() throws EvaluationException
	{
		IntegerExp n = IntegerExp.valueOf(0);
		replay(context);
		assertEquals(BooleanExp.valueOf(true),
				LogicalFunctions.nof(context, n));
		verify(context);
		reset(context);
		replay(context);
		n = IntegerExp.valueOf(1);
		assertEquals(BooleanExp.valueOf(true),
				LogicalFunctions.nof(context, n, BooleanExp.valueOf(true)));
		verify(context);
		reset(context);
		replay(context);
		assertEquals(BooleanExp.valueOf(false),
				LogicalFunctions.nof(context, n, BooleanExp.valueOf(false)));
		verify(context);
		reset(context);
		replay(context);
		n = IntegerExp.valueOf(2);
		assertEquals(BooleanExp.valueOf(true),
				LogicalFunctions.nof(context, n,
						BooleanExp.valueOf(true), BooleanExp.valueOf(true)));
		verify(context);
		reset(context);
		replay(context);
		assertEquals(BooleanExp.valueOf(false),
				LogicalFunctions.nof(context, n,
						BooleanExp.valueOf(true), BooleanExp.valueOf(false)));
		verify(context);
		reset(context);
		replay(context);
		n = IntegerExp.valueOf(2);
		assertEquals(BooleanExp.valueOf(true),
				LogicalFunctions.nof(context, n,
						BooleanExp.valueOf(true), 
						BooleanExp.valueOf(false), 
						BooleanExp.valueOf(true)));
		verify(context);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testNOfFunctionInderterminate() throws EvaluationException
	{
		IntegerExp n = IntegerExp.valueOf(4);
		replay(context);
		assertEquals(BooleanExp.valueOf(false),
				LogicalFunctions.nof(context, n, BooleanExp.valueOf(false)));
		verify(context);
	}
}
