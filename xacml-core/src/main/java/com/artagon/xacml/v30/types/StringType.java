package com.artagon.xacml.v30.types;

import java.util.Collection;

import com.artagon.xacml.v30.AttributeValue;
import com.artagon.xacml.v30.AttributeValueType;
import com.artagon.xacml.v30.BagOfAttributeValues;
import com.artagon.xacml.v30.BagOfAttributeValuesType;
import com.google.common.base.Preconditions;


public enum StringType implements AttributeValueType
{
	STRING("http://www.w3.org/2001/XMLSchema#string");
	
	private String typeId;
	private BagOfAttributeValuesType bagType;
	
	private StringType(String typeId){
		this.typeId = typeId;
		this.bagType = new BagOfAttributeValuesType(this);
	}
	
	@Override
	public boolean isConvertableFrom(Object any) {
		return StringValue.class.isInstance(any) || String.class.isInstance(any);
	}

	@Override
	public StringValue fromXacmlString(String v, Object ...params) {
		return create(v);
	}
	
	@Override
	public StringValue create(Object any, Object ...params){
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertableFrom(any), String.format(
				"Value=\"%s\" of class=\"%s\" can't ne converted to XACML \"string\" type", 
				any, any.getClass()));
		return new StringValue(this, (String)any);
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