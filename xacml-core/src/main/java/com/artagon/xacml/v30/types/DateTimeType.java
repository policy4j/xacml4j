package com.artagon.xacml.v30.types;

import java.util.Collection;
import java.util.GregorianCalendar;

import javax.xml.datatype.XMLGregorianCalendar;

import com.artagon.xacml.v30.AttributeExp;
import com.artagon.xacml.v30.AttributeExpType;
import com.artagon.xacml.v30.BagOfAttributesExp;
import com.artagon.xacml.v30.BagOfAttributesExpType;
import com.artagon.xacml.v30.core.DateTime;

public enum DateTimeType implements AttributeExpType 
{
	DATETIME("http://www.w3.org/2001/XMLSchema#dateTime");

	private String typeId;
	private BagOfAttributesExpType bagType;

	private DateTimeType(String typeId) {
		this.typeId = typeId;
		this.bagType = new BagOfAttributesExpType(this);
	}

	public boolean isConvertableFrom(Object any) {
		return XMLGregorianCalendar.class.isInstance(any)
				|| String.class.isInstance(any)
				|| GregorianCalendar.class.isInstance(any);
	}

	@Override
	public DateTimeValueExp fromXacmlString(String v, Object... params) {
		return new DateTimeValueExp(this, DateTime.create(v));
	}

	@Override
	public DateTimeValueExp create(Object any, Object... params) {		
		return new DateTimeValueExp(this, DateTime.create(any));
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