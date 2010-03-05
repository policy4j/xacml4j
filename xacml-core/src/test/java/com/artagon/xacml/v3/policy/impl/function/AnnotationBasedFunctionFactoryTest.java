package com.artagon.xacml.v3.policy.impl.function;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.policy.FunctionFactory;
import com.artagon.xacml.v3.policy.FunctionSpec;
import com.artagon.xacml.v3.policy.XacmlPolicyTestCase;
import com.artagon.xacml.v3.policy.type.DataTypes;

public class AnnotationBasedFunctionFactoryTest extends XacmlPolicyTestCase
{
	private FunctionFactory f;
	
	@Before
	public void init(){
		this.f = new AnnotationBasedFunctionFactory(TestFunctions.class);
	}
	
	@Test
	public void testTest1And2Functions() throws Exception
	{
		FunctionSpec spec1 = f.getFunction("test1");
		assertEquals(DataTypes.BOOLEAN.create(Boolean.FALSE),  
				spec1.invoke(context, DataTypes.INTEGER.create(1), DataTypes.INTEGER.create(2)));
		
		FunctionSpec spec2 = f.getFunction("test2");
		assertEquals(DataTypes.INTEGER.create(2),  
				spec2.invoke(context, DataTypes.INTEGER.bag(
						DataTypes.INTEGER.create(1), DataTypes.INTEGER.create(2))));
		
	}
	
	@Test
	public void testLazyParamEvaluationPassingEvaluationContext() throws Exception
	{
		FunctionSpec spec3 = f.getFunction("test3");
		FunctionSpec spec4 = f.getFunction("test4");
		spec3.invoke(context, DataTypes.INTEGER.create(10), DataTypes.INTEGER.create(10));
		spec3.invoke(context, DataTypes.INTEGER.create(10));
		spec4.invoke(context, DataTypes.INTEGER.create(10));
		
	}
	
	@Test
	public void testVarArgFunctionInvocation() throws Exception
	{
		FunctionSpec spec5 = f.getFunction("test5VarArg");
		FunctionSpec spec6 = f.getFunction("test6VarArg");
		
		spec5.invoke(context, DataTypes.INTEGER.create(10));
		spec5.invoke(context, DataTypes.INTEGER.create(10), DataTypes.BOOLEAN.create(false));
		spec5.invoke(context, DataTypes.INTEGER.create(10), DataTypes.BOOLEAN.create(false), DataTypes.BOOLEAN.create(false));
		
		spec6.invoke(context, DataTypes.INTEGER.create(10), DataTypes.INTEGER.create(10));
		spec6.invoke(context, DataTypes.INTEGER.create(10), DataTypes.INTEGER.create(10), DataTypes.BOOLEAN.create(false));
		
	}
}
