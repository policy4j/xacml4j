package org.xacml4j.v30.types;

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

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.GregorianCalendar;

public final class Date extends BaseCalendar<Date>
{
	private static final long serialVersionUID = -79539790774966290L;

	public Date(XMLGregorianCalendar value) {
		super(value);
	}

	/**
	 * Creates {@link Date} from a given
	 * object defaultProvider, supported types are:
	 * {@link String}, {@link GregorianCalendar},
	 * {@link XMLGregorianCalendar}
	 *
	 * @return {@link Date} defaultProvider
	 */
	public static Date of(Object v){
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
