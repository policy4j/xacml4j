package com.artagon.xacml.v30.types;

import javax.xml.datatype.XMLGregorianCalendar;

import com.artagon.xacml.v30.core.Time;

public final class TimeValueExp extends BaseAttributeExpression<Time>
		implements Comparable<TimeValueExp> 
{
	private static final long serialVersionUID = -8244143626423796791L;

	TimeValueExp(TimeType type, XMLGregorianCalendar value) {
		super(type, new Time(value));
	}
	
	TimeValueExp(TimeType type, Time time) {
		super(type, time);
	}
	
	@Override
	public int compareTo(TimeValueExp v) {
		return getValue().compareTo(v.getValue());
	}
}
