package org.xacml4j.v30.spi.function;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.pdp.FunctionSpec;
import org.xacml4j.v30.types.BooleanExp;
import org.xacml4j.v30.types.IntegerExp;



public class AnnotationBasedFunctionFactoryTest
{
	private FunctionProvider f1;
	private FunctionProvider f2;
	private EvaluationContext context;

	@Before
	public void init() throws Exception
	{
		this.f1 = new AnnotiationBasedFunctionProvider(TestFunctions.class);
		this.f2 = new AnnotiationBasedFunctionProvider(new TestInstanceFunctions());
		this.context = createStrictMock(EvaluationContext.class);
	}

	@Test
	public void testTest1And2FunctionsStaticProvider() throws Exception
	{
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false).times(2);
		replay(context);
		FunctionSpec spec1 = f1.getFunction("test1");
		assertEquals(BooleanExp.valueOf(Boolean.FALSE),
				spec1.invoke(context, IntegerExp.valueOf(1), IntegerExp.valueOf(2)));

		FunctionSpec spec2 = f1.getFunction("test2");
		assertEquals(IntegerExp.valueOf(2),
				spec2.invoke(context, IntegerExp.bag().value(1, 2).build()));
		verify(context);

	}

	@Test
	public void testTest1And2FunctionsInstanceProvider() throws Exception
	{
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false).times(2);
		replay(context);
		FunctionSpec spec1 = f2.getFunction("test1");
		assertNotNull(spec1);
		assertEquals(BooleanExp.valueOf(Boolean.FALSE),
				spec1.invoke(context, IntegerExp.valueOf(1), IntegerExp.valueOf(2)));

		FunctionSpec spec2 = f2.getFunction("test2");
		assertEquals(IntegerExp.valueOf(2),
				spec2.invoke(context, IntegerExp.bag().value(1, 2).build()));
		verify(context);

	}

	@Test
	public void testLazyParamEvaluationPassingEvaluationContext() throws Exception
	{
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false).times(3);
		replay(context);
		FunctionSpec spec3 = f1.getFunction("test3");
		FunctionSpec spec4 = f1.getFunction("test4");
		spec3.invoke(context, IntegerExp.valueOf(10), IntegerExp.valueOf(10));
		spec3.invoke(context, IntegerExp.valueOf(10));
		spec4.invoke(context, IntegerExp.valueOf(10));
		verify(context);

	}

	@Test
	public void testVarArgFunctionInvocation() throws Exception
	{
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false).times(5);
		replay(context);
		FunctionSpec spec5 = f1.getFunction("test5VarArg");
		FunctionSpec spec6 = f1.getFunction("test6VarArg");

		spec5.invoke(context, IntegerExp.valueOf(10));
		spec5.invoke(context, IntegerExp.valueOf(10), BooleanExp.valueOf(false));
		spec5.invoke(context, IntegerExp.valueOf(10), BooleanExp.valueOf(false), BooleanExp.valueOf(false));

		spec6.invoke(context, IntegerExp.valueOf(10), IntegerExp.valueOf(10));
		spec6.invoke(context, IntegerExp.valueOf(10), IntegerExp.valueOf(10), BooleanExp.valueOf(false));
		verify(context);

	}

}
