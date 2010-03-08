package com.artagon.xacml.v3.policy.type;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import com.artagon.xacml.v3.policy.AttributeValueType;
import com.artagon.xacml.v3.policy.BagOfAttributeValuesType;

public interface TimeType extends AttributeValueType
{	
	TimeValue create(Object value, Object ...params);
	TimeValue fromXacmlString(String v, Object ...params);
	BagOfAttributeValuesType<TimeValue> bagOf();
	
	final class TimeValue extends BaseAttributeValue<XMLGregorianCalendar> 
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
}


