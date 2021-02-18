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

import com.google.common.base.Preconditions;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.GregorianCalendar;

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
	 * object defaultProvider, supported types are:
	 * {@link String}, {@link GregorianCalendar},
	 * {@link XMLGregorianCalendar}
	 *
	 * @return {@link DateTime} defaultProvider
	 */
	public static DateTime of(Object v){
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

	public int getMillisecond(){
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
