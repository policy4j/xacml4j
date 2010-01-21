package com.artagon.xacml.policy.type;

import javax.xml.xpath.XPathExpression;

public interface XPathExpressionType 
{
	XPathExpression create(Object v);
	XPathExpression fromXacmlString(String v);
}
