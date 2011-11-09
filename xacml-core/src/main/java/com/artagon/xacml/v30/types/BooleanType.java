package com.artagon.xacml.v30.types;

import java.util.Collection;

import com.artagon.xacml.v30.AttributeExp;
import com.artagon.xacml.v30.AttributeExpType;
import com.artagon.xacml.v30.BagOfAttributeExp;
import com.artagon.xacml.v30.BagOfAttributeExpType;
import com.google.common.base.Preconditions;

public enum BooleanType implements AttributeExpType
{	
	BOOLEAN("http://www.w3.org/2001/XMLSchema#boolean");
		
	private BooleanExp FALSE;
	private BooleanExp TRUE;
	
	private String typeId;
	private BagOfAttributeExpType bagType;
	
	private BooleanType(String typeId){
		this.typeId = typeId;
		this.bagType = new BagOfAttributeExpType(this);
		this.FALSE = new BooleanExp(this, Boolean.FALSE);
		this.TRUE = new BooleanExp(this, Boolean.TRUE);
	}
	
	public boolean isConvertableFrom(Object any) {
		return Boolean.class.isInstance(any) || String.class.isInstance(any);
	}
	
	@Override
	public BooleanExp create(Object any, Object ...parameters){
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
	public BooleanExp fromXacmlString(String v, Object ...parameters) {
		Preconditions.checkNotNull(v);
		return Boolean.parseBoolean(v)?TRUE:FALSE;
	}
	
	@Override
	public String getDataTypeId() {
		return typeId;
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