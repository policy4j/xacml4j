package com.artagon.xacml.v30.types;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.artagon.xacml.v30.core.AttributeCategories;

public class XPathExpressionTypeTest
{

	@Test
	public void testCreateXPathAttribute() throws Exception
	{
		XPathExpressionValueExp v = XPathExpressionType.XPATHEXPRESSION.create("/test", AttributeCategories.SUBJECT_RECIPIENT);
		assertEquals("/test", v.toXacmlString());
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
