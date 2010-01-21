package com.artagon.xacml.policy.type;

import javax.xml.xpath.XPathExpression;

import com.artagon.xacml.policy.BagOfAttributesType;
import com.artagon.xacml.policy.type.RFC822NameType.RFC822NameValue;

public interface XPathExpressionType 
{
	XPathExpression create(Object v);
	XPathExpression fromXacmlString(String v);
}
