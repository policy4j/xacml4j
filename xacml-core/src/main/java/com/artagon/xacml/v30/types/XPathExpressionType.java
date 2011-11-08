package com.artagon.xacml.v30.types;

import java.util.Collection;

import com.artagon.xacml.v30.AttributeExp;
import com.artagon.xacml.v30.AttributeExpType;
import com.artagon.xacml.v30.BagOfAttributesExp;
import com.artagon.xacml.v30.BagOfAttributesExpType;
import com.artagon.xacml.v30.XacmlSyntaxException;
import com.artagon.xacml.v30.core.AttributeCategories;
import com.artagon.xacml.v30.core.AttributeCategory;
import com.google.common.base.Preconditions;

public enum XPathExpressionType implements AttributeExpType
{
	XPATHEXPRESSION("urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression");
	
	private String typeId;
	private BagOfAttributesExpType bagType;
	
	private XPathExpressionType(String typeId){
		this.typeId = typeId;
		this.bagType = new BagOfAttributesExpType(this);
	}
	
	public boolean isConvertableFrom(Object any) {
		return (any instanceof String);
	}
	
	public XPathExpressionValueExp create(String xpath, AttributeCategories category) 
	{
		return new XPathExpressionValueExp(this, xpath, category);
	}
	
	@Override
	public XPathExpressionValueExp create(Object v, Object ... params) 
	{
		Preconditions.checkArgument(isConvertableFrom(v), 
				"Given instance=\"%s\" can not be converted to this type value", v);
		Preconditions.checkArgument(params != null && params.length > 0, 
				"XPath category must be specified");
		return new XPathExpressionValueExp(this, (String)v, (AttributeCategories)params[0]);
	}
	
	@Override
	public XPathExpressionValueExp fromXacmlString(String v, Object ...params) 
	{
		Preconditions.checkArgument(params != null && params.length > 0, 
				"XPath category must be specified");
		try{
			AttributeCategory categoryId = AttributeCategories.parse(String.valueOf(params[0]));
			return new XPathExpressionValueExp(this, v, categoryId);
		}catch(XacmlSyntaxException e){
			throw new IllegalArgumentException(e);
		}
	}
	
	@Override
	public String getDataTypeId() {
		return typeId;
	}

	@Override
	public BagOfAttributesExpType bagType() {
		return bagType;
	}

	@Override
	public BagOfAttributesExp bagOf(AttributeExp... values) {
		return bagType.create(values);
	}

	@Override
	public BagOfAttributesExp bagOf(Collection<AttributeExp> values) {
		return bagType.create(values);
	}
	
	@Override
	public BagOfAttributesExp bagOf(Object... values) {
		return bagType.bagOf(values);
	}

	@Override
	public BagOfAttributesExp emptyBag() {
		return bagType.createEmpty();
	}
	
	@Override
	public String toString(){
		return typeId;
	}
}
