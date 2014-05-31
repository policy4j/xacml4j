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

import java.io.Serializable;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.google.common.base.Preconditions;

public abstract class BaseCalendar <T extends BaseCalendar<?>>
	implements Serializable, Comparable<T>
{
	private static final long serialVersionUID = -1896156800821765849L;

	protected XMLGregorianCalendar calendar;

	private static DatatypeFactory df = null;

	static{
		try{
			df = DatatypeFactory.newInstance();
		}catch(DatatypeConfigurationException e){
		}
	}

	protected BaseCalendar(XMLGregorianCalendar calendar){
		Preconditions.checkNotNull(calendar);
		this.calendar =  calendar;
	}

	/**
	 * Creates an {@link XMLGregorianCalendar} from given object
	 *
	 * @param v a representation of {@link XMLGregorianCalendar}
	 * @return {@link XMLGregorianCalendar}
	 */
	private static XMLGregorianCalendar parseXmlCalendar(Object v){
		XMLGregorianCalendar c = null;
		if(String.class.isInstance(v)){
			c = df.newXMLGregorianCalendar((String)v);
		}
		if(XMLGregorianCalendar.class.isInstance(v)){
			c = (XMLGregorianCalendar)((XMLGregorianCalendar)v).clone();
		}
		if(c.getTimezone() == DatatypeConstants.FIELD_UNDEFINED){
			c.setTimezone(0);
		}
		return c;
	}

	protected static XMLGregorianCalendar parseDate(Object v){
		Preconditions.checkNotNull(v);
		XMLGregorianCalendar c = null;
		if(GregorianCalendar.class.isInstance(v)){
			c = df.newXMLGregorianCalendar((GregorianCalendar)v);
			c = df.newXMLGregorianCalendarDate(
					c.getYear(),
					c.getMonth(),
					c.getDay(),
					c.getTimezone());
			return c;
		}
		c = parseXmlCalendar(v);
		Preconditions.checkArgument(c.getXMLSchemaType().equals(DatatypeConstants.DATE));
		return c;
	}

	protected static XMLGregorianCalendar parseDateTime(Object v){
		Preconditions.checkNotNull(v);
		XMLGregorianCalendar c = null;
		if(GregorianCalendar.class.isInstance(v)){
			return df.newXMLGregorianCalendar((GregorianCalendar)v);
		}
		c = parseXmlCalendar(v);
		Preconditions.checkArgument(c.getXMLSchemaType().equals(DatatypeConstants.DATETIME));
		return c;
	}

	protected static XMLGregorianCalendar parseTime(Object v){
		Preconditions.checkNotNull(v);
		XMLGregorianCalendar c = null;
		if(GregorianCalendar.class.isInstance(v)){
			c = df.newXMLGregorianCalendar((GregorianCalendar)v);
			c = df.newXMLGregorianCalendarTime(
					c.getHour(),
					c.getMinute(),
					c.getSecond(),
					c.getTimezone());
			return c;
		}
		c = parseXmlCalendar(v);
		Preconditions.checkArgument(c.getXMLSchemaType().equals(DatatypeConstants.TIME));
		return c;
	}

	public XMLGregorianCalendar toCalendar(){
		return (XMLGregorianCalendar)calendar.clone();
	}

	public T add(BaseDuration<?> d){
		XMLGregorianCalendar c = (XMLGregorianCalendar)calendar.clone();
		c.add(d.getDuration());
		return makeCalendar(c);
	}

	public T subtract(BaseDuration<?> d){
		XMLGregorianCalendar c = (XMLGregorianCalendar)calendar.clone();
		c.add(d.getDuration().negate());
		return makeCalendar(c);
	}

	@Override
	public boolean equals(Object o){
		if(o == null){
			return false;
		}
		if(o == this){
			return true;
		}
		if(!(getClass().isInstance(o))){
			return false;
		}
		BaseCalendar<?> c = (BaseCalendar<?>)o;
		return calendar.equals(c.calendar);
	}

	@Override
	public int compareTo(T v) {
		int r = calendar.compare(v.calendar);
		if (r == DatatypeConstants.INDETERMINATE) {
			throw new IllegalArgumentException(String.format(
					"Can't compare a=\"%s\" with b=\"%s\", "
							+ "result is INDETERMINATE", calendar,
					v.calendar));
		}
		return r == DatatypeConstants.EQUAL ? 0
				: ((r == DatatypeConstants.GREATER) ? 1 : -1);
	}

	@Override
	public int hashCode(){
		return calendar.hashCode();
	}

	@Override
	public String toString(){
		return toXacmlString();
	}

	public String toXacmlString(){
		return calendar.toXMLFormat();
	}

	protected abstract T makeCalendar(XMLGregorianCalendar c);
}
