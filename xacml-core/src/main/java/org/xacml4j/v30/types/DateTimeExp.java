package org.xacml4j.v30.types;

import javax.xml.datatype.XMLGregorianCalendar;

import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.DateTime;

import com.google.common.base.Preconditions;

public final class DateTimeExp 
	extends BaseAttributeExp<DateTime> 
	implements Comparable<DateTimeExp>
{
	private static final long serialVersionUID = 1085808268199675887L;

	DateTimeExp(AttributeExpType type, 
			DateTime value) {
		super(type, value);
		Preconditions.checkArgument(
				DateTimeType.DATETIME.getDataTypeId().equals(type.getDataTypeId()));
	}
	
	DateTimeExp(DateTimeType type, XMLGregorianCalendar value) {
		super(type, new DateTime(value));
	}
	
	@Override
	public int compareTo(DateTimeExp o) {
		return getValue().compareTo(o.getValue());
	}
	
	public DateTimeExp add(DayTimeDurationExp v){
		return new DateTimeExp((DateTimeType)getType(), 
				getValue().add(v.getValue()));
	}
	
	public DateTimeExp subtract(DayTimeDurationExp v){
		return new DateTimeExp((DateTimeType)getType(), 
				getValue().subtract(v.getValue()));
	}
	
	public DateTimeExp subtract(YearMonthDurationExp v){
		return new DateTimeExp((DateTimeType)getType(), 
				getValue().subtract(v.getValue()));
	}
	
	public DateTimeExp add(YearMonthDurationExp v){
		return new DateTimeExp((DateTimeType)getType(), 
				getValue().add(v.getValue()));
	}
}