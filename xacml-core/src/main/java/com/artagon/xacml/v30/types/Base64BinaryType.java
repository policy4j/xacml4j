package com.artagon.xacml.v30.types;

import java.util.Collection;

import com.artagon.xacml.util.Base64;
import com.artagon.xacml.util.Base64DecoderException;
import com.artagon.xacml.v30.AttributeValue;
import com.artagon.xacml.v30.AttributeValueType;
import com.artagon.xacml.v30.BagOfAttributeValues;
import com.artagon.xacml.v30.BagOfAttributeValuesType;
import com.google.common.base.Preconditions;

public enum Base64BinaryType implements AttributeValueType
{	
	BASE64BINARY("http://www.w3.org/2001/XMLSchema#base64Binary");
	
	private String typeId;
	private BagOfAttributeValuesType bagType;
	
	private Base64BinaryType(String typeId){
		this.typeId = typeId;
		this.bagType = new BagOfAttributeValuesType(this);
	}
	
	@Override
	public boolean isConvertableFrom(Object any) {
		return byte[].class.isInstance(any) || String.class.isInstance(any);
	}
	
	@Override
	public Base64BinaryValue create(Object any, Object ...params){
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertableFrom(any), String.format(
				"Value=\"%s\" of class=\"%s\" can't ne converted to XACML \"hexBinary\" type", 
				any, any.getClass()));
		if(String.class.isInstance(any)){
			return fromXacmlString((String)any);
		}
		if(byte[].class.isInstance(any)){
			return new Base64BinaryValue(new BinaryValue((byte[])any));
		}
		return new Base64BinaryValue((BinaryValue)any);
	}

	@Override
	public Base64BinaryValue fromXacmlString(String v, Object ...params) {
		Preconditions.checkNotNull(v);
		try{
			return create(Base64.decode(v));
		}catch(Base64DecoderException e){
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
