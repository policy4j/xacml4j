package com.artagon.xacml.v3.types;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;

import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.BagOfAttributeValuesType;
import com.google.common.base.Preconditions;


public interface YearMonthDurationType extends AttributeValueType
{
	YearMonthDurationValue create(Object value, Object ...params);
	YearMonthDurationValue fromXacmlString(String v, Object ...params);
	
	BagOfAttributeValuesType<YearMonthDurationValue> bagOf();
	
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
	}
}



