package com.artagon.xacml.v30.types;

import java.util.Collection;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import com.artagon.xacml.v30.AttributeValue;
import com.artagon.xacml.v30.AttributeValueType;
import com.artagon.xacml.v30.BagOfAttributeValues;
import com.artagon.xacml.v30.BagOfAttributeValuesType;
import com.google.common.base.Preconditions;

public enum YearMonthDurationType implements AttributeValueType
{
	YEARMONTHDURATION("http://www.w3.org/2001/XMLSchema#yearMonthDuration");

	private DatatypeFactory xmlDataTypesFactory;
	
	private String typeId;
	private BagOfAttributeValuesType bagType;

	private YearMonthDurationType(String typeId) 
	{
		this.typeId = typeId;
		this.bagType = new BagOfAttributeValuesType(this);
		try {
			this.xmlDataTypesFactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace(System.err);
		}
	}
	
	@Override
	public boolean isConvertableFrom(Object any) {
		return any instanceof Duration || any instanceof String;
	}

	@Override
	public YearMonthDurationValue fromXacmlString(String v, Object ...params) 
	{
		Preconditions.checkNotNull(v);
		Duration yearMonthDuration = xmlDataTypesFactory.newDurationYearMonth(v);
		return new YearMonthDurationValue(this, validate(yearMonthDuration));
	}
	
	@Override
	public YearMonthDurationValue create(Object any, Object ...params){
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertableFrom(any), String.format(
				"Value=\"%s\" of class=\"%s\" " +
				"can't ne converted to XACML \"date\" type", 
				any, any.getClass()));
		if(String.class.isInstance(any)){
			return fromXacmlString((String)any);
		}
		return new YearMonthDurationValue(this, validate((Duration)any));
	}
	
	private Duration validate(Duration duration)
	{
		if(!(duration.isSet(DatatypeConstants.DAYS) 
				|| duration.isSet(DatatypeConstants.HOURS) 
				|| duration.isSet(DatatypeConstants.MINUTES) 
				|| duration.isSet(DatatypeConstants.SECONDS))){
			return duration;
		}
		throw new IllegalArgumentException("Invalid duration");
	}
	
	@Override
	public String getDataTypeId() {
		return typeId;
	}

	@Override
	public BagOfAttributeValuesType bagType() {
		return bagType;
	}

	@Override
	public BagOfAttributeValues bagOf(AttributeValue... values) {
		return bagType.create(values);
	}

	@Override
	public BagOfAttributeValues bagOf(Collection<AttributeValue> values) {
		return bagType.create(values);
	}
	
	@Override
	public BagOfAttributeValues bagOf(Object... values) {
		return bagType.bagOf(values);
	}
	
	@Override
	public BagOfAttributeValues emptyBag() {
		return bagType.createEmpty();
	}
	
	@Override
	public String toString(){
		return typeId;
	}
}



