package com.artagon.xacml.policy.type;

import javax.xml.datatype.XMLGregorianCalendar;

import com.artagon.xacml.policy.AttributeDataType;
import com.artagon.xacml.policy.BagOfAttributesType;
import com.artagon.xacml.policy.BaseAttributeValue;
import com.artagon.xacml.policy.type.RFC822NameType.RFC822NameValue;

public interface TimeType extends AttributeDataType
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


