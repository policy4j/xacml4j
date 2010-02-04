package com.artagon.xacml.v3.policy.type;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.policy.type.XPathExpressionType.XPathExpressionValue;

public class XPathExpressionTypeImpl extends BaseAttributeType<XPathExpressionValue> 
	implements XPathExpressionType
{
	public XPathExpressionTypeImpl(String typeId) {
		super(typeId);
	}
	
	@Override
	public boolean isConvertableFrom(Object any) {
		return any instanceof String;
	}
	
	public XPathExpressionValue create(String xpath, AttributeCategoryId category) 
		throws XPathExpressionException
	{
		XPathFactory factory = XPathFactory.newInstance();
		XPath xp =  factory.newXPath();
		XPathExpression xexp =  xp.compile(xpath);
		return new XPathExpressionValue(this, xexp, category);
	}
	
	/**
	 * @throws UnsupportedOperationException
	 */
	public XPathExpressionValue create(Object v) {
		throw new UnsupportedOperationException("Type does not support this operation");
	}
	
	/**
	 * @throws UnsupportedOperationException
	 */
	public XPathExpressionValue fromXacmlString(String v) {
		throw new UnsupportedOperationException("Type does not support this operation");
	}
}
