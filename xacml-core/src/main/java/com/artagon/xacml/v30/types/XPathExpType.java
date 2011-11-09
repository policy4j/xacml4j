package com.artagon.xacml.v30.types;

import java.util.Collection;

import com.artagon.xacml.v30.AttributeCategories;
import com.artagon.xacml.v30.AttributeCategory;
import com.artagon.xacml.v30.pdp.AttributeExp;
import com.artagon.xacml.v30.pdp.AttributeExpType;
import com.artagon.xacml.v30.pdp.BagOfAttributeExp;
import com.artagon.xacml.v30.pdp.BagOfAttributeExpType;
import com.artagon.xacml.v30.pdp.XacmlSyntaxException;
import com.google.common.base.Preconditions;

public enum XPathExpType implements AttributeExpType
{
	XPATHEXPRESSION("urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression");
	
	private String typeId;
	private BagOfAttributeExpType bagType;
	
	private XPathExpType(String typeId){
		this.typeId = typeId;
		this.bagType = new BagOfAttributeExpType(this);
	}
	
	public boolean isConvertableFrom(Object any) {
		return (any instanceof String);
	}
	
	public XPathExp create(String xpath, AttributeCategories category) 
	{
		return new XPathExp(this, xpath, category);
	}
	
	@Override
	public XPathExp create(Object v, Object ... params) 
	{
		Preconditions.checkArgument(isConvertableFrom(v), 
				"Given instance=\"%s\" can not be converted to this type value", v);
		Preconditions.checkArgument(params != null && params.length > 0, 
				"XPath category must be specified");
		return new XPathExp(this, (String)v, (AttributeCategories)params[0]);
	}
	
	@Override
	public XPathExp fromXacmlString(String v, Object ...params) 
	{
		Preconditions.checkArgument(params != null && params.length > 0, 
				"XPath category must be specified");
		try{
			AttributeCategory categoryId = AttributeCategories.parse(String.valueOf(params[0]));
			return new XPathExp(this, v, categoryId);
		}catch(XacmlSyntaxException e){
			throw new IllegalArgumentException(e);
		}
	}
	
	@Override
	public String getDataTypeId() {
		return typeId;
	}

	@Override
	public BagOfAttributeExpType bagType() {
		return bagType;
	}

	@Override
	public BagOfAttributeExp bagOf(AttributeExp... values) {
		return bagType.create(values);
	}

	@Override
	public BagOfAttributeExp bagOf(Collection<AttributeExp> values) {
		return bagType.create(values);
	}
	
	@Override
	public BagOfAttributeExp bagOf(Object... values) {
		return bagType.bagOf(values);
	}

	@Override
	public BagOfAttributeExp emptyBag() {
		return bagType.createEmpty();
	}
	
	@Override
	public String toString(){
		return typeId;
	}
}
