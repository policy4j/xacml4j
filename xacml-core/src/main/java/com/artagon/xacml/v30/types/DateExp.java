package com.artagon.xacml.v30.types;

import javax.xml.datatype.XMLGregorianCalendar;

import com.artagon.xacml.v30.core.Date;

public final class DateExp extends BaseAttributeExp<Date> 
	implements Comparable<DateExp>
{
	private static final long serialVersionUID = -4744947303379182831L;

	DateExp(DateType type, Date value) {
		super(type, value);
	}
	
	DateExp(DateType type, XMLGregorianCalendar value) {
		super(type, new Date(value));
	}
	
	@Override
	public int compareTo(DateExp o) {
		return getValue().compareTo(o.getValue());
	}
	
	public DateExp subtract(YearMonthDurationExp v){
		return new DateExp((DateType)getType(), getValue().subtract(v.getValue()));
	}
	
	public DateExp add(YearMonthDurationExp v){
		return new DateExp((DateType)getType(), getValue().add(v.getValue()));
	}
}
