package com.artagon.xacml.v3.policy.type;

import javax.xml.xpath.XPathExpression;

public interface XPathExpressionType 
{
	XPathExpression create(Object v);
	XPathExpression fromXacmlString(String v);
}
