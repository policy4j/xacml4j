package com.artagon.xacml.v3.types;


import java.util.Collection;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.types.YearMonthDurationType.YearMonthDurationValue;

public interface DateType extends AttributeValueType
{
	DateValue create(Object value, Object ...params);
	DateValue fromXacmlString(String v, Object ...params);
	
	final class DateValue extends BaseAttributeValue<XMLGregorianCalendar> 
		implements Comparable<DateValue>
	{
		public DateValue(DateType type, XMLGregorianCalendar value) {
			super(type, value);
		}
		
			
		public DateValue add(YearMonthDurationValue duration){
			return add(duration.getValue());
		}
		
		public DateValue subtract(YearMonthDurationValue duration){
			return subtract(duration.getValue());
		}
				
		private DateValue add(Duration duration){
			XMLGregorianCalendar dateTime = getValue();
			XMLGregorianCalendar copy = (XMLGregorianCalendar)dateTime.clone();
			copy.add(duration);
			return new DateValue((DateType)getType(), copy);
		}
		
		private DateValue subtract(Duration duration){
			return add(duration.negate());
		}
		
		@Override
		public int compareTo(DateValue v) {
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
		private final static DateType INSTANCE = new DateTypeImpl("http://www.w3.org/2001/XMLSchema#date");
		
		public static DateType getInstance(){
			return INSTANCE;
		}
		
		public static DateValue create(Object v, Object ...params){
			return INSTANCE.create(v, params);
		}
		
		public static DateValue fromXacmlString(String v, Object ...params){
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
