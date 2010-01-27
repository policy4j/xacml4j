package com.artagon.xacml.v3.policy.function;

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
	public void testLazyParamEvaluation() throws Exception
	{
		FunctionSpec spec = f.getFunction("test3");
		spec.invoke(context, DataTypes.INTEGER.create(10));
		
	}
	
	@Test
	public void testTest5Function() throws Exception
	{
		FunctionSpec spec = f.getFunction("test5");
		
		spec.invoke(context, DataTypes.INTEGER.create(10));
		spec.invoke(context, DataTypes.INTEGER.create(10), DataTypes.BOOLEAN.create(false));
		
	}
}
