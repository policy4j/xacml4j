package com.artagon.xacml.v3.types;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.artagon.xacml.v3.XacmlSyntaxException;

public class XacmlTypesTest
{
	@Test
	public void testXACML20DeprecatedTypeMapping() throws XacmlSyntaxException
	{
		assertNotNull(XacmlDataTypes.getType("http://www.w3.org/TR/2002/WD-xquery-operators-20020816#yearMonthDuration"));
		assertNotNull(XacmlDataTypes.getType("urn:oasis:names:tc:xacml:2.0:data-type:xpathExpression"));
		assertNotNull(XacmlDataTypes.getType("http://www.w3.org/TR/2002/WD-xquery-operators-20020816#dayTimeDuration"));
	}
	
}
