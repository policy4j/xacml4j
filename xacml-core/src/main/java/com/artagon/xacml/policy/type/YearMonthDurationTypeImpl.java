package com.artagon.xacml.policy.type;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import com.artagon.xacml.policy.BaseAttributeDataType;
import com.artagon.xacml.policy.type.YearMonthDurationType.YearMonthDurationValue;
import com.artagon.xacml.util.Preconditions;

final class YearMonthDurationTypeImpl extends 
	BaseAttributeDataType<YearMonthDurationValue> implements YearMonthDurationType
{
	private DatatypeFactory xmlDataTypesFactory;
	
	public YearMonthDurationTypeImpl(String typeId)
	{
		super(typeId, Duration.class);
		try{
			this.xmlDataTypesFactory = DatatypeFactory.newInstance();
		}catch(DatatypeConfigurationException e){
			throw new IllegalStateException(e);
		}
	}

	@Override
	public YearMonthDurationValue fromXacmlString(String v) {
		Preconditions.checkNotNull(v);
		Duration dayTimeDuration = xmlDataTypesFactory.newDurationDayTime(v);
		return new YearMonthDurationValue(this, validate(dayTimeDuration));
	}
	
	@Override
	public YearMonthDurationValue create(Object any){
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
