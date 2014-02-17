package org.xacml4j.v30.types;

import javax.xml.datatype.XMLGregorianCalendar;

import org.xacml4j.v30.Date;


public final class DateExp extends BaseAttributeExp<Date>
	implements Comparable<DateExp>
{
	private static final long serialVersionUID = -4744947303379182831L;

	public DateExp(Date value) {
		super(DateType.DATE, value);
	}

	public DateExp(XMLGregorianCalendar value) {
		super(DateType.DATE, new Date(value));
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
}
