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
	{
		XPathFactory factory = XPathFactory.newInstance();
		XPath xp =  factory.newXPath();
		try
		{
			XPathExpression xexp =  xp.compile(xpath);
			return new XPathExpressionValue(this, xexp, category);
		}catch(XPathExpressionException e){
			return null;
		}
	}
	
	/**
	 * @throws UnsupportedOperationException
	 */
	public XPathExpressionValue create(Object v, Object ... params) {
		return null;
	}
	
	/**
	 * @throws UnsupportedOperationException
	 */
	public XPathExpressionValue fromXacmlString(String v, Object ...params) {
		return null;
	}
}
