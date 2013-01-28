package com.artagon.xacml.v30.types;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v30.XacmlSyntaxException;

public class TypesTest
{
	private Types types;
	
	@Before
	protected void init(){
		this.types = Types.Builder.builder().defaultTypes().create();
	}
	
	@Test
	public void testXACML20DeprecatedTypeMapping() throws XacmlSyntaxException
	{
		assertNotNull(types.getType("http://www.w3.org/TR/2002/WD-xquery-operators-20020816#yearMonthDuration"));
		assertNotNull(types.getType("urn:oasis:names:tc:xacml:2.0:data-type:xpathExpression"));
		assertNotNull(types.getType("http://www.w3.org/TR/2002/WD-xquery-operators-20020816#dayTimeDuration"));
	}
	
}
