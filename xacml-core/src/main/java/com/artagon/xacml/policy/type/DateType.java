package com.artagon.xacml.policy.type;


import javax.xml.datatype.XMLGregorianCalendar;

import com.artagon.xacml.policy.AttributeDataType;
import com.artagon.xacml.policy.BaseAttributeValue;

public interface DateType  extends AttributeDataType
{
	String TYPE_ID = "http://www.w3.org/2001/XMLSchema#date";
	
	DateValue create(Object value);
	DateValue fromXacmlString(String v);
	
	final class DateValue extends BaseAttributeValue<XMLGregorianCalendar>
	{
		public DateValue(DateType type, XMLGregorianCalendar value) {
			super(type, value);
		}
	}
}
