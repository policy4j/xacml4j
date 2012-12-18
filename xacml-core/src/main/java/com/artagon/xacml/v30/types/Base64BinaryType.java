package com.artagon.xacml.v30.types;

import java.util.Collection;

import com.artagon.xacml.v30.AttributeExp;
import com.artagon.xacml.v30.AttributeExpType;
import com.artagon.xacml.v30.BagOfAttributeExp;
import com.artagon.xacml.v30.BagOfAttributeExpType;
import com.artagon.xacml.v30.BinaryValue;
import com.google.common.base.Preconditions;

public enum Base64BinaryType implements AttributeExpType
{
	BASE64BINARY("http://www.w3.org/2001/XMLSchema#base64Binary");

	private String typeId;
	private BagOfAttributeExpType bagType;

	private Base64BinaryType(String typeId){
		this.typeId = typeId;
		this.bagType = new BagOfAttributeExpType(this);
	}

	private boolean isConvertableFrom(Object any) {
		return byte[].class.isInstance(any) || String.class.isInstance(any) ||
				BinaryValue.class.isInstance(any);
	}

	@Override
	public Base64BinaryExp create(Object any, Object ...params){
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertableFrom(any), String.format(
				"Value=\"%s\" of class=\"%s\" can not be" +
				" converted to XACML \"base64binary\" type",
				any, any.getClass()));
		if(String.class.isInstance(any)){
			return fromXacmlString((String)any);
		}
		if(byte[].class.isInstance(any)){
			return new Base64BinaryExp(BinaryValue.fromBytes((byte[])any));
		}
		return new Base64BinaryExp((BinaryValue)any);
	}

	@Override
	public Base64BinaryExp fromXacmlString(String v, Object ...params) {
		Preconditions.checkNotNull(v);
		return new Base64BinaryExp(BinaryValue.fromBase64Encoded(v));
	}

	@Override
	public String getDataTypeId() {
		return typeId;
	}

	@Override
	public BagOfAttributeExpType bagType() {
		return bagType;
	}

	public BagOfAttributeExp.Builder bag(){
		return new BagOfAttributeExp.Builder(this);
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
