package com.artagon.xacml.v30.types;

import java.util.Collection;

import com.artagon.xacml.v30.AttributeExp;
import com.artagon.xacml.v30.AttributeExpType;
import com.artagon.xacml.v30.BagOfAttributesExp;
import com.artagon.xacml.v30.BagOfAttributesExpType;
import com.google.common.base.Preconditions;

public enum IntegerType implements AttributeExpType
{
	INTEGER("http://www.w3.org/2001/XMLSchema#integer");
	
	private String typeId;
	private BagOfAttributesExpType bagType;
	
	private IntegerType(String typeId){
		this.typeId = typeId;
		this.bagType = new BagOfAttributesExpType(this);
	}
	
	private boolean isConvertableFrom(Object any) {
		return Long.class.isInstance(any) || Integer.class.isInstance(any) ||
		Short.class.isInstance(any) || Byte.class.isInstance(any) ||
		String.class.isInstance(any);
	}


	@Override
	public IntegerValueExp create(Object any, Object ...params){
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertableFrom(any), String.format(
				"Value=\"%s\" of class=\"%s\" can't ne converted to XACML \"integer\" type", 
				any, any.getClass()));
		if(String.class.isInstance(any)){
			return fromXacmlString((String)any);
		}
		if(Byte.class.isInstance(any)){
			return new IntegerValueExp(this, ((Byte)any).longValue());
		}
		if(Short.class.isInstance(any)){
			return new IntegerValueExp(this, ((Short)any).longValue());
		}
		if(Integer.class.isInstance(any)){
			return new IntegerValueExp(this, ((Integer)any).longValue());
		}
		return new IntegerValueExp(this, (Long)any);
	}

	@Override
	public IntegerValueExp fromXacmlString(String v, Object ...params) {
        Preconditions.checkNotNull(v);
		if ((v.length() >= 1) && 
        		(v.charAt(0) == '+')){
			v = v.substring(1);
		}
		return new IntegerValueExp(this, Long.valueOf(v));
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