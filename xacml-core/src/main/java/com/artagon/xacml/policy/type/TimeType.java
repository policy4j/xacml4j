package com.artagon.xacml.policy.type;

import javax.xml.datatype.XMLGregorianCalendar;

import com.artagon.xacml.policy.AttributeDataType;
import com.artagon.xacml.policy.BaseAttributeValue;

public interface TimeType extends AttributeDataType
{	
	TimeValue create(Object value);
	TimeValue fromXacmlString(String v);
	
	final class TimeValue extends BaseAttributeValue<XMLGregorianCalendar>
	{
		public TimeValue(TimeType type, XMLGregorianCalendar value) {
			super(type, value);
		}
	}
}


