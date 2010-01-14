package com.artagon.xacml.policy.type;

import javax.xml.datatype.XMLGregorianCalendar;

import com.artagon.xacml.policy.AttributeDataType;
import com.artagon.xacml.policy.BaseAttributeValue;

public interface DateTimeType extends AttributeDataType
{	
	DateTimeValue create(Object value);
	DateTimeValue fromXacmlString(String v);
	
	final class DateTimeValue extends BaseAttributeValue<XMLGregorianCalendar>
	{
		public DateTimeValue(DateTimeType type, XMLGregorianCalendar value) {
			super(type, value);
		}
	}
}