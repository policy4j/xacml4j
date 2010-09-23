package com.artagon.xacml.v3.types;

import java.util.Collection;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.BagOfAttributeValuesType;
import com.artagon.xacml.v3.types.DayTimeDurationType.DayTimeDurationValue;
import com.artagon.xacml.v3.types.YearMonthDurationType.YearMonthDurationValue;

public interface DateTimeType extends AttributeValueType
{	
	DateTimeValue create(Object value, Object ...params);
	DateTimeValue fromXacmlString(String v, Object ...params);
	BagOfAttributeValuesType bagOf();
	
	final class DateTimeValue extends BaseAttributeValue<XMLGregorianCalendar> 
		implements Comparable<DateTimeValue>
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
		
		public DateTimeValue subtract(DayTimeDurationValue duration){
			return subtract(duration.getValue());
		}
		
		private DateTimeValue add(Duration duration)
		{
			XMLGregorianCalendar dateTime = getValue();
			XMLGregorianCalendar copy = (XMLGregorianCalendar)dateTime.clone();
			copy.add(duration);
			return new DateTimeValue((DateTimeType)getType(), copy);
		}
		
		private DateTimeValue subtract(Duration duration){
			return add(duration.negate());
		}

		@Override
		public int compareTo(DateTimeValue v) {
			int r = getValue().compare(v.getValue());
			if(r == DatatypeConstants.INDETERMINATE){
				throw new IllegalArgumentException(
						String.format("Can't compare a=\"%s\" with b=\"%s\", " +
								"result is INDETERMINATE", getValue(), v.getValue()));
			}
			return r == DatatypeConstants.EQUAL?0:(
					(r == DatatypeConstants.GREATER)?1:-1);
		}
	}
	
	public final class Factory
	{
		private final static DateTimeType INSTANCE = new DateTimeTypeImpl("http://www.w3.org/2001/XMLSchema#dateTime");
		
		public static DateTimeType getInstance(){
			return INSTANCE;
		}
		
		public static DateTimeValue create(Object v, Object ...params){
			return INSTANCE.create(v, params);
		}
		
		public static DateTimeValue fromXacmlString(String v, Object ...params){
			return INSTANCE.fromXacmlString(v, params);
		}
		
		public static BagOfAttributeValues bagOf(AttributeValue ...values){
			return INSTANCE.bagOf().create(values);
		}
		
		public static BagOfAttributeValues bagOf(Collection<AttributeValue> values){
			return INSTANCE.bagOf().create(values);
		}
		
		public static BagOfAttributeValues emptyBag(){
			return INSTANCE.bagOf().createEmpty();
		}
	}
}