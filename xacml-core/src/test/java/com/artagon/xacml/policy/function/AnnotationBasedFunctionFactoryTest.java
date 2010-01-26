package com.artagon.xacml.policy.function;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import com.artagon.xacml.policy.FunctionFactory;
import com.artagon.xacml.policy.FunctionSpec;
import com.artagon.xacml.policy.XacmlPolicyTestCase;
import com.artagon.xacml.policy.type.XacmlDataType;

public class AnnotationBasedFunctionFactoryTest extends XacmlPolicyTestCase
{
	
	@Test
	public void testFunctionAnnotations() throws Exception
	{
		FunctionFactory f = new AnnotationBasedFunctionFactory(TestFunctions.class);
		FunctionSpec spec1 = f.getFunction("test1");
		assertEquals(XacmlDataType.BOOLEAN.create(Boolean.FALSE),  
				spec1.invoke(context, XacmlDataType.INTEGER.create(1), XacmlDataType.INTEGER.create(2)));
		
		FunctionSpec spec2 = f.getFunction("test2");
		assertEquals(XacmlDataType.INTEGER.create(2),  
				spec2.invoke(context, XacmlDataType.INTEGER.bag(
						XacmlDataType.INTEGER.create(1), XacmlDataType.INTEGER.create(2))));
		
	}
}
