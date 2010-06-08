package com.artagon.xacml.v3.policy.spi;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.policy.FunctionSpec;
import com.artagon.xacml.v3.policy.function.TestFunctions;
import com.artagon.xacml.v3.policy.spi.function.ReflectionBasedFunctionProvider;
import com.artagon.xacml.v3.types.XacmlDataTypes;

public class AnnotationBasedFunctionFactoryTest
{
	private FunctionProvider f;
	private EvaluationContext context;
	
	@Before
	public void init(){
		this.f = new ReflectionBasedFunctionProvider(TestFunctions.class);
		this.context = createStrictMock(EvaluationContext.class);
	}
	
	@Test
	public void testTest1And2Functions() throws Exception
	{
		expect(context.isValidateFuncParamAtRuntime()).andReturn(false).times(2);
		replay(context);
		FunctionSpec spec1 = f.getFunction("test1");
		assertEquals(XacmlDataTypes.BOOLEAN.create(Boolean.FALSE),  
				spec1.invoke(context, XacmlDataTypes.INTEGER.create(1), XacmlDataTypes.INTEGER.create(2)));
		
		FunctionSpec spec2 = f.getFunction("test2");
		assertEquals(XacmlDataTypes.INTEGER.create(2),  
				spec2.invoke(context, XacmlDataTypes.INTEGER.bag(
						XacmlDataTypes.INTEGER.create(1), XacmlDataTypes.INTEGER.create(2))));
		verify(context);
		
	}
	
	@Test
	public void testLazyParamEvaluationPassingEvaluationContext() throws Exception
	{
		expect(context.isValidateFuncParamAtRuntime()).andReturn(false).times(3);
		replay(context);
		FunctionSpec spec3 = f.getFunction("test3");
		FunctionSpec spec4 = f.getFunction("test4");
		spec3.invoke(context, XacmlDataTypes.INTEGER.create(10), XacmlDataTypes.INTEGER.create(10));
		spec3.invoke(context, XacmlDataTypes.INTEGER.create(10));
		spec4.invoke(context, XacmlDataTypes.INTEGER.create(10));
		verify(context);
		
	}
	
	@Test
	public void testVarArgFunctionInvocation() throws Exception
	{
		expect(context.isValidateFuncParamAtRuntime()).andReturn(false).times(5);
		replay(context);
		FunctionSpec spec5 = f.getFunction("test5VarArg");
		FunctionSpec spec6 = f.getFunction("test6VarArg");
		
		spec5.invoke(context, XacmlDataTypes.INTEGER.create(10));
		spec5.invoke(context, XacmlDataTypes.INTEGER.create(10), XacmlDataTypes.BOOLEAN.create(false));
		spec5.invoke(context, XacmlDataTypes.INTEGER.create(10), XacmlDataTypes.BOOLEAN.create(false), XacmlDataTypes.BOOLEAN.create(false));
		
		spec6.invoke(context, XacmlDataTypes.INTEGER.create(10), XacmlDataTypes.INTEGER.create(10));
		spec6.invoke(context, XacmlDataTypes.INTEGER.create(10), XacmlDataTypes.INTEGER.create(10), XacmlDataTypes.BOOLEAN.create(false));
		verify(context);
		
	}
}
