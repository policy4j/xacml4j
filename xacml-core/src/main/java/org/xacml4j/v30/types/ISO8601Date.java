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

import org.xacml4j.util.DateTimeDurationUtils;
import org.xacml4j.v30.SyntaxException;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.XMLGregorianCalendar;

/**
 *
 */
public final class ISO8601Date extends Value.OtherValue<LocalDate>
    implements Comparable<ISO8601Date>
{
    private static final long serialVersionUID = -4744947303379182831L;

    private ISO8601Date(LocalDate value) {
        super(XacmlTypes.DATE, value);
    }

    public static ISO8601Date ofAny(Object v,
                             Object...params)
    {
        if (v instanceof java.lang.String) {
            return ISO8601Date.of((java.lang.String) v);
        }
        if (v instanceof StringVal) {
            return ISO8601Date.of(((StringVal) v));
        }
        if (v instanceof Date) {
            return ISO8601Date.of((Date)v);
        }
        if (v instanceof GregorianCalendar) {
            return ISO8601Date.of((GregorianCalendar) v);
        }
        if (v instanceof ZonedDateTime) {
            return ISO8601Date.of((ZonedDateTime)v);
        }
        if (v instanceof OffsetDateTime) {
            return ISO8601Date.of((OffsetDateTime)v);
        }
        if (v instanceof XMLGregorianCalendar) {
            return ISO8601Date.of((XMLGregorianCalendar) v);
        }
        throw SyntaxException.invalidAttributeValue(v,
                XacmlTypes.DATE);
    }

    public static ISO8601Date of(java.lang.String v){
        return new ISO8601Date(LocalDate.parse(v, DateTimeFormatter.ISO_DATE));
    }

    public static ISO8601Date of(LocalDate date){
        return new ISO8601Date(date);
    }

    public static ISO8601Date of(StringVal v){
        return new ISO8601Date(LocalDate.parse(v.get(), DateTimeFormatter.ISO_DATE));
    }

    public static ISO8601Date of(Date v){
        return of(v.toInstant().atOffset(ZoneOffset.UTC));
    }

    public static ISO8601Date of(XMLGregorianCalendar v){
        return new ISO8601Date(DateTimeDurationUtils.toISO860Date(v));
    }

    public static ISO8601Date of(ZonedDateTime v){
        return new ISO8601Date(v.toLocalDate());
    }

    public static ISO8601Date of(OffsetDateTime v){
        return new ISO8601Date(v.toLocalDate());
    }

    public static ISO8601Date of(GregorianCalendar v){
        return new ISO8601Date(DateTimeDurationUtils.toISO860Date(v));
    }

    public StringVal toStringExp(){
        return StringVal.of(toXacmlString());
    }

    public StringVal toStringExp(DateTimeFormatter df){
        return StringVal.of(value.format(df));
    }

    public String toXacmlString(){
        return value.format(DateTimeFormatter.ISO_DATE);
    }

    protected String formatVal(LocalDate v){
        return toXacmlString();
    }

    public int getYear(){
        return value.getYear();
    }

    public Month getMonth(){
        return value.getMonth();
    }
    public int getMonthValue(){
        return value.getMonthValue();
    }

    public int getDayOfMonth(){
        return value.getDayOfMonth();
    }

    @Override
    public int compareTo(ISO8601Date o) {
        return get().compareTo(o.get());
    }

    public ISO8601Date plus(ISO8601YearMonthDuration duration){
        return ISO8601Date.of(value.plus(duration.value));
    }

    public ISO8601Date minus(ISO8601YearMonthDuration duration){
        return ISO8601Date.of(value.minus(duration.value));
    }
}
