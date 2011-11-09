package com.artagon.xacml.v30.types;

import java.util.Collection;
import java.util.GregorianCalendar;

import javax.xml.datatype.XMLGregorianCalendar;

import com.artagon.xacml.v30.AttributeExp;
import com.artagon.xacml.v30.AttributeExpType;
import com.artagon.xacml.v30.BagOfAttributeExp;
import com.artagon.xacml.v30.BagOfAttributeExpType;
import com.artagon.xacml.v30.core.Time;

public enum TimeType implements AttributeExpType
{	
	TIME("http://www.w3.org/2001/XMLSchema#time");
	
	private String typeId;
	private BagOfAttributeExpType bagType;

	private TimeType(String typeId) 
	{
		this.typeId = typeId;
		this.bagType = new BagOfAttributeExpType(this);
	}
	
	public boolean isConvertableFrom(Object any) {
		return XMLGregorianCalendar.class.isInstance(any) || String.class.isInstance(any) ||
		GregorianCalendar.class.isInstance(any);
	}
	
	@Override
	public TimeExp fromXacmlString(String v, Object ...params) {
		return new TimeExp(this, Time.create(v));
	}
	
	@Override
	public TimeExp create(Object any, Object ...params){
		return new TimeExp(this, Time.create(any));
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


