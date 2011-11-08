package com.artagon.xacml.v30.core;

import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import com.google.common.base.Preconditions;

public final class DateTime extends 
	BaseCalendar<DateTime> 
{
	private static final long serialVersionUID = 1085808268199675887L;
	
	public DateTime(XMLGregorianCalendar value) {
		super(value);
		Preconditions.checkArgument(
				value.getXMLSchemaType().equals(DatatypeConstants.DATETIME), 
				"Given value=\"%s\" does " + "not represent type=\"%s\"",
				value.toXMLFormat(), DatatypeConstants.DATETIME);
	}
	
	/**
	 * Creates {@link DateTime} from a given
	 * object instance, supported types are:
	 * {@link String}, {@link GregorianCalendar},
	 * {@link XMLGregorianCalendar}
	 * 
	 * @return {@link DateTime} instance
	 */
	public static DateTime create(Object v){
		return new DateTime(parseDateTime(v));
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
		return calendar.getTimezone();
	}

	@Override
	protected DateTime makeCalendar(XMLGregorianCalendar c) {
		return new DateTime(c);
	}
}
