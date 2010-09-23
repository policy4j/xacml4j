package com.artagon.xacml.v3.types;

import java.util.Collection;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;

import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.google.common.base.Preconditions;


public interface DayTimeDurationType extends AttributeValueType
{
	DayTimeDurationValue create(Object value, Object ...params);
	DayTimeDurationValue fromXacmlString(String v, Object ...params);
	
	final class DayTimeDurationValue extends BaseDurationValue
	{
		public DayTimeDurationValue(DayTimeDurationType type, 
				Duration value) {
			super(type, value);
			Preconditions.checkArgument(!value.isSet(DatatypeConstants.YEARS) && 
					!value.isSet(DatatypeConstants.MONTHS));
		}
	}
	
	public final class Factory
	{
		private final static DayTimeDurationType INSTANCE = new DayTimeDurationTypeImpl("http://www.w3.org/2001/XMLSchema#dayTimeDuration");
		
		public static DayTimeDurationType getInstance(){
			return INSTANCE;
		}
		
		public static DayTimeDurationValue create(Object v, Object ...params){
			return INSTANCE.create(v, params);
		}
		
		public static DayTimeDurationValue fromXacmlString(String v, Object ...params){
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

