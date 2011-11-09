package com.artagon.xacml.v30.types;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.artagon.xacml.v30.pdp.XacmlSyntaxException;

public class XacmlTypesTest
{
	@Test
	public void testXACML20DeprecatedTypeMapping() throws XacmlSyntaxException
	{
		assertNotNull(DataTypes.getType("http://www.w3.org/TR/2002/WD-xquery-operators-20020816#yearMonthDuration"));
		assertNotNull(DataTypes.getType("urn:oasis:names:tc:xacml:2.0:data-type:xpathExpression"));
		assertNotNull(DataTypes.getType("http://www.w3.org/TR/2002/WD-xquery-operators-20020816#dayTimeDuration"));
	}
	
}
