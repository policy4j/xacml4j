package com.artagon.xacml.v3.policy.type;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;

import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.BagOfAttributeValuesType;
import com.artagon.xacml.v3.policy.spi.type.BaseAttributeValue;
import com.google.common.base.Preconditions;


public interface YearMonthDurationType extends AttributeValueType
{
	YearMonthDurationValue create(Object value, Object ...params);
	YearMonthDurationValue fromXacmlString(String v, Object ...params);
	
	BagOfAttributeValuesType<YearMonthDurationValue> bagOf();
	
	final class YearMonthDurationValue extends BaseAttributeValue<Duration>
	{
		public YearMonthDurationValue(YearMonthDurationType type, 
				Duration value) {
			super(type, value);
			Preconditions.checkArgument(
					value.getXMLSchemaType() == DatatypeConstants.DURATION_YEARMONTH);
		}
	}
}



