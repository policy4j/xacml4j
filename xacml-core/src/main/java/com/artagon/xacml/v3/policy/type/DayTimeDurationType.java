package com.artagon.xacml.v3.policy.type;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.BagOfAttributeValuesType;
import com.artagon.xacml.v3.policy.spi.type.BaseAttributeValue;


public interface DayTimeDurationType extends AttributeValueType
{
	DayTimeDurationValue create(Object value, Object ...params);
	DayTimeDurationValue fromXacmlString(String v, Object ...params);
	BagOfAttributeValuesType<DayTimeDurationValue> bagOf();
	
	final class DayTimeDurationValue extends BaseAttributeValue<Duration>
	{
		public DayTimeDurationValue(DayTimeDurationType type, 
				Duration value) {
			super(type, value);
			Preconditions.checkArgument(!value.isSet(DatatypeConstants.YEARS) && 
					!value.isSet(DatatypeConstants.MONTHS));
		}
	}
}

