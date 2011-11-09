package com.artagon.xacml.v30.types;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.artagon.xacml.v30.AttributeCategories;

public class XPathExpressionTypeTest
{

	@Test
	public void testCreateXPathAttribute() throws Exception
	{
		XPathExp v = XPathExpType.XPATHEXPRESSION.create("/test", AttributeCategories.SUBJECT_RECIPIENT);
		assertEquals("/test", v.toXacmlString());
		assertEquals(AttributeCategories.SUBJECT_RECIPIENT, v.getCategory());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testCreateXPathWithoutCategory()
	{
		XPathExpType.XPATHEXPRESSION.create("/test");
	}
	
	@Test(expected=ClassCastException.class)
	public void testCreateXPathWithCategoryAsString()
	{
		XPathExpType.XPATHEXPRESSION.create("/test", "test");
	}
}
