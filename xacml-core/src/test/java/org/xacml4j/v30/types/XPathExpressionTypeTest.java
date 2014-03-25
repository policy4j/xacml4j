package org.xacml4j.v30.types;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.xacml4j.v30.AttributeCategories;


public class XPathExpressionTypeTest
{

	@Test
	public void testCreateXPathAttribute() throws Exception
	{
		XPathExp v = XPathExp.valueOf("/test", AttributeCategories.SUBJECT_RECIPIENT);
		assertEquals("/test", v.getPath());
		assertEquals(AttributeCategories.SUBJECT_RECIPIENT, v.getCategory());
	}
}
