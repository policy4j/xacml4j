package com.artagon.xacml.v3.policy.type;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.policy.type.DayTimeDurationType.DayTimeDurationValue;

final class DayTimeDurationTypeImpl extends BaseAttributeType<DayTimeDurationValue> implements DayTimeDurationType
{
	private DatatypeFactory xmlDataTypesFactory;
	
	public DayTimeDurationTypeImpl(String typeId)
	{
		super(typeId, Duration.class);
		try{
			this.xmlDataTypesFactory = DatatypeFactory.newInstance();
		}catch(DatatypeConfigurationException e){
			throw new IllegalStateException(e);
		}
	}

	@Override
	public DayTimeDurationValue fromXacmlString(String v) {
		Preconditions.checkNotNull(v);
		Duration dayTimeDuration = xmlDataTypesFactory.newDurationDayTime(v);
		return new DayTimeDurationValue(this, validate(dayTimeDuration));
	}
	
	@Override
	public DayTimeDurationValue create(Object any){
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
}


