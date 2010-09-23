package com.artagon.xacml.v3.spi.function;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.FunctionSpec;
import com.artagon.xacml.v3.spi.FunctionProvider;
import com.artagon.xacml.v3.types.IntegerType;
import com.artagon.xacml.v3.types.XacmlDataTypes;

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
		assertEquals(XacmlDataTypes.BOOLEAN.create(Boolean.FALSE),  
				spec1.invoke(context, IntegerType.Factory.create(1), IntegerType.Factory.create(2)));
		
		FunctionSpec spec2 = f1.getFunction("test2");
		assertEquals(XacmlDataTypes.INTEGER.create(2),  
				spec2.invoke(context, IntegerType.Factory.bagOf(
						IntegerType.Factory.create(1), IntegerType.Factory.create(2))));
		verify(context);
		
	}
	
	@Test
	public void testTest1And2FunctionsInstanceProvider() throws Exception
	{
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false).times(2);
		replay(context);
		FunctionSpec spec1 = f2.getFunction("test1");
		assertNotNull(spec1);
		assertEquals(XacmlDataTypes.BOOLEAN.create(Boolean.FALSE),  
				spec1.invoke(context, XacmlDataTypes.INTEGER.create(1), XacmlDataTypes.INTEGER.create(2)));
		
		FunctionSpec spec2 = f2.getFunction("test2");
		assertEquals(XacmlDataTypes.INTEGER.create(2),  
				spec2.invoke(context, IntegerType.Factory.bagOf(
						IntegerType.Factory.create(1), IntegerType.Factory.create(2))));
		verify(context);
		
	}
	
	@Test
	public void testLazyParamEvaluationPassingEvaluationContext() throws Exception
	{
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false).times(3);
		replay(context);
		FunctionSpec spec3 = f1.getFunction("test3");
		FunctionSpec spec4 = f1.getFunction("test4");
		spec3.invoke(context, XacmlDataTypes.INTEGER.create(10), XacmlDataTypes.INTEGER.create(10));
		spec3.invoke(context, XacmlDataTypes.INTEGER.create(10));
		spec4.invoke(context, XacmlDataTypes.INTEGER.create(10));
		verify(context);
		
	}
	
	@Test
	public void testVarArgFunctionInvocation() throws Exception
	{
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false).times(5);
		replay(context);
		FunctionSpec spec5 = f1.getFunction("test5VarArg");
		FunctionSpec spec6 = f1.getFunction("test6VarArg");
		
		spec5.invoke(context, XacmlDataTypes.INTEGER.create(10));
		spec5.invoke(context, XacmlDataTypes.INTEGER.create(10), XacmlDataTypes.BOOLEAN.create(false));
		spec5.invoke(context, XacmlDataTypes.INTEGER.create(10), XacmlDataTypes.BOOLEAN.create(false), XacmlDataTypes.BOOLEAN.create(false));
		
		spec6.invoke(context, XacmlDataTypes.INTEGER.create(10), XacmlDataTypes.INTEGER.create(10));
		spec6.invoke(context, XacmlDataTypes.INTEGER.create(10), XacmlDataTypes.INTEGER.create(10), XacmlDataTypes.BOOLEAN.create(false));
		verify(context);
		
	}
	 
}
