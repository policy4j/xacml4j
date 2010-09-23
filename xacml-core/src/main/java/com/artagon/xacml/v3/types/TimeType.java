package com.artagon.xacml.v3.types;

import java.util.Collection;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.BagOfAttributeValues;

public interface TimeType extends AttributeValueType
{	
	TimeValue create(Object value, Object ...params);
	TimeValue fromXacmlString(String v, Object ...params);
	
	final class TimeValue extends SimpleAttributeValue<XMLGregorianCalendar> 
		implements Comparable<TimeValue>
	{
		public TimeValue(TimeType type, XMLGregorianCalendar value) {
			super(type, value);
		}
				
		@Override
		public int compareTo(TimeValue v) {
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
		private final static TimeType INSTANCE = new TimeTypeImpl("http://www.w3.org/2001/XMLSchema#time");
		
		public static TimeType getInstance(){
			return INSTANCE;
		}
		
		public static TimeValue create(Object v, Object ...params){
			return INSTANCE.create(v, params);
		}
		
		public static TimeValue fromXacmlString(String v, Object ...params){
			return INSTANCE.fromXacmlString(v, params);
		}
		
		public static BagOfAttributeValues bagOf(AttributeValue ...values){
			return INSTANCE.bagType().create(values);
		}
		
		public static BagOfAttributeValues bagOf(Collection<AttributeValue> values){
			return INSTANCE.bagType().create(values);
		}
		
		public static BagOfAttributeValues emptyBag(){
			return INSTANCE.bagType().createEmpty();
		}
	}
}


