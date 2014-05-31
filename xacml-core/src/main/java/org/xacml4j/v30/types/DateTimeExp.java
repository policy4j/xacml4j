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
