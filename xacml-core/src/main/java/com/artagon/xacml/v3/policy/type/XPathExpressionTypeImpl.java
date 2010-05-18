package com.artagon.xacml.v3.policy.type;


import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.policy.spi.type.BaseAttributeType;
import com.artagon.xacml.v3.policy.type.XPathExpressionType.XPathExpressionValue;
import com.google.common.base.Preconditions;

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
	
	@Override
	public XPathExpressionValue create(String xpath, AttributeCategoryId category) 
	{
		return new XPathExpressionValue(this, xpath, category);
	}
	
	@Override
	public XPathExpressionValue create(Object v, Object ... params) 
	{
		Preconditions.checkArgument(isConvertableFrom(v), 
				"Given instance=\"%s\" can not be converted to this type value", v);
		Preconditions.checkArgument(params != null && params.length > 0, 
				"XPath category must be specified");
		return new XPathExpressionValue(this, (String)v, (AttributeCategoryId)params[0]);
	}
	
	@Override
	public XPathExpressionValue fromXacmlString(String v, Object ...params) 
	{
		Preconditions.checkArgument(params != null && params.length > 0, "XPath category must be specified");
		return new XPathExpressionValue(this, v, 
				AttributeCategoryId.parse(String.valueOf(params[0])));
	}
}
