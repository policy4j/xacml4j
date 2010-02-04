package com.artagon.xacml.v3.policy.type;

import javax.xml.xpath.XPath;

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

	/**
	 * @throws UnsupportedOperationException
	 */
	public XPathExpressionValue create(Object v) {
		throw new UnsupportedOperationException("Type does not support this operation");
	}

	@Override
	public XPathExpressionValue create(XPath xpath,
			AttributeCategoryId category) {
		return new XPathExpressionValue(this, xpath, category);
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	public XPathExpressionValue fromXacmlString(String v) {
		throw new UnsupportedOperationException("Type does not support this operation");
	}
}
