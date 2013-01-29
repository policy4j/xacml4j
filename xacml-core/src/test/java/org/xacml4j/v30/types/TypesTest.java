package org.xacml4j.v30.types;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.XacmlSyntaxException;

public class TypesTest
{
	private Types types;
	
	@Before
	public void init(){
		this.types = Types.builder().defaultTypes().create();
	}
	
	@Test
	public void testXACML20DeprecatedTypeMapping() throws XacmlSyntaxException
	{
		assertNotNull(types.getType("http://www.w3.org/TR/2002/WD-xquery-operators-20020816#yearMonthDuration"));
		assertNotNull(types.getType("urn:oasis:names:tc:xacml:2.0:data-type:xpathExpression"));
		assertNotNull(types.getType("http://www.w3.org/TR/2002/WD-xquery-operators-20020816#dayTimeDuration"));
	}
	
}
