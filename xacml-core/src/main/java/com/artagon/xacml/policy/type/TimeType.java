package com.artagon.xacml.policy.type;

import javax.xml.datatype.XMLGregorianCalendar;

import com.artagon.xacml.policy.AttributeValueType;
import com.artagon.xacml.policy.BagOfAttributeValuesType;

public interface TimeType extends AttributeValueType
{	
	TimeValue create(Object value);
	TimeValue fromXacmlString(String v);
	BagOfAttributeValuesType<TimeValue> bagOf();
	
	final class TimeValue extends BaseAttributeValue<XMLGregorianCalendar>
	{
		public TimeValue(TimeType type, XMLGregorianCalendar value) {
			super(type, value);
		}
	}
}


