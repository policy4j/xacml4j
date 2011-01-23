package com.artagon.xacml.v30.types;

import java.util.Collection;

import com.artagon.xacml.v30.AttributeValue;
import com.artagon.xacml.v30.AttributeValueType;
import com.artagon.xacml.v30.BagOfAttributeValues;
import com.artagon.xacml.v30.BagOfAttributeValuesType;
import com.google.common.base.Preconditions;

public enum IntegerType implements AttributeValueType
{
	INTEGER("http://www.w3.org/2001/XMLSchema#integer");
	
	private String typeId;
	private BagOfAttributeValuesType bagType;
	
	private IntegerType(String typeId){
		this.typeId = typeId;
		this.bagType = new BagOfAttributeValuesType(this);
	}
	
	@Override
	public boolean isConvertableFrom(Object any) {
		return Long.class.isInstance(any) || Integer.class.isInstance(any) ||
		Short.class.isInstance(any) || Byte.class.isInstance(any) ||
		String.class.isInstance(any);
	}


	@Override
	public IntegerValue create(Object any, Object ...params){
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertableFrom(any), String.format(
				"Value=\"%s\" of class=\"%s\" can't ne converted to XACML \"integer\" type", 
				any, any.getClass()));
		if(String.class.isInstance(any)){
			return fromXacmlString((String)any);
		}
		if(Byte.class.isInstance(any)){
			return new IntegerValue(this, ((Byte)any).longValue());
		}
		if(Short.class.isInstance(any)){
			return new IntegerValue(this, ((Short)any).longValue());
		}
		if(Integer.class.isInstance(any)){
			return new IntegerValue(this, ((Integer)any).longValue());
		}
		return new IntegerValue(this, (Long)any);
	}

	@Override
	public IntegerValue fromXacmlString(String v, Object ...params) {
        Preconditions.checkNotNull(v);
		if ((v.length() >= 1) && 
        		(v.charAt(0) == '+')){
			v = v.substring(1);
		}
        return new IntegerValue(this, Long.valueOf(v));
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