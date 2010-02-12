package com.artagon.xacml.v3.policy.type;


import javax.xml.datatype.XMLGregorianCalendar;

import com.artagon.xacml.v3.policy.AttributeValueType;

public interface DateType extends AttributeValueType
{
	DateValue create(Object value, Object ...params);
	DateValue fromXacmlString(String v, Object ...params);
	
	final class DateValue extends BaseAttributeValue<XMLGregorianCalendar>
	{
		public DateValue(DateType type, XMLGregorianCalendar value) {
			super(type, value);
		}
	}
}
