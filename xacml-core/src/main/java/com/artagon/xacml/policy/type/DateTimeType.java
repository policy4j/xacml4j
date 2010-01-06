package com.artagon.xacml.policy.type;

import org.joda.time.DateTime;

import com.artagon.xacml.policy.AttributeDataType;
import com.artagon.xacml.policy.BaseAttributeValue;

public interface DateTimeType extends AttributeDataType
{	
	DateTimeValue create(Object value);
	DateTimeValue fromXacmlString(String v);
	
	final class DateTimeValue extends BaseAttributeValue<DateTime>
	{
		public DateTimeValue(DateTimeType type, DateTime value) {
			super(type, value);
		}
	}
}