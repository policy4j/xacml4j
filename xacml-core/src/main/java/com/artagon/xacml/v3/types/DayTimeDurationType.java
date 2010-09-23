package com.artagon.xacml.v3.types;

import java.util.Collection;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.BagOfAttributeValuesType;
import com.google.common.base.Preconditions;


public enum DayTimeDurationType implements AttributeValueType
{
	DAYTIMEDURATION("http://www.w3.org/2001/XMLSchema#dayTimeDuration");
	
	private DatatypeFactory xmlDataTypesFactory;

	private String typeId;
	private BagOfAttributeValuesType bagType;

	private DayTimeDurationType(String typeId) 
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
		return Duration.class.isInstance(any) || String.class.isInstance(any);
	}

	@Override
	public DayTimeDurationValue fromXacmlString(String v, Object ...params) {
		Preconditions.checkNotNull(v);
		Duration dayTimeDuration = xmlDataTypesFactory.newDurationDayTime(v);
		return new DayTimeDurationValue(this, validate(dayTimeDuration));
	}
	
	@Override
	public DayTimeDurationValue create(Object any, Object ...params){
		Preconditions.checkNotNull(any);
		Preconditions.checkArgument(isConvertableFrom(any), String.format(
				"Value=\"%s\" of class=\"%s\" " +
				"can't ne converted to XACML \"date\" type", 
				any, any.getClass()));
		if(String.class.isInstance(any)){
			return fromXacmlString((String)any);
		}
		return new DayTimeDurationValue(this, validate((Duration)any));
	}
	
	private Duration validate(Duration duration){
		if(!(duration.isSet(DatatypeConstants.YEARS) 
				&& duration.isSet(DatatypeConstants.MONTHS))){
			return duration;
		}
		throw new IllegalArgumentException();
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
	public BagOfAttributeValues emptyBag() {
		return bagType.createEmpty();
	}
}

