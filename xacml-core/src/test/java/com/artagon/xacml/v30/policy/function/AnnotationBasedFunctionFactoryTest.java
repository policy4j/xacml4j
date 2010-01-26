package com.artagon.xacml.v30.policy.function;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.artagon.xacml.v30.policy.FunctionFactory;
import com.artagon.xacml.v30.policy.FunctionSpec;
import com.artagon.xacml.v30.policy.XacmlPolicyTestCase;
import com.artagon.xacml.v30.policy.function.AnnotationBasedFunctionFactory;
import com.artagon.xacml.v30.policy.type.DataTypes;

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
