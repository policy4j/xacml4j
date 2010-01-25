package com.artagon.xacml.policy.type;


import javax.xml.datatype.XMLGregorianCalendar;

import com.artagon.xacml.policy.AttributeType;

public interface DateType extends AttributeType
{
	DateValue create(Object value);
	DateValue fromXacmlString(String v);
	
	final class DateValue extends BaseAttributeValue<XMLGregorianCalendar>
	{
		public DateValue(DateType type, XMLGregorianCalendar value) {
			super(type, value);
		}
	}
}
