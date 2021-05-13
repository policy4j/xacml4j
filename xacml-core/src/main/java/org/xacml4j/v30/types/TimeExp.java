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
import org.xacml4j.v30.Time;


public final class TimeExp extends BaseAttributeExp<Time>
		implements Comparable<TimeExp>
{
	private static final long serialVersionUID = -8244143626423796791L;

	TimeExp(XMLGregorianCalendar value) {
		super(XacmlTypes.TIME, new Time(value));
	}

	TimeExp(Time time) {
		super(XacmlTypes.TIME, time);
	}
	
	public static TimeExp valueOf(String v){
		return new TimeExp(Time.valueOf(v));
	}
	
	public static TimeExp valueOf(XMLGregorianCalendar v){
		return new TimeExp(Time.valueOf(v));
	}
	
	public static TimeExp valueOf(Calendar v){
		return new TimeExp(Time.valueOf(v));
	}
	
	public static TimeExp valueOf(Time v){
		return new TimeExp(v);
	}
	
	public StringExp toStringExp(){
		return StringExp.valueOf(getValue().toXacmlString());
	}
	
	public static BagOfAttributeExp emptyBag(){
		return XacmlTypes.TIME.emptyBag();
	}
	
	public static BagOfAttributeExp.Builder bag(){
		return XacmlTypes.TIME.bag();
	}

	@Override
	public int compareTo(TimeExp v) {
		return getValue().compareTo(v.getValue());
	}
}
