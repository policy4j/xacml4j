package org.xacml4j.v30;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

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
	public static Time valueOf(Object v){
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

	public int getMillisecond(){
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
