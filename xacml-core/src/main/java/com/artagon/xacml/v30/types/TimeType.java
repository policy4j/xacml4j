package com.artagon.xacml.v30.types;

import java.util.Collection;
import java.util.GregorianCalendar;

import javax.xml.datatype.XMLGregorianCalendar;

import com.artagon.xacml.v30.AttributeExp;
import com.artagon.xacml.v30.AttributeExpType;
import com.artagon.xacml.v30.BagOfAttributesExp;
import com.artagon.xacml.v30.BagOfAttributesExpType;
import com.artagon.xacml.v30.core.Time;

public enum TimeType implements AttributeExpType
{	
	TIME("http://www.w3.org/2001/XMLSchema#time");
	
	private String typeId;
	private BagOfAttributesExpType bagType;

	private TimeType(String typeId) 
	{
		this.typeId = typeId;
		this.bagType = new BagOfAttributesExpType(this);
	}
	
	public boolean isConvertableFrom(Object any) {
		return XMLGregorianCalendar.class.isInstance(any) || String.class.isInstance(any) ||
		GregorianCalendar.class.isInstance(any);
	}
	
	@Override
	public TimeValueExp fromXacmlString(String v, Object ...params) {
		return new TimeValueExp(this, Time.create(v));
	}
	
	@Override
	public TimeValueExp create(Object any, Object ...params){
		return new TimeValueExp(this, Time.create(any));
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


