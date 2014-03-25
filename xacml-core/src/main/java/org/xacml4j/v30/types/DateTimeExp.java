package org.xacml4j.v30.types;

import java.util.Calendar;

import javax.xml.datatype.XMLGregorianCalendar;

import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.DateTime;

public final class DateTimeExp
	extends BaseAttributeExp<DateTime>
	implements Comparable<DateTimeExp>
{
	private static final long serialVersionUID = 1085808268199675887L;

	DateTimeExp(DateTime value) {
		super(XacmlTypes.DATETIME, value);
	}
			
	public static DateTimeExp valueOf(String v){
		return new DateTimeExp(DateTime.create(v));
	}
	
	public static DateTimeExp valueOf(DateTime v){
		return new DateTimeExp(v);
	}
	
	public static DateTimeExp valueOf(XMLGregorianCalendar v){
		return new DateTimeExp(DateTime.create(v));
	}
	
	public static DateTimeExp valueOf(Calendar v){
		return new DateTimeExp(DateTime.create(v));
	}
	
	public static DateTimeExp valueOf(StringExp v){
		return valueOf(v.getValue());
	}
	
	public StringExp toStringExp(){
		return StringExp.valueOf(getValue().toXacmlString());
	}
	
	@Override
	public int compareTo(DateTimeExp o) {
		return getValue().compareTo(o.getValue());
	}

	public DateTimeExp add(DayTimeDurationExp v){
		return new DateTimeExp(
				getValue().add(v.getValue()));
	}

	public DateTimeExp subtract(DayTimeDurationExp v){
		return new DateTimeExp(getValue().subtract(v.getValue()));
	}

	public DateTimeExp subtract(YearMonthDurationExp v){
		return new DateTimeExp(getValue().subtract(v.getValue()));
	}

	public DateTimeExp add(YearMonthDurationExp v){
		return new DateTimeExp(getValue().add(v.getValue()));
	}
	
	public static BagOfAttributeExp emptyBag(){
		return XacmlTypes.DATETIME.emptyBag();
	}
	
	public static BagOfAttributeExp.Builder bag(){
		return XacmlTypes.DATETIME.bag();
	}
}
