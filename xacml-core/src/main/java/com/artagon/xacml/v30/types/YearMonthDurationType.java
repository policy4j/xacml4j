package com.artagon.xacml.v30.types;

import java.util.Collection;

import javax.xml.datatype.Duration;

import com.artagon.xacml.v30.AttributeExp;
import com.artagon.xacml.v30.AttributeExpType;
import com.artagon.xacml.v30.BagOfAttributeExp;
import com.artagon.xacml.v30.BagOfAttributeExpType;
import com.artagon.xacml.v30.YearMonthDuration;

public enum YearMonthDurationType implements AttributeExpType
{
	YEARMONTHDURATION("http://www.w3.org/2001/XMLSchema#yearMonthDuration");

	private String typeId;
	private BagOfAttributeExpType bagType;

	private YearMonthDurationType(String typeId)
	{
		this.typeId = typeId;
		this.bagType = new BagOfAttributeExpType(this);
	}

	public boolean isConvertableFrom(Object any) {
		return any instanceof Duration || any instanceof String || any instanceof YearMonthDuration;
	}

	@Override
	public YearMonthDurationExp fromXacmlString(String v, Object ...params)
	{
		return new YearMonthDurationExp(this, YearMonthDuration.create(v));
	}

	@Override
	public YearMonthDurationExp create(Object any, Object ...params){
		return new YearMonthDurationExp(this, YearMonthDuration.create(any));
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



