package com.artagon.xacml.v3.policy.type;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.policy.AttributeValueType;
import com.artagon.xacml.v3.policy.BagOfAttributeValuesType;


public interface DayTimeDurationType extends AttributeValueType
{
	DayTimeDurationValue create(Object value);
	DayTimeDurationValue fromXacmlString(String v);
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

