package org.xacml4j.v30.marshall.jaxb.builder;


import org.junit.Test;
import static org.junit.Assert.*;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.types.StringType;

public class ExpressionBuilderTest 
{
	@Test
	public void testExpressionBuilder()
	{
		AttributeExp v = StringType.STRING.create("aa");
		ExpressionTypeBuilder b = ExpressionTypeBuilder. getBuilder(v);
		assertNotNull(b);
	}
}
