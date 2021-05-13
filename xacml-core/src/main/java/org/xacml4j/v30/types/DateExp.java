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
