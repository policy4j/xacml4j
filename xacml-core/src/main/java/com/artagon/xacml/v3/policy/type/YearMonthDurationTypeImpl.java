package com.artagon.xacml.v3.policy.type;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import com.artagon.xacml.v3.policy.spi.type.BaseAttributeType;
import com.artagon.xacml.v3.policy.type.YearMonthDurationType.YearMonthDurationValue;
import com.google.common.base.Preconditions;

final class YearMonthDurationTypeImpl extends 
	BaseAttributeType<YearMonthDurationValue> implements YearMonthDurationType
{
	private DatatypeFactory xmlDataTypesFactory;
	
	public YearMonthDurationTypeImpl(String typeId)
	{
		super(typeId);
		try{
			this.xmlDataTypesFactory = DatatypeFactory.newInstance();
		}catch(DatatypeConfigurationException e){
			throw new IllegalStateException(e);
		}
	}
	
	@Override
	public boolean isConvertableFrom(Object any) {
		return any instanceof Duration || any instanceof String;
	}

	@Override
	public YearMonthDurationValue fromXacmlString(String v, Object ...params) {
		Preconditions.checkNotNull(v);
		Duration dayTimeDuration = xmlDataTypesFactory.newDurationDayTime(v);
		return new YearMonthDurationValue(this, validate(dayTimeDuration));
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
	
	private Duration validate(Duration duration){
		if(!(duration.isSet(DatatypeConstants.DAYS) 
				&& duration.isSet(DatatypeConstants.HOURS)) &&
				duration.isSet(DatatypeConstants.MINUTES) &&
				duration.isSet(DatatypeConstants.SECONDS)){
			return duration;
		}
		throw new IllegalArgumentException();
	}
}
