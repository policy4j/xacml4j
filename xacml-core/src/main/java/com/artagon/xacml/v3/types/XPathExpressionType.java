package com.artagon.xacml.v3.types;

import java.util.Collection;

import com.artagon.xacml.v3.AttributeCategories;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.BagOfAttributeValuesType;
import com.artagon.xacml.v3.XacmlSyntaxException;
import com.google.common.base.Preconditions;

public enum XPathExpressionType implements AttributeValueType
{
	XPATHEXPRESSION("urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression");
	
	private String typeId;
	private BagOfAttributeValuesType bagType;
	
	private XPathExpressionType(String typeId){
		this.typeId = typeId;
		this.bagType = new BagOfAttributeValuesType(this);
	}
	
	public boolean isConvertableFrom(Object any) {
		return any instanceof String;
	}
	
	public XPathExpressionValue create(String xpath, AttributeCategories category) 
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
		return new XPathExpressionValue(this, (String)v, (AttributeCategories)params[0]);
	}
	
	@Override
	public XPathExpressionValue fromXacmlString(String v, Object ...params) 
	{
		Preconditions.checkArgument(params != null && params.length > 0, 
				"XPath category must be specified");
		try{
			AttributeCategories categoryId = AttributeCategories.parse(String.valueOf(params[0]));
			return new XPathExpressionValue(this, v, categoryId);
		}catch(XacmlSyntaxException e){
			throw new IllegalArgumentException(e);
		}
	}
	
	@Override
	public String getDataTypeId() {
		return typeId;
	}

	@Override
	public BagOfAttributeValuesType bagType() {
		return bagType;
	}

	@Override
	public BagOfAttributeValues bagOf(AttributeValue... values) {
		return bagType.create(values);
	}

	@Override
	public BagOfAttributeValues bagOf(Collection<AttributeValue> values) {
		return bagType.create(values);
	}

	@Override
	public BagOfAttributeValues emptyBag() {
		return bagType.createEmpty();
	}
	
	@Override
	public String toString(){
		return typeId;
	}
}
