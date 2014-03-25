package org.xacml4j.v30.types;

import java.util.Calendar;

import javax.xml.datatype.XMLGregorianCalendar;

import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.Date;


public final class DateExp extends BaseAttributeExp<Date>
	implements Comparable<DateExp>
{
	private static final long serialVersionUID = -4744947303379182831L;

	DateExp(Date value) {
		super(XacmlTypes.DATE, value);
	}

	DateExp(XMLGregorianCalendar value) {
		super(XacmlTypes.DATE, new Date(value));
	}
	
	public static DateExp valueOf(String v){
		return new DateExp(Date.valueOf(v));
	}
	
	public static DateExp valueOf(StringExp v){
		return valueOf(v.getValue());
	}
	
	public static DateExp valueOf(XMLGregorianCalendar v){
		return new DateExp(Date.valueOf(v));
	}
	
	public static DateExp valueOf(Calendar v){
		return new DateExp(Date.valueOf(v));
	}
	
	public static DateExp valueOf(Date v){
		return new DateExp(v);
	}
	
	public StringExp toStringExp(){
		return StringExp.valueOf(getValue().toXacmlString());
	}
	
	@Override
	public int compareTo(DateExp o) {
		return getValue().compareTo(o.getValue());
	}

	public DateExp subtract(YearMonthDurationExp v){
		return new DateExp(getValue().subtract(v.getValue()));
	}

	public DateExp add(YearMonthDurationExp v){
		return new DateExp(getValue().add(v.getValue()));
	}
	
	public static BagOfAttributeExp emptyBag(){
		return XacmlTypes.DATE.emptyBag();
	}
	
	public static BagOfAttributeExp.Builder bag(){
		return XacmlTypes.DATE.bag();
	}
}
