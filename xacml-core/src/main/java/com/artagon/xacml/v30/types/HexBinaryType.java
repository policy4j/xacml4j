package com.artagon.xacml.v30.types;

import java.util.Collection;

import com.artagon.xacml.v30.AttributeExp;
import com.artagon.xacml.v30.AttributeExpType;
import com.artagon.xacml.v30.BagOfAttributesExp;
import com.artagon.xacml.v30.BagOfAttributesExpType;
import com.artagon.xacml.v30.core.BinaryValue;
import com.google.common.base.Preconditions;

public enum HexBinaryType implements AttributeExpType
{
	HEXBINARY("http://www.w3.org/2001/XMLSchema#hexBinary");

	private String typeId;
	private BagOfAttributesExpType bagType;
	
	private HexBinaryType(String typeId){
		this.typeId = typeId;
		this.bagType = new BagOfAttributesExpType(this);
	}
	
	private boolean isConvertableFrom(Object any) {
		return byte[].class.isInstance(any) || String.class.isInstance(any) 
				|| BinaryValue.class.isInstance(any);
	}
	
	@Override
	public HexBinaryValueExp create(Object any, Object ...params){
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertableFrom(any), String.format(
				"Value=\"%s\" of class=\"%s\" can't ne converted to XACML \"hexBinary\" type", 
				any, any.getClass()));
		if(String.class.isInstance(any)){
			return fromXacmlString((String)any);
		}
		if(byte[].class.isInstance(any)){
			return new HexBinaryValueExp(this, new BinaryValue((byte[])any));
		}
		return new HexBinaryValueExp(this, (BinaryValue)any);
	}

	@Override
	public HexBinaryValueExp fromXacmlString(String v, Object ...params) {
		return new HexBinaryValueExp(this, BinaryValue.fromHexEncoded(v));
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
