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
		FunctionFactory f = new AnnotationBasedFunctionFactory(EqualityPredicates.class);
		FunctionSpec spec = f.getFunction("urn:oasis:names:tc:xacml:1.0:function:integer-equal");
		assertEquals(XacmlDataType.BOOLEAN.create(Boolean.FALSE),  
				spec.invoke(context, XacmlDataType.INTEGER.create(1), XacmlDataType.INTEGER.create(2)));
	}
}
