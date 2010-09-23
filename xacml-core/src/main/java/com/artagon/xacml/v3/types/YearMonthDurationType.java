package com.artagon.xacml.v3.types;

import java.util.Collection;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;

import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.BagOfAttributeValuesType;
import com.google.common.base.Preconditions;


public interface YearMonthDurationType extends AttributeValueType
{
	YearMonthDurationValue create(Object value, Object ...params);
	YearMonthDurationValue fromXacmlString(String v, Object ...params);
	
	BagOfAttributeValuesType bagType();
	
	final class YearMonthDurationValue extends BaseDurationValue
	{
		public YearMonthDurationValue(YearMonthDurationType type, 
				Duration value) {
			super(type, value);
			Preconditions.checkArgument(
					value.getXMLSchemaType() == DatatypeConstants.DURATION_YEARMONTH);
		}
	}
	
	public final class Factory
	{
		private final static YearMonthDurationType INSTANCE = new YearMonthDurationTypeImpl("http://www.w3.org/2001/XMLSchema#yearMonthDuration");
		
		public static YearMonthDurationType getInstance(){
			return INSTANCE;
		}
		
		public static YearMonthDurationValue create(Object v, Object ...params){
			return INSTANCE.create(v, params);
		}
		
		public static YearMonthDurationValue fromXacmlString(String v, Object ...params){
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



