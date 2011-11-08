package com.artagon.xacml.v30.types;

import javax.xml.datatype.XMLGregorianCalendar;

import com.artagon.xacml.v30.AttributeExpType;
import com.artagon.xacml.v30.core.DateTime;
import com.google.common.base.Preconditions;

public final class DateTimeValueExp 
	extends BaseAttributeExpression<DateTime> 
	implements Comparable<DateTimeValueExp>
{
	private static final long serialVersionUID = 1085808268199675887L;

	DateTimeValueExp(AttributeExpType type, 
			DateTime value) {
		super(type, value);
		Preconditions.checkArgument(
				DateTimeType.DATETIME.getDataTypeId().equals(type.getDataTypeId()));
	}
	
	DateTimeValueExp(DateTimeType type, XMLGregorianCalendar value) {
		super(type, new DateTime(value));
	}
	
	@Override
	public int compareTo(DateTimeValueExp o) {
		return getValue().compareTo(o.getValue());
	}
	
	public DateTimeValueExp add(DayTimeDurationValueExp v){
		return new DateTimeValueExp((DateTimeType)getType(), 
				getValue().add(v.getValue()));
	}
	
	public DateTimeValueExp subtract(DayTimeDurationValueExp v){
		return new DateTimeValueExp((DateTimeType)getType(), 
				getValue().subtract(v.getValue()));
	}
	
	public DateTimeValueExp subtract(YearMonthDurationValueExp v){
		return new DateTimeValueExp((DateTimeType)getType(), 
				getValue().subtract(v.getValue()));
	}
	
	public DateTimeValueExp add(YearMonthDurationValueExp v){
		return new DateTimeValueExp((DateTimeType)getType(), 
				getValue().add(v.getValue()));
	}
}