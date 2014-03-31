package org.xacml4j.v30.types;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.xacml4j.v30.Categories;


public class XPathExpressionTypeTest
{

	@Test
	public void testCreateXPathAttribute() throws Exception
	{
		XPathExp v = XPathExp.valueOf("/test", Categories.SUBJECT_RECIPIENT);
		assertEquals("/test", v.getPath());
		assertEquals(Categories.SUBJECT_RECIPIENT, v.getCategory());
	}
}
