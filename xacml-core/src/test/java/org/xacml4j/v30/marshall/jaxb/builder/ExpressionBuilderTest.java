package org.xacml4j.v30.marshall.jaxb.builder;

import org.junit.Test;
import org.xacml4j.v30.marshall.jaxb.builder.ExpressionTypeBuilder.ApplyTypeBuilder;
import org.xacml4j.v30.marshall.jaxb.builder.ExpressionTypeBuilder.VariableReferenceTypeBuilder;


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
