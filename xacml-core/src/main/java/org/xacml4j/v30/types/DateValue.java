package org.xacml4j.v30.types;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2019 Xacml4J.org
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

/**
 *
 */
public final class DateValue extends BaseValue<Date>
    implements Comparable<DateValue>
{
    private static final long serialVersionUID = -4744947303379182831L;

    private DateValue(Date value) {
        super(XacmlTypes.DATE, value);
    }

    private DateValue(XMLGregorianCalendar value) {
        super(XacmlTypes.DATE, new Date(value));
    }


    static DateValue fromObjectWithParams(Object v,
                                            Object...params)
    {
        if (v instanceof String) {
            return DateValue.of((String) v);
        }
        if (v instanceof StringValue) {
            return DateValue.of(((StringValue) v).value());
        }
        if (v instanceof Calendar) {
            return DateValue.of((Calendar) v);
        }
        if (v instanceof XMLGregorianCalendar) {
            return DateValue.of(v.toString());
        }
        return DateValue.of((Date) v);
    }

    static DateValue of(String v){
        return new DateValue(Date.of(v));
    }

    static DateValue of(StringValue v){
        return of(v.value());
    }

    static DateValue of(XMLGregorianCalendar v){
        return new DateValue(Date.of(v));
    }

    static DateValue of(Calendar v){
        return new DateValue(Date.of(v));
    }

    static DateValue of(Date v){
        return new DateValue(v);
    }

    public StringValue toStringExp(){
        return StringValue.of(value().toXacmlString());
    }

    @Override
    public int compareTo(DateValue o) {
        return value().compareTo(o.value());
    }

    public DateValue subtract(YearMonthDurationValue v){
        return new DateValue(value().subtract(v.value()));
    }

    public DateValue add(YearMonthDurationValue v){
        return new DateValue(value().add(v.value()));
    }
}
