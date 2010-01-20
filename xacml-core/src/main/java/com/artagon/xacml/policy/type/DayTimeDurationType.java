package com.artagon.xacml.policy.type;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;

import com.artagon.xacml.policy.AttributeDataType;
import com.artagon.xacml.policy.BaseAttributeValue;
import com.artagon.xacml.util.Preconditions;


public interface DayTimeDurationType extends AttributeDataType
{
	String TYPE_ID = "urn:oasis:names:tc:xacml:2.0:data-type:dayTimeDuration";
	
	DayTimeDurationValue create(Object value);
	DayTimeDurationValue fromXacmlString(String v);
	
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

