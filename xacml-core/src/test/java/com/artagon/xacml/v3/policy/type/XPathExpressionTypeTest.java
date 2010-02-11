package com.artagon.xacml.v3.policy.type;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.policy.type.XPathExpressionType.XPathExpressionValue;

public class XPathExpressionTypeTest 
{
	
	@Test
	public void testCreateXPathAttribute() throws XPathExpressionException
	{
		XPathExpressionType t = DataTypes.XPATHEXPRESSION.getType();
		XPathExpressionValue v = t.create("md:record/md:medical", AttributeCategoryId.RESOURCE);
	}
}
