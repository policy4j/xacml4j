package com.artagon.xacml.v30.types;

import javax.xml.datatype.XMLGregorianCalendar;

import com.artagon.xacml.v30.core.Date;

public final class DateValueExp extends BaseAttributeExpression<Date> 
	implements Comparable<DateValueExp>
{
	private static final long serialVersionUID = -4744947303379182831L;

	DateValueExp(DateType type, Date value) {
		super(type, value);
	}
	
	DateValueExp(DateType type, XMLGregorianCalendar value) {
		super(type, new Date(value));
	}
	
	@Override
	public int compareTo(DateValueExp o) {
		return getValue().compareTo(o.getValue());
	}
	
	public DateValueExp subtract(YearMonthDurationValueExp v){
		return new DateValueExp((DateType)getType(), getValue().subtract(v.getValue()));
	}
	
	public DateValueExp add(YearMonthDurationValueExp v){
		return new DateValueExp((DateType)getType(), getValue().add(v.getValue()));
	}
}
