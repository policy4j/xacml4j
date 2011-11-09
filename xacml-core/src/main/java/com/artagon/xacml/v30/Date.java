package com.artagon.xacml.v30;

import java.util.GregorianCalendar;

import javax.xml.datatype.XMLGregorianCalendar;

public final class Date extends BaseCalendar<Date> 
{
	private static final long serialVersionUID = -79539790774966290L;
	
	public Date(XMLGregorianCalendar value) {
		super(value);
	}
	
	/**
	 * Creates {@link Date} from a given
	 * object instance, supported types are:
	 * {@link String}, {@link GregorianCalendar},
	 * {@link XMLGregorianCalendar}
	 * 
	 * @return {@link Date} instance
	 */
	public static Date create(Object v){
		return new Date(parseDate(v));
	}
	
	public int getYear(){
		return calendar.getYear();
	}
	
	public int getMonth(){
		return calendar.getMonth();
	}
	
	public int getDay(){
		return calendar.getDay();
	}

	@Override
	protected Date makeCalendar(XMLGregorianCalendar c) {
		return new Date(c);
	}
}
