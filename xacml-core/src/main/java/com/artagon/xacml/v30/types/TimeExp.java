package com.artagon.xacml.v30.types;

import javax.xml.datatype.XMLGregorianCalendar;

import com.artagon.xacml.v30.Time;

public final class TimeExp extends BaseAttributeExp<Time>
		implements Comparable<TimeExp> 
{
	private static final long serialVersionUID = -8244143626423796791L;

	TimeExp(TimeType type, XMLGregorianCalendar value) {
		super(type, new Time(value));
	}
	
	TimeExp(TimeType type, Time time) {
		super(type, time);
	}
	
	@Override
	public int compareTo(TimeExp v) {
		return getValue().compareTo(v.getValue());
	}
}
