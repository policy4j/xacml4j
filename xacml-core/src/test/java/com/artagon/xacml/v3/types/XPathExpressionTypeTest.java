package com.artagon.xacml.v3.types;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.artagon.xacml.v3.AttributeCategories;

public class XPathExpressionTypeTest
{

	@Test
	public void testCreateXPathAttribute() throws Exception
	{
		XPathExpressionValue v = XPathExpressionType.XPATHEXPRESSION.create("/test", AttributeCategories.SUBJECT_RECIPIENT);
		assertEquals("/test", v.getValue());
		assertEquals(AttributeCategories.SUBJECT_RECIPIENT, v.getCategory());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testCreateXPathWithoutCategory()
	{
		XPathExpressionType.XPATHEXPRESSION.create("/test");
	}
	
	@Test(expected=ClassCastException.class)
	public void testCreateXPathWithCategoryAsString()
	{
		XPathExpressionType.XPATHEXPRESSION.create("/test", "test");
	}
}
