package com.artagon.xacml.policy.type;

import javax.xml.datatype.XMLGregorianCalendar;

import com.artagon.xacml.policy.AttributeType;
import com.artagon.xacml.policy.BagOfAttributesType;
import com.artagon.xacml.policy.BaseAttributeValue;

public interface TimeType extends AttributeType
{	
	TimeValue create(Object value);
	TimeValue fromXacmlString(String v);
	BagOfAttributesType<TimeValue> bagOf();
	
	final class TimeValue extends BaseAttributeValue<XMLGregorianCalendar>
	{
		public TimeValue(TimeType type, XMLGregorianCalendar value) {
			super(type, value);
		}
	}
}


