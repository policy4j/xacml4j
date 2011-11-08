package com.artagon.xacml.v30.types;

import java.util.Collection;

import com.artagon.xacml.v30.AttributeExp;
import com.artagon.xacml.v30.AttributeExpType;
import com.artagon.xacml.v30.BagOfAttributesExp;
import com.artagon.xacml.v30.BagOfAttributesExpType;
import com.google.common.base.Preconditions;

public enum BooleanType implements AttributeExpType
{	
	BOOLEAN("http://www.w3.org/2001/XMLSchema#boolean");
		
	private BooleanValueExp FALSE;
	private BooleanValueExp TRUE;
	
	private String typeId;
	private BagOfAttributesExpType bagType;
	
	private BooleanType(String typeId){
		this.typeId = typeId;
		this.bagType = new BagOfAttributesExpType(this);
		this.FALSE = new BooleanValueExp(this, Boolean.FALSE);
		this.TRUE = new BooleanValueExp(this, Boolean.TRUE);
	}
	
	public boolean isConvertableFrom(Object any) {
		return Boolean.class.isInstance(any) || String.class.isInstance(any);
	}
	
	@Override
	public BooleanValueExp create(Object any, Object ...parameters){
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
	public BooleanValueExp fromXacmlString(String v, Object ...parameters) {
		Preconditions.checkNotNull(v);
		return Boolean.parseBoolean(v)?TRUE:FALSE;
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