package com.artagon.xacml.v30.marshall.jaxb.builder;

import org.junit.Test;
import com.artagon.xacml.v30.marshall.jaxb.builder.ExpressionTypeBuilder.*;

public class ExpressionBuilderTest 
{
	@Test
	public void testExpressionBuilder()
	{
		ApplyTypeBuilder.newBuilder()
		.functionId("test")
		.expression(VariableReferenceTypeBuilder.newBuilder()
				.variableId("test"))
				.build();
	}
}
