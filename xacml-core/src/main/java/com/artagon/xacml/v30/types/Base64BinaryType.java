package com.artagon.xacml.v30.types;

import java.util.Collection;

import com.artagon.xacml.v30.AttributeExp;
import com.artagon.xacml.v30.AttributeExpType;
import com.artagon.xacml.v30.BagOfAttributesExp;
import com.artagon.xacml.v30.BagOfAttributesExpType;
import com.artagon.xacml.v30.core.BinaryValue;
import com.google.common.base.Preconditions;

public enum Base64BinaryType implements AttributeExpType
{	
	BASE64BINARY("http://www.w3.org/2001/XMLSchema#base64Binary");
	
	private String typeId;
	private BagOfAttributesExpType bagType;
	
	private Base64BinaryType(String typeId){
		this.typeId = typeId;
		this.bagType = new BagOfAttributesExpType(this);
	}
	
	private boolean isConvertableFrom(Object any) {
		return byte[].class.isInstance(any) || String.class.isInstance(any) ||
				BinaryValue.class.isInstance(any);
	}
	
	@Override
	public Base64BinaryValueExp create(Object any, Object ...params){
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertableFrom(any), String.format(
				"Value=\"%s\" of class=\"%s\" can not be" +
				" converted to XACML \"base64binary\" type", 
				any, any.getClass()));
		if(String.class.isInstance(any)){
			return fromXacmlString((String)any);
		}
		if(byte[].class.isInstance(any)){
			return new Base64BinaryValueExp(new BinaryValue((byte[])any));
		}
		return new Base64BinaryValueExp((BinaryValue)any);
	}

	@Override
	public Base64BinaryValueExp fromXacmlString(String v, Object ...params) {
		Preconditions.checkNotNull(v);
		return new Base64BinaryValueExp(BinaryValue.fromBase64Encoded(v));
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
