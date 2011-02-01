package com.artagon.xacml.v30.types;

import java.util.Collection;

import com.artagon.xacml.v30.AttributeValue;
import com.artagon.xacml.v30.AttributeValueType;
import com.artagon.xacml.v30.BagOfAttributeValues;
import com.artagon.xacml.v30.BagOfAttributeValuesType;
import com.google.common.base.Preconditions;

public enum BooleanType implements AttributeValueType
{	
	BOOLEAN("http://www.w3.org/2001/XMLSchema#boolean");
		
	private BooleanValue FALSE;
	private BooleanValue TRUE;
	
	private String typeId;
	private BagOfAttributeValuesType bagType;
	
	private BooleanType(String typeId){
		this.typeId = typeId;
		this.bagType = new BagOfAttributeValuesType(this);
		this.FALSE = new BooleanValue(this, Boolean.FALSE);
		this.TRUE = new BooleanValue(this, Boolean.TRUE);
	}
	
	@Override
	public boolean isConvertableFrom(Object any) {
		return Boolean.class.isInstance(any) || String.class.isInstance(any);
	}
	
	@Override
	public BooleanValue create(Object any, Object ...parameters){
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertableFrom(any),String.format(
				"Value=\"%s\" of class=\"%s\" can't ne converted to XACML \"boolean\" type", 
				any, any.getClass()));
		if(String.class.isInstance(any)){
			return fromXacmlString((String)any);
		}
		return ((Boolean)any)?TRUE:FALSE;
	}

	@Override
	public BooleanValue fromXacmlString(String v, Object ...parameters) {
		Preconditions.checkNotNull(v);
		return Boolean.parseBoolean(v)?TRUE:FALSE;
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
	public BagOfAttributeValues bagOf(Object... values) {
		return bagType.bagOf(values);
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