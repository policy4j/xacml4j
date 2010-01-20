package com.artagon.xacml.policy.type;

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import com.artagon.xacml.DataTypeId;
import com.artagon.xacml.policy.AttributeDataType;
import com.artagon.xacml.policy.BaseAttributeValue;
import com.artagon.xacml.policy.type.DayTimeDurationType.DayTimeDurationValue;
import com.artagon.xacml.policy.type.YearMonthDurationType.YearMonthDurationValue;

public interface DateTimeType extends AttributeDataType
{	
	DateTimeValue create(Object value);
	DateTimeValue fromXacmlString(String v);

	public enum DateTimeTypeId implements DataTypeId
	{
		DATETIME("http://www.w3.org/2001/XMLSchema#dateTime");
		
		private String typeId;
		
		private DateTimeTypeId(String typeId){
			this.typeId = typeId;
		}
		
		public String toString(){
			return typeId;
		}
	}
	
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