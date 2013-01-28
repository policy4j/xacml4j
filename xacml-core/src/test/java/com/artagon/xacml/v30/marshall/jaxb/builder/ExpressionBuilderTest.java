package com.artagon.xacml.v30.marshall.jaxb.builder;


import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.artagon.xacml.v30.AttributeExp;
import com.artagon.xacml.v30.types.StringType;

public class ExpressionBuilderTest 
{
	@Test
	public void testExpressionBuilder()
	{
		AttributeExp v = StringType.STRING.create("aa");
		ExpressionTypeBuilder b = ExpressionTypeBuilder.getBuilder(v);
		assertNotNull(b);
	}
}
