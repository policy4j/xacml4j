package com.artagon.xacml.v30;

import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import com.google.common.base.Preconditions;

public class Time extends BaseCalendar<Time> 
{
	private static final long serialVersionUID = -5881356998754053591L;
	
	public Time(XMLGregorianCalendar value) {
		super(value);
	}
	
	/**
	 * Creates {@link Time} from a given
	 * object instance, supported types are:
	 * {@link String}, {@link GregorianCalendar},
	 * {@link XMLGregorianCalendar}
	 * 
	 * @return {@link Time} instance
	 */
	public static Time create(Object v){
		return new Time(parseTime(v));
	}
	
	public int getHour(){
		return calendar.getHour();
	}
	
	public int getMinute(){
		return calendar.getMinute();
	}
	
	public int getSecond(){
		return calendar.getSecond();
	}
	
	public int getMilisecond(){
		return calendar.getMillisecond();
	}
	
	public int getTimezoneOffset(){
		Preconditions.checkState(isTimezoneSet());
		return calendar.getTimezone();
	}
	
	public boolean isTimezoneSet(){
		return calendar.getTimezone() != DatatypeConstants.FIELD_UNDEFINED;
	}

	@Override
	protected Time makeCalendar(XMLGregorianCalendar c) {
		return new Time(c);
	}
}
