package com.artagon.xacml.v3.policy.type;

import javax.xml.datatype.XMLGregorianCalendar;

import com.artagon.xacml.v3.policy.AttributeValueType;
import com.artagon.xacml.v3.policy.BagOfAttributeValuesType;

public interface TimeType extends AttributeValueType
{	
	TimeValue create(Object value, Object ...params);
	TimeValue fromXacmlString(String v, Object ...params);
	BagOfAttributeValuesType<TimeValue> bagOf();
	
	final class TimeValue extends BaseAttributeValue<XMLGregorianCalendar>
	{
		public TimeValue(TimeType type, XMLGregorianCalendar value) {
			super(type, value);
		}
	}
}


