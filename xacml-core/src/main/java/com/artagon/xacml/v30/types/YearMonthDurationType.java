package com.artagon.xacml.v30.types;

import java.util.Collection;

import javax.xml.datatype.Duration;

import com.artagon.xacml.v30.AttributeExp;
import com.artagon.xacml.v30.AttributeExpType;
import com.artagon.xacml.v30.BagOfAttributesExp;
import com.artagon.xacml.v30.BagOfAttributesExpType;
import com.artagon.xacml.v30.core.YearMonthDuration;

public enum YearMonthDurationType implements AttributeExpType
{
	YEARMONTHDURATION("http://www.w3.org/2001/XMLSchema#yearMonthDuration");
	
	private String typeId;
	private BagOfAttributesExpType bagType;

	private YearMonthDurationType(String typeId) 
	{
		this.typeId = typeId;
		this.bagType = new BagOfAttributesExpType(this);
	}
	
	public boolean isConvertableFrom(Object any) {
		return any instanceof Duration || any instanceof String || any instanceof YearMonthDuration;
	}

	@Override
	public YearMonthDurationValueExp fromXacmlString(String v, Object ...params) 
	{
		return new YearMonthDurationValueExp(this, YearMonthDuration.create(v));
	}
	
	@Override
	public YearMonthDurationValueExp create(Object any, Object ...params){
		return new YearMonthDurationValueExp(this, YearMonthDuration.create(any));
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



