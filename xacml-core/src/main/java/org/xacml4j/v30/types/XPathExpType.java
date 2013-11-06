package org.xacml4j.v30.types;

import java.util.Collection;

import org.xacml4j.v30.AttributeCategories;
import org.xacml4j.v30.AttributeCategory;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.BagOfAttributeExpType;
import org.xacml4j.v30.XacmlSyntaxException;

import com.google.common.base.Preconditions;

public enum XPathExpType implements AttributeExpType
{
	XPATHEXPRESSION("urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression");

	private final String typeId;
	private final BagOfAttributeExpType bagType;

	private XPathExpType(String typeId){
		this.typeId = typeId;
		this.bagType = new BagOfAttributeExpType(this);
	}

	public boolean isConvertibleFrom(Object any) {
		return (any instanceof String);
	}

	public XPathExp create(String xpath, AttributeCategories category) {
		return new XPathExp(this, xpath, category);
	}

	@Override
	public XPathExp create(Object any, Object ... params)
	{
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertibleFrom(any),
				"Value=\"%s\" of type=\"%s\" can't be converted to XACML \"%s\" type",
				any, any.getClass(), typeId);
		Preconditions.checkArgument(params != null && params.length > 0,
				"XPath category must be specified");
		return new XPathExp(this, (String) any, (AttributeCategories)params[0]);
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
	public BagOfAttributeExp.Builder bag(){
		return new BagOfAttributeExp.Builder(this);
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
