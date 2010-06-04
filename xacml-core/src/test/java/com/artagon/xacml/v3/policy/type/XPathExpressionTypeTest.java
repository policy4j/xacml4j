package com.artagon.xacml.v3.policy.type;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.artagon.xacml.v3.types.XPathExpressionType.XPathExpressionValue;

public class XPathExpressionTypeTest
{

	@Test
	public void testCreateXPathAttribute() throws Exception
	{
		XPathExpressionValue v = XacmlDataTypes.XPATHEXPRESSION.create("/test", AttributeCategoryId.SUBJECT_RECIPIENT);
		assertEquals("/test", v.getValue());
		assertEquals(AttributeCategoryId.SUBJECT_RECIPIENT, v.getAttributeCategory());
	}
}
