package com.artagon.xacml.v3.policy.type;

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import com.artagon.xacml.v3.policy.AttributeValueType;
import com.artagon.xacml.v3.policy.BagOfAttributeValuesType;
import com.artagon.xacml.v3.policy.type.DayTimeDurationType.DayTimeDurationValue;
import com.artagon.xacml.v3.policy.type.YearMonthDurationType.YearMonthDurationValue;

public interface DateTimeType extends AttributeValueType
{	
	DateTimeValue create(Object value, Object ...params);
	DateTimeValue fromXacmlString(String v, Object ...params);
	BagOfAttributeValuesType<DateTimeValue> bagOf();
	
	final class DateTimeValue extends BaseAttributeValue<XMLGregorianCalendar>
	{
		public DateTimeValue(DateTimeType type, XMLGregorianCalendar value) {
			super(type, value);
		}
		
		public DateTimeValue add(DayTimeDurationValue duration){
			return add(duration.getValue());
		}
		
		public DateTimeValue add(YearMonthDurationValue duration){
			return add(duration.getValue());
		}
		
		public DateTimeValue subtract(YearMonthDurationValue duration){
			return subtract(duration.getValue());
		}
		
		private DateTimeValue add(Duration duration){
			XMLGregorianCalendar dateTime = getValue();
			XMLGregorianCalendar copy = (XMLGregorianCalendar)dateTime.clone();
			copy.add(duration);
			return new DateTimeValue((DateTimeType)getType(), copy);
		}
		
		private DateTimeValue subtract(Duration duration){
			return duration.getSign() == -1?add(duration):add(duration.negate());
		}
	}
}