package org.xacml4j.v30.types;

import java.util.Calendar;

import javax.xml.datatype.XMLGregorianCalendar;

import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.Time;


public final class TimeExp extends BaseAttributeExp<Time>
		implements Comparable<TimeExp>
{
	private static final long serialVersionUID = -8244143626423796791L;

	TimeExp(XMLGregorianCalendar value) {
		super(XacmlTypes.TIME, new Time(value));
	}

	TimeExp(Time time) {
		super(XacmlTypes.TIME, time);
	}
	
	public static TimeExp valueOf(String v){
		return new TimeExp(Time.valueOf(v));
	}
	
	public static TimeExp valueOf(XMLGregorianCalendar v){
		return new TimeExp(Time.valueOf(v));
	}
	
	public static TimeExp valueOf(Calendar v){
		return new TimeExp(Time.valueOf(v));
	}
	
	public static TimeExp valueOf(Time v){
		return new TimeExp(v);
	}
	
	public StringExp toStringExp(){
		return StringExp.valueOf(getValue().toXacmlString());
	}
	
	public static BagOfAttributeExp emptyBag(){
		return XacmlTypes.TIME.emptyBag();
	}
	
	public static BagOfAttributeExp.Builder bag(){
		return XacmlTypes.TIME.bag();
	}

	@Override
	public int compareTo(TimeExp v) {
		return getValue().compareTo(v.getValue());
	}
}
