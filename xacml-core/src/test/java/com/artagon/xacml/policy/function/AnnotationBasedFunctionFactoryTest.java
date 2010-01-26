package com.artagon.xacml.policy.function;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.artagon.xacml.policy.FunctionFactory;
import com.artagon.xacml.policy.FunctionSpec;
import com.artagon.xacml.policy.XacmlPolicyTestCase;
import com.artagon.xacml.policy.type.DataTypes;

public class AnnotationBasedFunctionFactoryTest extends XacmlPolicyTestCase
{
	
	@Test
	public void testFunctionAnnotations() throws Exception
	{
		FunctionFactory f = new AnnotationBasedFunctionFactory(TestFunctions.class);
		FunctionSpec spec1 = f.getFunction("test1");
		assertEquals(DataTypes.BOOLEAN.create(Boolean.FALSE),  
				spec1.invoke(context, DataTypes.INTEGER.create(1), DataTypes.INTEGER.create(2)));
		
		FunctionSpec spec2 = f.getFunction("test2");
		assertEquals(DataTypes.INTEGER.create(2),  
				spec2.invoke(context, DataTypes.INTEGER.bag(
						DataTypes.INTEGER.create(1), DataTypes.INTEGER.create(2))));
		
	}
}
