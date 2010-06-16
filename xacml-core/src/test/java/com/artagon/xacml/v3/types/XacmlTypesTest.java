package com.artagon.xacml.v3.types;

import org.junit.Test;
import static org.junit.Assert.assertNotNull;

public class XacmlTypesTest
{
	@Test
	public void testXACML20DeprecatedTypeMapping()
	{
		assertNotNull(XacmlDataTypes.getByTypeId("http://www.w3.org/TR/2002/WD-xquery-operators-20020816#yearMonthDuration"));
		assertNotNull(XacmlDataTypes.getByTypeId("urn:oasis:names:tc:xacml:2.0:data-type:xpath-expression"));
		assertNotNull(XacmlDataTypes.getByTypeId("http://www.w3.org/TR/2002/WD-xquery-operators-20020816#dayTimeDuration"));
	}
	
}
