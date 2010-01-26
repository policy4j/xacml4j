package com.artagon.xacml.v3.policy.type;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.policy.AttributeValueType;
import com.artagon.xacml.v3.policy.BagOfAttributeValuesType;


public interface YearMonthDurationType extends AttributeValueType
{
	YearMonthDurationValue create(Object value);
	YearMonthDurationValue fromXacmlString(String v);
	
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



